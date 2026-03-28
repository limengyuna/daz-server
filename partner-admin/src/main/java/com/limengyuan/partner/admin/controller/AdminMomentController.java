package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminMomentService;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 动态管理控制器
 * 鉴权已由网关统一完成，通过 UserContextHolder 获取管理员身份
 */
@RestController
@RequestMapping("/api/admin/moments")
public class AdminMomentController {

    private final AdminMomentService adminMomentService;

    public AdminMomentController(AdminMomentService adminMomentService) {
        this.adminMomentService = adminMomentService;
    }

    /**
     * 获取动态列表（分页 + 搜索 + 状态筛选 + 用户筛选）
     * GET /api/admin/moments?page=0&size=10&keyword=xxx&status=1&userId=1
     */
    @GetMapping
    public Result<PageResult<MomentVO>> getMomentList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "userId", required = false) Long userId) {

        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return adminMomentService.getMomentList(keyword, status, userId, page, size);
    }

    /**
     * 获取动态详情
     * GET /api/admin/moments/{momentId}
     */
    @GetMapping("/{momentId}")
    public Result<MomentVO> getMomentDetail(@PathVariable("momentId") Long momentId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminMomentService.getMomentDetail(momentId);
    }

    /**
     * 屏蔽动态
     * PUT /api/admin/moments/{momentId}/block
     */
    @PutMapping("/{momentId}/block")
    public Result<Void> blockMoment(@PathVariable("momentId") Long momentId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminMomentService.blockMoment(momentId);
    }

    /**
     * 恢复动态
     * PUT /api/admin/moments/{momentId}/restore
     */
    @PutMapping("/{momentId}/restore")
    public Result<Void> restoreMoment(@PathVariable("momentId") Long momentId) {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return adminMomentService.restoreMoment(momentId);
    }
}
