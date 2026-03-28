package com.limengyuan.partner.post.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.vo.RecommendedActivityVO;
import com.limengyuan.partner.common.dto.vo.TravelMemoryVO;
import com.limengyuan.partner.common.dto.request.CreateActivityRequest;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import com.limengyuan.partner.post.service.ActivityRecommendService;
import com.limengyuan.partner.post.service.ActivityService;
import com.limengyuan.partner.post.service.TravelMemoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器 - 活动发布相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityRecommendService recommendService;
    private final TravelMemoryService travelMemoryService;

    public ActivityController(ActivityService activityService,
                              ActivityRecommendService recommendService,
                              TravelMemoryService travelMemoryService) {
        this.activityService = activityService;
        this.recommendService = recommendService;
        this.travelMemoryService = travelMemoryService;
    }

    /**
     * 发布活动帖子
     * POST /api/activities
     */
    @PostMapping
    public Result<Activity> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return activityService.createActivity(userId, request);
    }

    /**
     * 获取活动详情
     * GET /api/activities/{id}
     */
    @GetMapping("/{id:\\d+}")
    @SentinelResource(value = "getActivity", blockHandler = "getActivityBlockHandler")
    public Result<ActivityVO> getActivity(@PathVariable("id") Long activityId) {
        return activityService.getActivity(activityId);
    }

    /**
     * AI 个性化推荐活动
     * GET /api/activities/recommend
     */
    @GetMapping("/recommend")
    public Result<List<RecommendedActivityVO>> recommendActivities() {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return recommendService.getRecommendations(userId);
    }

    /**
     * 获取当前登录用户发布的活动列表
     * GET /api/activities/my
     */
    @GetMapping("/my")
    public Result<List<ActivityVO>> getMyActivities() {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return activityService.getActivitiesByUser(userId);
    }

    /**
     * 根据用户ID获取该用户发布的活动列表（用于查看其他用户的活动帖子）
     * GET /api/activities/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<List<ActivityVO>> getActivitiesByUserId(@PathVariable("userId") Long userId) {
        return activityService.getActivitiesByUser(userId);
    }

    /**
     * 获取所有活动列表 (分页，支持按分类筛选)
     * GET /api/activities?page=0&size=5&categoryId=1
     */
    @GetMapping
    @SentinelResource(value = "listActivities", blockHandler = "listActivitiesBlockHandler")
    public Result<PageResult<ActivityVO>> getAllActivities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        return activityService.getAllActivities(page, size, categoryId);
    }
    
    // ==================== AI 旅行回忆 ====================

    /**
     * AI 生成旅行回忆视频脚本
     * GET /api/activities/{id}/travel-memory
     */
    @GetMapping("/{id:\\d+}/travel-memory")
    public Result<TravelMemoryVO> generateTravelMemory(@PathVariable("id") Long activityId) {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }
        return travelMemoryService.generateTravelMemory(activityId, userId);
    }

    // ==================== Sentinel 降级处理方法 ====================

    public Result<ActivityVO> getActivityBlockHandler(Long activityId, BlockException ex) {
        log.warn("[Sentinel] 获取活动详情接口被限流/降级, activityId={}", activityId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    public Result<PageResult<ActivityVO>> listActivitiesBlockHandler(
            int page, int size, Integer categoryId, BlockException ex) {
        log.warn("[Sentinel] 获取活动列表接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
