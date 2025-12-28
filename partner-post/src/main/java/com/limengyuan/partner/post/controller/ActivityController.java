package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.CreateActivityRequest;
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
    public Result<Activity> getActivity(@PathVariable("id") Long activityId) {
        return activityService.getActivity(activityId);
    }

    /**
     * 获取当前登录用户发布的活动列表
     * GET /api/activities/my
     * 
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/my")
    public Result<List<Activity>> getMyActivities(
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
}
