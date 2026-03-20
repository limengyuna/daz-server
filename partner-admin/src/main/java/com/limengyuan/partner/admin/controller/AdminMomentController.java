package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminAuthService;
import com.limengyuan.partner.admin.service.AdminMomentService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 动态管理控制器
 */
@RestController
@RequestMapping("/api/admin/moments")
public class AdminMomentController {

    private final AdminMomentService adminMomentService;
    private final AdminAuthService adminAuthService;

    public AdminMomentController(AdminMomentService adminMomentService,
                                  AdminAuthService adminAuthService) {
        this.adminMomentService = adminMomentService;
        this.adminAuthService = adminAuthService;
    }

    /**
     * 获取动态列表（分页 + 搜索 + 状态筛选）
     * GET /api/admin/moments?page=0&size=10&keyword=xxx&status=1
     */
    @GetMapping
    public Result<PageResult<MomentVO>> getMomentList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminMomentService.getMomentList(keyword, status, page, size);
    }

    /**
     * 屏蔽动态
     * PUT /api/admin/moments/{momentId}/block
     */
    @PutMapping("/{momentId}/block")
    public Result<Void> blockMoment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("momentId") Long momentId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminMomentService.blockMoment(momentId);
    }

    /**
     * 恢复动态
     * PUT /api/admin/moments/{momentId}/restore
     */
    @PutMapping("/{momentId}/restore")
    public Result<Void> restoreMoment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("momentId") Long momentId) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminMomentService.restoreMoment(momentId);
    }
}
