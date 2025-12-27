package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.CreateActivityRequest;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}
