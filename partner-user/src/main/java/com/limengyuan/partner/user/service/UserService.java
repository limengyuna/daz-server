package com.limengyuan.partner.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 */
@Service
@Transactional
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取用户详情（内部使用，包含完整信息，密码除外）
     */
    public Result<User> getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 不返回密码
        user.setPasswordHash(null);
        return Result.success(user);
    }

    /**
     * 获取用户公开信息（用于查看其他用户的资料，不包含敏感字段）
     */
    public Result<UserProfileVO> getUserProfileById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserProfileVO profile = UserProfileVO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .city(user.getCity())
                .bio(user.getBio())
                .tags(user.getTags())
                .creditScore(user.getCreditScore())
                .createdAt(user.getCreatedAt())
                .build();
        return Result.success(profile);
    }

    /**
     * 获取用户列表
     */
    public Result<List<User>> getAllUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        List<User> users = userMapper.selectList(wrapper);
        // 不返回密码
        users.forEach(user -> user.setPasswordHash(null));
        return Result.success(users);
    }

    /**
     * 更新用户信息
     */
    public Result<User> updateUser(Long userId, User updateData) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        // 设置要更新的字段
        updateData.setUserId(userId);

        // 执行更新（使用自定义的 updateUserInfo 方法，只更新指定字段）
        int rows = userMapper.updateUserInfo(updateData);
        if (rows > 0) {
            // 返回更新后的用户信息
            User updated = userMapper.selectById(userId);
            if (updated != null) {
                updated.setPasswordHash(null);
                return Result.success("更新成功", updated);
            }
            return Result.error("更新后查询失败");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 删除用户
     */
    public Result<Void> deleteUser(Long userId) {
        // 检查用户是否存在
        if (userMapper.selectById(userId) == null) {
            return Result.error("用户不存在");
        }

        int rows = userMapper.deleteById(userId);
        if (rows > 0) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
