package com.limengyuan.partner.admin.service;

import com.limengyuan.partner.admin.mapper.AdminActivityMapper;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员端 - 活动管理服务
 */
@Service
public class AdminActivityService {

    private final AdminActivityMapper adminActivityMapper;

    public AdminActivityService(AdminActivityMapper adminActivityMapper) {
        this.adminActivityMapper = adminActivityMapper;
    }

    /**
     * 分页查询活动列表
     *
     * @param keyword 搜索关键词（标题）
     * @param status  活动状态筛选（可为 null）
     * @param userId  用户ID筛选（可为 null）
     * @param page    页码，从0开始
     * @param size    每页数量
     */
    public Result<PageResult<ActivityVO>> getActivityList(String keyword, Integer status, Long userId, int page, int size) {
        int offset = page * size;
        List<ActivityVO> activities = adminActivityMapper.findActivitiesPage(keyword, status, userId, size, offset);
        long total = adminActivityMapper.countActivities(keyword, status, userId);
        return Result.success(PageResult.of(activities, total, page, size));
    }

    /**
     * 根据活动ID查询详情
     *
     * @param activityId 活动ID
     */
    public Result<ActivityVO> getActivityDetail(Long activityId) {
        ActivityVO activity = adminActivityMapper.findActivityById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    /**
     * 下架活动（设置状态为已取消）
     */
    public Result<Void> cancelActivity(Long activityId) {
        int rows = adminActivityMapper.updateActivityStatus(activityId, 3);
        if (rows == 0) {
            return Result.error("活动不存在");
        }
        return Result.success("已下架该活动", null);
    }

    /**
     * 恢复活动（设置状态为招募中）
     */
    public Result<Void> restoreActivity(Long activityId) {
        int rows = adminActivityMapper.updateActivityStatus(activityId, 0);
        if (rows == 0) {
            return Result.error("活动不存在");
        }
        return Result.success("已恢复该活动", null);
    }
}
