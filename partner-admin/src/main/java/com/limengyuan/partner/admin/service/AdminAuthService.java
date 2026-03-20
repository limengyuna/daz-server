package com.limengyuan.partner.admin.service;

import com.limengyuan.partner.admin.mapper.AdminMapper;
import com.limengyuan.partner.common.dto.request.AdminLoginRequest;
import com.limengyuan.partner.common.dto.response.AdminLoginResponse;
import com.limengyuan.partner.common.entity.Admin;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 管理员认证服务
 */
@Service
public class AdminAuthService {

    private final AdminMapper adminMapper;

    public AdminAuthService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /**
     * 管理员登录
     */
    public Result<AdminLoginResponse> login(AdminLoginRequest request) {
        // 1. 查询管理员
        Admin admin = adminMapper.findByUsername(request.getUsername());
        if (admin == null) {
            return Result.error("账号或密码错误");
        }

        // 2. 验证密码
        String passwordHash = hashPassword(request.getPassword());
        if (!passwordHash.equals(admin.getPasswordHash())) {
            return Result.error("账号或密码错误");
        }

        // 3. 检查状态
        if (admin.getStatus() == 0) {
            return Result.error("该管理员账号已被禁用");
        }

        // 4. 更新最后登录时间
        adminMapper.updateLastLoginTime(admin.getAdminId());

        // 5. 生成 JWT Token（使用 adminId 的负数形式，与普通用户区分）
        String token = JwtUtils.generateToken(-admin.getAdminId());

        // 6. 构建响应
        AdminLoginResponse response = AdminLoginResponse.builder()
                .adminId(admin.getAdminId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatarUrl(admin.getAvatarUrl())
                .role(admin.getRole())
                .token(token)
                .build();

        return Result.success("登录成功", response);
    }

    /**
     * 从 Token 中解析管理员ID
     * 管理员的 Token 中存储的是负数 ID，取绝对值还原
     *
     * @param authHeader Authorization 请求头
     * @return 管理员ID，无效返回 null
     */
    public Long getAdminIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        Long id = JwtUtils.getUserIdFromToken(authHeader);
        // 管理员 Token 中的 ID 是负数
        if (id == null || id >= 0) {
            return null;
        }
        return -id;
    }

    /**
     * 密码加密 (SHA-256)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
