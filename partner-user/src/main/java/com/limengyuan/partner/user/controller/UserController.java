package com.limengyuan.partner.user.controller;

import com.limengyuan.partner.common.dto.UserMeResponse;
import com.limengyuan.partner.common.dto.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.user.service.UserService;
import com.limengyuan.partner.user.service.UserFollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器 - 用户信息相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserFollowService userFollowService;

    public UserController(UserService userService, UserFollowService userFollowService) {
        this.userService = userService;
        this.userFollowService = userFollowService;
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

        // 获取关注统计信息并设置到用户对象中
        Result<Map<String, Integer>> statsResult = userFollowService.getFollowStats(userId);
        Map<String, Integer> stats = statsResult.getData();
        User user = userResult.getData();
        user.setFollowingCount(stats.get("followingCount"));
        user.setFollowersCount(stats.get("followersCount"));

        // 构建响应
        UserMeResponse response = UserMeResponse.builder()
                .user(user)
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

    // ==================== 关注相关接口 ====================

    /**
     * 关注用户
     * POST /api/user/follow/{followeeId}
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @PostMapping("/follow/{followeeId}")
    public Result<Void> followUser(
            @PathVariable("followeeId") Long followeeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 获取当前登录用户ID
        Long currentUserId = extractUserId(authHeader, cookieToken);
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return userFollowService.followUser(currentUserId, followeeId);
    }

    /**
     * 取消关注
     * DELETE /api/user/follow/{followeeId}
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @DeleteMapping("/follow/{followeeId}")
    public Result<Void> unfollowUser(
            @PathVariable("followeeId") Long followeeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 获取当前登录用户ID
        Long currentUserId = extractUserId(authHeader, cookieToken);
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return userFollowService.unfollowUser(currentUserId, followeeId);
    }

    /**
     * 检查是否关注某用户
     * GET /api/user/follow/check/{followeeId}
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/follow/check/{followeeId}")
    public Result<Boolean> checkFollowing(
            @PathVariable("followeeId") Long followeeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 获取当前登录用户ID
        Long currentUserId = extractUserId(authHeader, cookieToken);
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return userFollowService.checkFollowing(currentUserId, followeeId);
    }

    /**
     * 获取关注列表（我关注的人）
     * GET /api/user/following
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/following")
    public Result<List<User>> getFollowingList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 获取当前登录用户ID
        Long currentUserId = extractUserId(authHeader, cookieToken);
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return userFollowService.getFollowingList(currentUserId);
    }

    /**
     * 获取粉丝列表（关注我的人）
     * GET /api/user/followers
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/followers")
    public Result<List<User>> getFollowersList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "token", required = false) String cookieToken) {

        // 获取当前登录用户ID
        Long currentUserId = extractUserId(authHeader, cookieToken);
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return userFollowService.getFollowersList(currentUserId);
    }

    /**
     * 获取指定用户的关注列表
     * GET /api/user/{userId}/following
     */
    @GetMapping("/{userId}/following")
    public Result<List<User>> getUserFollowingList(@PathVariable("userId") Long userId) {
        return userFollowService.getFollowingList(userId);
    }

    /**
     * 获取指定用户的粉丝列表
     * GET /api/user/{userId}/followers
     */
    @GetMapping("/{userId}/followers")
    public Result<List<User>> getUserFollowersList(@PathVariable("userId") Long userId) {
        return userFollowService.getFollowersList(userId);
    }

    /**
     * 获取关注统计信息
     * GET /api/user/{userId}/follow-stats
     */
    @GetMapping("/{userId}/follow-stats")
    public Result<Map<String, Integer>> getFollowStats(@PathVariable("userId") Long userId) {
        return userFollowService.getFollowStats(userId);
    }

    // ==================== 辅助方法 ====================

    /**
     * 从请求头或Cookie中提取用户ID
     */
    private Long extractUserId(String authHeader, String cookieToken) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader;
        } else if (cookieToken != null) {
            token = cookieToken;
        }

        if (token == null) {
            return null;
        }

        return JwtUtils.getUserIdFromToken(token);
    }
}
