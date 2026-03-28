package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminUserService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 用户管理控制器
 * 鉴权已由网关统一完成，通过 UserContextHolder 获取管理员身份
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 获取用户列表（分页 + 搜索）
     * GET /api/admin/users?page=0&size=10&keyword=xxx
     */
    @GetMapping
    public Result<PageResult<User>> getUserList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminUserService.getUserList(keyword, page, size);
    }

    /**
     * 查看用户详情
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public Result<User> getUserDetail(@PathVariable("userId") Long userId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminUserService.getUserDetail(userId);
    }

    /**
     * 封禁用户
     * PUT /api/admin/users/{userId}/ban
     */
    @PutMapping("/{userId}/ban")
    public Result<Void> banUser(@PathVariable("userId") Long userId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminUserService.banUser(userId);
    }

    /**
     * 解封用户
     * PUT /api/admin/users/{userId}/unban
     */
    @PutMapping("/{userId}/unban")
    public Result<Void> unbanUser(@PathVariable("userId") Long userId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminUserService.unbanUser(userId);
    }
}
