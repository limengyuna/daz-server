package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.request.LoginRequest;
import com.limengyuan.partner.common.dto.response.LoginResponse;
import com.limengyuan.partner.common.dto.request.RegisterRequest;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 认证控制器 - 登录注册
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<UserProfileVO> register(@RequestBody RegisterRequest request) {
        // 1. 检查用户名是否已存在
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }

        // 2. 密码加密 (使用 BCrypt)
        String passwordHash = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // 3. 构建用户实体并插入（MP 自动回填 userId）
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .gender(request.getGender() != null ? request.getGender() : 0)
                .city(request.getCity())
                .creditScore(70)
                .status(1)
                .build();

        userMapper.insert(user);

        // 4. 返回脱敏后的 VO（不包含密码等敏感字段）
        UserProfileVO vo = UserProfileVO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .city(user.getCity())
                .creditScore(user.getCreditScore())
                .createdAt(user.getCreatedAt())
                .build();
        return Result.success("注册成功", vo);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 查询用户
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 2. 验证密码 (使用 BCrypt)
        if (user.getPasswordHash() == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            return Result.error("用户名或密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() == 0) {
            return Result.error("账号已被封禁");
        }

        // 4. 生成 JWT Token
        String token = JwtUtils.generateToken(user.getUserId());

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
        User user = userMapper.findByUsername(username);
        boolean available = (user == null);
        return Result.success(available ? "用户名可用" : "用户名已被占用", available);
    }


}
