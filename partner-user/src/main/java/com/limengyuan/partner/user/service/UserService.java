package com.limengyuan.partner.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.request.UpdateUserRequest;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 实名认证（上传认证图片，一旦认证不允许修改）
     */
    public Result<Void> verifyRealName(Long userId, String realNameImage) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 检查是否已经实名认证，已认证不允许修改
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            return Result.error("您已完成实名认证，无法重复认证");
        }

        // 校验图片链接不能为空
        if (realNameImage == null || realNameImage.trim().isEmpty()) {
            return Result.error("请上传实名认证图片");
        }

        // 执行实名认证更新
        int rows = userMapper.updateRealNameVerification(userId, realNameImage);
        if (rows > 0) {
            return Result.success("实名认证成功", null);
        }
        return Result.error("实名认证失败，请稍后重试");
    }

    /**
     * 获取当前用户的实名认证信息
     */
    public Result<Map<String, Object>> getVerifyInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("isVerified", user.getIsVerified() != null ? user.getIsVerified() : 0);
        info.put("realNameImage", user.getRealNameImage());
        return Result.success(info);
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
                .isVerified(user.getIsVerified())
                .tags(user.getTags())
                .creditScore(user.getCreditScore())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
