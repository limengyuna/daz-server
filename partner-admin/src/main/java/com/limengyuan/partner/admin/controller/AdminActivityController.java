package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminActivityService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 活动管理控制器
 * 鉴权已由网关统一完成，通过 UserContextHolder 获取管理员身份
 */
@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    public AdminActivityController(AdminActivityService adminActivityService) {
        this.adminActivityService = adminActivityService;
    }

    /**
     * 获取活动列表（分页 + 搜索 + 状态筛选 + 用户筛选）
     * GET /api/admin/activities?page=0&size=10&keyword=xxx&status=0&userId=1
     */
    @GetMapping
    public Result<PageResult<ActivityVO>> getActivityList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "userId", required = false) Long userId) {

        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminActivityService.getActivityList(keyword, status, userId, page, size);
    }

    /**
     * 获取活动详情
     * GET /api/admin/activities/{activityId}
     */
    @GetMapping("/{activityId}")
    public Result<ActivityVO> getActivityDetail(@PathVariable("activityId") Long activityId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminActivityService.getActivityDetail(activityId);
    }

    /**
     * 下架活动
     * PUT /api/admin/activities/{activityId}/cancel
     */
    @PutMapping("/{activityId}/cancel")
    public Result<Void> cancelActivity(@PathVariable("activityId") Long activityId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminActivityService.cancelActivity(activityId);
    }

    /**
     * 恢复活动
     * PUT /api/admin/activities/{activityId}/restore
     */
    @PutMapping("/{activityId}/restore")
    public Result<Void> restoreActivity(@PathVariable("activityId") Long activityId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminActivityService.restoreActivity(activityId);
    }
}
