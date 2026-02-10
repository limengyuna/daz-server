package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.UserMeResponse;
import com.limengyuan.partner.common.dto.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器 - 用户信息相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/me
     * 
     * 请求头需携带: Authorization: Bearer {token}
     * 或 Cookie 中包含 token
     * 
     * 响应: { user: {...}, newToken: "..." }
     * newToken 仅当 Token 剩余有效期 < 2 天时返回，前端需更新本地存储
     */
    @GetMapping("/me")
    public Result<UserMeResponse> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 优先从 Authorization Header 获取，其次从 Cookie 获取
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader;
        } else if (cookieToken != null) {
            token = cookieToken;
        }

        if (token == null) {
            return Result.error("未登录或 Token 无效");
        }

        // 解析 Token 获取用户 ID
        Long userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("Token 无效或已过期");
        }

        // 获取用户信息
        Result<User> userResult = userService.getUserById(userId);
        if (userResult.getCode() != 200) {
            return Result.error(userResult.getMessage());
        }

        // 构建响应
        UserMeResponse response = UserMeResponse.builder()
                .user(userResult.getData())
                .build();

        // 检查是否需要刷新 Token（剩余有效期 < 2 天）
        if (JwtUtils.shouldRefreshToken(token)) {
            response.setNewToken(JwtUtils.generateToken(userId));
        }

        return Result.success(response);
    }

    /**
     * 获取用户公开信息（不含敏感字段）
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public Result<UserProfileVO> getUser(@PathVariable("id") Long userId) {
        return userService.getUserProfileById(userId);
    }

    /**
     * 获取用户列表
     * GET /api/user/list
     */
    @GetMapping("/list")
    public Result<List<User>> listUsers() {
        return userService.getAllUsers();
    }

    /**
     * 更新用户信息
     * PUT /api/user/{id}
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable("id") Long userId, @RequestBody User user) {
        return userService.updateUser(userId, user);
    }

    /**
     * 删除用户
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }
}
