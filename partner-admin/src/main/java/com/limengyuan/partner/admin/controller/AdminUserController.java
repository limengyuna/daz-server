package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminAuthService;
import com.limengyuan.partner.admin.service.AdminUserService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminAuthService adminAuthService;

    public AdminUserController(AdminUserService adminUserService,
                                AdminAuthService adminAuthService) {
        this.adminUserService = adminUserService;
        this.adminAuthService = adminAuthService;
    }

    /**
     * 获取用户列表（分页 + 搜索）
     * GET /api/admin/users?page=0&size=10&keyword=xxx
     */
    @GetMapping
    public Result<PageResult<User>> getUserList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {

        // 验证管理员身份
        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminUserService.getUserList(keyword, page, size);
    }

    /**
     * 查看用户详情
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public Result<User> getUserDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("userId") Long userId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminUserService.getUserDetail(userId);
    }

    /**
     * 封禁用户
     * PUT /api/admin/users/{userId}/ban
     */
    @PutMapping("/{userId}/ban")
    public Result<Void> banUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("userId") Long userId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminUserService.banUser(userId);
    }

    /**
     * 解封用户
     * PUT /api/admin/users/{userId}/unban
     */
    @PutMapping("/{userId}/unban")
    public Result<Void> unbanUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("userId") Long userId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminUserService.unbanUser(userId);
    }
}
