package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.LoginRequest;
import com.limengyuan.partner.common.dto.LoginResponse;
import com.limengyuan.partner.common.dto.RegisterRequest;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * 认证控制器 - 登录注册
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        // 1. 检查用户名是否已存在
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, request.getUsername());
        if (count != null && count > 0) {
            return Result.error("用户名已存在");
        }

        // 2. 密码加密
        String passwordHash = hashPassword(request.getPassword());

        // 3. 插入用户
        String insertSql = """
                INSERT INTO users (username, password_hash, nickname, gender, city, credit_score, status)
                VALUES (?, ?, ?, ?, ?, 100, 1)
                """;
        jdbcTemplate.update(insertSql,
                request.getUsername(),
                passwordHash,
                request.getNickname() != null ? request.getNickname() : request.getUsername(),
                request.getGender() != null ? request.getGender() : 0,
                request.getCity());

        // 4. 查询新创建的用户
        String querySql = "SELECT * FROM users WHERE username = ?";
        User user = jdbcTemplate.queryForObject(querySql, new BeanPropertyRowMapper<>(User.class),
                request.getUsername());

        // 清除密码返回
        if (user != null) {
            user.setPasswordHash(null);
        }

        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 查询用户
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), request.getUsername());

        if (users.isEmpty()) {
            return Result.error("用户名或密码错误");
        }

        User user = users.get(0);

        // 2. 验证密码
        String passwordHash = hashPassword(request.getPassword());
        if (!passwordHash.equals(user.getPasswordHash())) {
            return Result.error("用户名或密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() == 0) {
            return Result.error("账号已被封禁");
        }

        // 4. 生成 Token (简化版，后续可换成 JWT)
        String token = UUID.randomUUID().toString().replace("-", "");

        // 5. 返回登录信息
        LoginResponse response = LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();

        return Result.success("登录成功", response);
    }

    /**
     * 检查用户名是否可用
     * GET /api/auth/check-username?username=xxx
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        boolean available = count == null || count == 0;
        return Result.success(available ? "用户名可用" : "用户名已被占用", available);
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
