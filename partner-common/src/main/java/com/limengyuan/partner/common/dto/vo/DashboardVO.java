package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台仪表盘数据统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /**
     * 用户总数
     */
    private Long totalUsers;

    /**
     * 活动总数
     */
    private Long totalActivities;

    /**
     * 动态总数
     */
    private Long totalMoments;

    /**
     * 今日新增用户数
     */
    private Long todayNewUsers;

    /**
     * 今日新增活动数
     */
    private Long todayNewActivities;

    /**
     * 今日新增动态数
     */
    private Long todayNewMoments;

    /**
     * 招募中的活动数
     */
    private Long recruitingActivities;

    /**
     * 被封禁的用户数
     */
    private Long bannedUsers;
}
