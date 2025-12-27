package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * 获取用户详情
     */
    public Result<User> getUserById(Long userId) {
        return userMapper.findById(userId)
                .map(user -> {
                    // 不返回密码
                    user.setPasswordHash(null);
                    return Result.success(user);
                })
                .orElse(Result.error("用户不存在"));
    }

    /**
     * 获取用户列表
     */
    public Result<List<User>> getAllUsers() {
        List<User> users = userMapper.findAll();
        // 不返回密码
        users.forEach(user -> user.setPasswordHash(null));
        return Result.success(users);
    }

    /**
     * 更新用户信息
     */
    public Result<User> updateUser(Long userId, User updateData) {
        // 检查用户是否存在
        return userMapper.findById(userId)
                .map(existingUser -> {
                    // 设置要更新的字段
                    updateData.setUserId(userId);

                    // 执行更新
                    int rows = userMapper.update(updateData);
                    if (rows > 0) {
                        // 返回更新后的用户信息
                        return userMapper.findById(userId)
                                .map(updated -> {
                                    updated.setPasswordHash(null);
                                    return Result.success("更新成功", updated);
                                })
                                .orElse(Result.error("更新后查询失败"));
                    } else {
                        return Result.<User>error("更新失败");
                    }
                })
                .orElse(Result.error("用户不存在"));
    }

    /**
     * 删除用户
     */
    public Result<Void> deleteUser(Long userId) {
        // 检查用户是否存在
        if (userMapper.findById(userId).isEmpty()) {
            return Result.error("用户不存在");
        }

        int rows = userMapper.deleteById(userId);
        if (rows > 0) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
