package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminDashboardService;
import com.limengyuan.partner.common.dto.vo.DashboardVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 数据统计控制器
 * 鉴权已由网关统一完成，通过 UserContextHolder 获取管理员身份
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取仪表盘统计数据
     * GET /api/admin/dashboard
     */
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        Long adminId = UserContextHolder.getPrincipalId();
        if (adminId == null) {
            return Result.error(401, "未登录或无管理员权限");
        }
        return dashboardService.getDashboardData();
    }
}
