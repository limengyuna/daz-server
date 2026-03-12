package com.limengyuan.partner.post.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.request.CreateActivityRequest;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.post.service.ActivityService;
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
    @SentinelResource(value = "getActivity", blockHandler = "getActivityBlockHandler")
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
    @SentinelResource(value = "listActivities", blockHandler = "listActivitiesBlockHandler")
    public Result<PageResult<ActivityVO>> getAllActivities(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        return activityService.getAllActivities(page, size, categoryId);
    }

    // ==================== Sentinel 降级处理方法 ====================

    /**
     * 获取活动详情 - 限流降级处理
     */
    public Result<ActivityVO> getActivityBlockHandler(Long activityId, BlockException ex) {
        log.warn("[Sentinel] 获取活动详情接口被限流/降级, activityId={}", activityId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取活动列表 - 限流降级处理
     */
    public Result<PageResult<ActivityVO>> listActivitiesBlockHandler(
            int page, int size, Integer categoryId, BlockException ex) {
        log.warn("[Sentinel] 获取活动列表接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
