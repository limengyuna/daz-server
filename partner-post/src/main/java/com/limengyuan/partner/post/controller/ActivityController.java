package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.ActivityVO;
import com.limengyuan.partner.common.dto.CreateActivityRequest;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.post.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器 - 活动发布相关接口
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * 发布活动帖子
     * POST /api/activities
     */
    @PostMapping
    public Result<Activity> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return activityService.createActivity(request);
    }

    /**
     * 获取活动详情
     * GET /api/activities/{id}
     */
    @GetMapping("/{id}")
    public Result<ActivityVO> getActivity(@PathVariable("id") Long activityId) {
        return activityService.getActivity(activityId);
    }

    /**
     * 获取当前登录用户发布的活动列表
     * GET /api/activities/my
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/my")
    public Result<List<ActivityVO>> getMyActivities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未登录或 Token 无效");
        }

        // 从 Token 解析用户 ID
        Long userId = JwtUtils.getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error("Token 无效或已过期");
        }

        return activityService.getActivitiesByUser(userId);
    }

    /**
     * 根据用户ID获取该用户发布的活动列表（用于查看其他用户的活动帖子）
     * GET /api/activities/user/{userId}
     *
     * @param userId 目标用户ID
     */
    @GetMapping("/user/{userId}")
    public Result<List<ActivityVO>> getActivitiesByUserId(@PathVariable("userId") Long userId) {
        return activityService.getActivitiesByUser(userId);
    }

    /**
     * 获取所有活动列表 (分页，支持按分类筛选)
     * GET /api/activities?page=0&size=5&categoryId=1
     * 
     * @param page       页码，从0开始，默认0
     * @param size       每页数量，默认5
     * @param categoryId 分类ID，可选，不传则查询所有分类
     */
    @GetMapping
    public Result<PageResult<ActivityVO>> getAllActivities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        return activityService.getAllActivities(page, size, categoryId);
    }
}
