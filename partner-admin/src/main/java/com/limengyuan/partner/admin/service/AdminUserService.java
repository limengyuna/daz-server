package com.limengyuan.partner.admin.service;

import com.limengyuan.partner.admin.mapper.AdminUserMapper;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员端 - 用户管理服务
 */
@Service
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;

    public AdminUserService(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    /**
     * 分页查询用户列表
     *
     * @param keyword 搜索关键词（用户名/昵称）
     * @param page    页码，从0开始
     * @param size    每页数量
     */
    public Result<PageResult<User>> getUserList(String keyword, int page, int size) {
        int offset = page * size;
        List<User> users = adminUserMapper.findUsersPage(keyword, size, offset);
        long total = adminUserMapper.countUsers(keyword);

        // 清除密码字段
        users.forEach(u -> u.setPasswordHash(null));

        return Result.success(PageResult.of(users, total, page, size));
    }

    /**
     * 封禁用户
     */
    public Result<Void> banUser(Long userId) {
        int rows = adminUserMapper.updateUserStatus(userId, 0);
        if (rows == 0) {
            return Result.error("用户不存在");
        }
        return Result.success("已封禁该用户", null);
    }

    /**
     * 解封用户
     */
    public Result<Void> unbanUser(Long userId) {
        int rows = adminUserMapper.updateUserStatus(userId, 1);
        if (rows == 0) {
            return Result.error("用户不存在");
        }
        return Result.success("已解封该用户", null);
    }

    /**
     * 查看用户详情
     */
    public Result<User> getUserDetail(Long userId) {
        User user = adminUserMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPasswordHash(null);
        return Result.success(user);
    }
}
