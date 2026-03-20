package com.limengyuan.partner.admin.controller;

import com.limengyuan.partner.admin.service.AdminAuthService;
import com.limengyuan.partner.admin.service.AdminDashboardService;
import com.limengyuan.partner.common.dto.vo.DashboardVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 数据统计控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;
    private final AdminAuthService adminAuthService;

    public AdminDashboardController(AdminDashboardService dashboardService,
                                     AdminAuthService adminAuthService) {
        this.dashboardService = dashboardService;
        this.adminAuthService = adminAuthService;
    }

    /**
     * 获取仪表盘统计数据
     * GET /api/admin/dashboard
     */
    @GetMapping
    public Result<DashboardVO> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (adminAuthService.getAdminIdFromToken(authHeader) == null) {
            return Result.error(401, "未登录或无管理员权限");
        }

        return dashboardService.getDashboardData();
    }
}
