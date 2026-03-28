package com.limengyuan.partner.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.request.UpdateUserRequest;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        return Result.success(toProfileVO(user));
    }

    /**
     * 获取用户列表（返回 VO，不暴露敏感字段）
     */
    public Result<List<UserProfileVO>> getAllUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        List<User> users = userMapper.selectList(wrapper);
        List<UserProfileVO> voList = users.stream()
                .map(this::toProfileVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 更新用户信息（入参为 DTO，只允许修改安全字段）
     */
    public Result<UserProfileVO> updateUser(Long userId, UpdateUserRequest request) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        // 构建更新实体，只映射 DTO 中允许的字段
        User updateData = User.builder()
                .userId(userId)
                .nickname(request.getNickname())
                .avatarUrl(request.getAvatarUrl())
                .gender(request.getGender())
                .birthday(request.getBirthday())
                .city(request.getCity())
                .bio(request.getBio())
                .tags(request.getTags())
                .build();

        // 执行更新（使用自定义的 updateUserInfo 方法，只更新指定字段）
        int rows = userMapper.updateUserInfo(updateData);
        if (rows > 0) {
            // 返回更新后的用户信息（VO 格式）
            User updated = userMapper.selectById(userId);
            if (updated != null) {
                return Result.success("更新成功", toProfileVO(updated));
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

    /**
     * User 实体转 UserProfileVO（脱敏）
     */
    public UserProfileVO toProfileVO(User user) {
        return UserProfileVO.builder()
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
    }
}
