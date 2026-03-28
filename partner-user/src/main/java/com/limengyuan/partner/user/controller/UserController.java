package com.limengyuan.partner.user.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.request.UpdateUserRequest;
import com.limengyuan.partner.common.dto.response.UserMeResponse;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.PageHelper;
import com.limengyuan.partner.common.util.UserContextHolder;
import com.limengyuan.partner.user.service.UserService;
import com.limengyuan.partner.user.service.UserFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器 - 用户信息相关接口
 */
@Slf4j
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
     * 响应: { user: {...} }
     * (Token 刷新逻辑已迁至网关 AuthGlobalFilter)
     */
    @GetMapping("/me")
    @SentinelResource(value = "getUserMe", blockHandler = "getUserMeBlockHandler")
    public Result<UserMeResponse> getCurrentUser() {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
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

        return Result.success(response);
    }

    /**
     * 获取用户公开信息（不含敏感字段）
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    @SentinelResource(value = "getUserProfile", blockHandler = "getUserProfileBlockHandler")
    public Result<UserProfileVO> getUser(@PathVariable("id") Long userId) {
        return userService.getUserProfileById(userId);
    }

    /**
     * 获取用户列表（返回脱敏后的 VO）
     * GET /api/user/list
     */
    @GetMapping("/list")
    @SentinelResource(value = "listUsers", blockHandler = "listUsersBlockHandler")
    public Result<List<UserProfileVO>> listUsers() {
        return userService.getAllUsers();
    }

    /**
     * 更新用户信息（入参为 DTO，只允许修改安全字段）
     * PUT /api/user/{id}
     */
    @PutMapping("/{id}")
    public Result<UserProfileVO> updateUser(@PathVariable("id") Long userId, @RequestBody UpdateUserRequest request) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        // 防止同角色间的横向越权
        if (!currentUserId.equals(userId)) {
            return Result.error(403, "无权修改其他用户的资料");
        }
        return userService.updateUser(currentUserId, request);
    }

    /**
     * 删除用户
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long userId) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        // 防止同角色间的横向越权
        if (!currentUserId.equals(userId)) {
            return Result.error(403, "无权删除其他用户的账号");
        }
        return userService.deleteUser(currentUserId);
    }

    // ==================== 关注相关接口 ====================

    /**
     * 关注用户
     * POST /api/user/follow/{followeeId}
     */
    @PostMapping("/follow/{followeeId}")
    public Result<Void> followUser(@PathVariable("followeeId") Long followeeId) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return userFollowService.followUser(currentUserId, followeeId);
    }

    /**
     * 取消关注
     * DELETE /api/user/follow/{followeeId}
     */
    @DeleteMapping("/follow/{followeeId}")
    public Result<Void> unfollowUser(@PathVariable("followeeId") Long followeeId) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return userFollowService.unfollowUser(currentUserId, followeeId);
    }

    /**
     * 检查是否关注某用户
     * GET /api/user/follow/check/{followeeId}
     */
    @GetMapping("/follow/check/{followeeId}")
    public Result<Boolean> checkFollowing(@PathVariable("followeeId") Long followeeId) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return userFollowService.checkFollowing(currentUserId, followeeId);
    }

    /**
     * 获取关注列表（我关注的人）- 分页
     * GET /api/user/following?page=0&size=10
     */
    @GetMapping("/following")
    public Result<PageResult<UserProfileVO>> getFollowingList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        page = PageHelper.safePage(page);
        size = PageHelper.safeSize(size);

        return userFollowService.getFollowingList(currentUserId, page, size);
    }

    /**
     * 获取粉丝列表（关注我的人）- 分页
     * GET /api/user/followers?page=0&size=10
     */
    @GetMapping("/followers")
    public Result<PageResult<UserProfileVO>> getFollowersList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Long currentUserId = UserContextHolder.getPrincipalId();
        if (currentUserId == null) {
            return Result.error("未登录或 Token 无效");
        }

        page = PageHelper.safePage(page);
        size = PageHelper.safeSize(size);

        return userFollowService.getFollowersList(currentUserId, page, size);
    }

    /**
     * 获取指定用户的关注列表 - 分页
     * GET /api/user/{userId}/following?page=0&size=10
     */
    @GetMapping("/{userId}/following")
    public Result<PageResult<UserProfileVO>> getUserFollowingList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        page = PageHelper.safePage(page);
        size = PageHelper.safeSize(size);
        
        return userFollowService.getFollowingList(userId, page, size);
    }

    /**
     * 获取指定用户的粉丝列表 - 分页
     * GET /api/user/{userId}/followers?page=0&size=10
     */
    @GetMapping("/{userId}/followers")
    public Result<PageResult<UserProfileVO>> getUserFollowersList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        page = PageHelper.safePage(page);
        size = PageHelper.safeSize(size);
        
        return userFollowService.getFollowersList(userId, page, size);
    }

    /**
     * 获取关注统计信息
     * GET /api/user/{userId}/follow-stats
     */
    @GetMapping("/{userId}/follow-stats")
    public Result<Map<String, Integer>> getFollowStats(@PathVariable("userId") Long userId) {
        return userFollowService.getFollowStats(userId);
    }

    // ==================== Sentinel 降级处理方法 ====================

    /**
     * 获取当前用户信息 - 限流降级处理
     */
    public Result<UserMeResponse> getUserMeBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 获取当前用户信息接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取用户信息 - 限流降级处理
     */
    public Result<UserProfileVO> getUserProfileBlockHandler(
            Long userId, BlockException ex) {
        log.warn("[Sentinel] 获取用户信息接口被限流/降级, userId={}", userId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取用户列表 - 限流降级处理
     */
    public Result<List<UserProfileVO>> listUsersBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 获取用户列表接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
