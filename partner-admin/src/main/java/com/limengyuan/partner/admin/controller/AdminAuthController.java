package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminAuthService;
import com.limengyuan.partner.common.dto.request.AdminLoginRequest;
import com.limengyuan.partner.common.dto.response.AdminLoginResponse;
import com.limengyuan.partner.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器 - 登录相关接口
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 管理员登录
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }
}
