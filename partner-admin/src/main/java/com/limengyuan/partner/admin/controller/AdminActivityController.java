package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminActivityService;
import com.limengyuan.partner.admin.service.AdminAuthService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 活动管理控制器
 */
@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;
    private final AdminAuthService adminAuthService;

    public AdminActivityController(AdminActivityService adminActivityService,
                                    AdminAuthService adminAuthService) {
        this.adminActivityService = adminActivityService;
        this.adminAuthService = adminAuthService;
    }

    /**
     * 获取活动列表（分页 + 搜索 + 状态筛选 + 用户筛选）
     * GET /api/admin/activities?page=0&size=10&keyword=xxx&status=0&userId=1
     */
    @GetMapping
    public Result<PageResult<ActivityVO>> getActivityList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "userId", required = false) Long userId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminActivityService.getActivityList(keyword, status, userId, page, size);
    }

    /**
     * 获取活动详情
     * GET /api/admin/activities/{activityId}
     */
    @GetMapping("/{activityId}")
    public Result<ActivityVO> getActivityDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("activityId") Long activityId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminActivityService.getActivityDetail(activityId);
    }

    /**
     * 下架活动
     * PUT /api/admin/activities/{activityId}/cancel
     */
    @PutMapping("/{activityId}/cancel")
    public Result<Void> cancelActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("activityId") Long activityId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminActivityService.cancelActivity(activityId);
    }

    /**
     * 恢复活动
     * PUT /api/admin/activities/{activityId}/restore
     */
    @PutMapping("/{activityId}/restore")
    public Result<Void> restoreActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("activityId") Long activityId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminActivityService.restoreActivity(activityId);
    }
}
