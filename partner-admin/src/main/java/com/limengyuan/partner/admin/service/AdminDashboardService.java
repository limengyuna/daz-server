package com.limengyuan.partner.admin.service;

import com.limengyuan.partner.admin.mapper.AdminDashboardMapper;
import com.limengyuan.partner.common.dto.vo.DashboardVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.stereotype.Service;

/**
 * 管理员端 - 数据统计服务
 */
@Service
public class AdminDashboardService {

    private final AdminDashboardMapper dashboardMapper;

    public AdminDashboardService(AdminDashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    /**
     * 获取仪表盘统计数据
     */
    public Result<DashboardVO> getDashboardData() {
        DashboardVO dashboard = DashboardVO.builder()
                .totalUsers(dashboardMapper.countTotalUsers())
                .totalActivities(dashboardMapper.countTotalActivities())
                .totalMoments(dashboardMapper.countTotalMoments())
                .todayNewUsers(dashboardMapper.countTodayNewUsers())
                .todayNewActivities(dashboardMapper.countTodayNewActivities())
                .todayNewMoments(dashboardMapper.countTodayNewMoments())
                .recruitingActivities(dashboardMapper.countRecruitingActivities())
                .bannedUsers(dashboardMapper.countBannedUsers())
                .build();

        return Result.success(dashboard);
    }
}
