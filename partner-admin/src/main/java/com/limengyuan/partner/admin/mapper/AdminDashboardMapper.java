package com.limengyuan.partner.admin.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 管理员端 - 数据统计 Mapper
 */
public interface AdminDashboardMapper {

    /**
     * 查询用户总数
     */
    @Select("SELECT COUNT(*) FROM users")
    long countTotalUsers();

    /**
     * 查询活动总数
     */
    @Select("SELECT COUNT(*) FROM activities")
    long countTotalActivities();

    /**
     * 查询动态总数
     */
    @Select("SELECT COUNT(*) FROM moments WHERE status != 0")
    long countTotalMoments();

    /**
     * 查询今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()")
    long countTodayNewUsers();

    /**
     * 查询今日新增活动数
     */
    @Select("SELECT COUNT(*) FROM activities WHERE DATE(created_at) = CURDATE()")
    long countTodayNewActivities();

    /**
     * 查询今日新增动态数
     */
    @Select("SELECT COUNT(*) FROM moments WHERE DATE(created_at) = CURDATE() AND status != 0")
    long countTodayNewMoments();

    /**
     * 查询招募中的活动数
     */
    @Select("SELECT COUNT(*) FROM activities WHERE status = 0")
    long countRecruitingActivities();

    /**
     * 查询被封禁的用户数
     */
    @Select("SELECT COUNT(*) FROM users WHERE status = 0")
    long countBannedUsers();
}
