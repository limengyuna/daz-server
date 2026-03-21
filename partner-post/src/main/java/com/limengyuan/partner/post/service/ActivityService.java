package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.request.CreateActivityRequest;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.ParticipantVO;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 活动服务层 - 封装业务逻辑
 */
@Service
@Transactional
public class ActivityService {

    /** 默认最大参与人数 */
    private static final int DEFAULT_MAX_PARTICIPANTS = 2;
    /** 默认费用方式: AA制 */
    private static final int DEFAULT_PAYMENT_TYPE = 1;
    /** 状态: 招募中 */
    private static final int STATUS_RECRUITING = 0;

    private final ActivityMapper activityMapper;
    private final ParticipantMapper participantMapper;
    private final ObjectMapper objectMapper;

    public ActivityService(ActivityMapper activityMapper, ParticipantMapper participantMapper,
            ObjectMapper objectMapper) {
        this.activityMapper = activityMapper;
        this.participantMapper = participantMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建活动
     */
    public Result<Activity> createActivity(CreateActivityRequest request) {
        // 1. 处理 images 为 JSON 字符串
        String imagesJson = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(request.getImages());
            } catch (JsonProcessingException e) {
                return Result.error("图片格式错误");
            }
        }

        // 1.1 处理 categoryIds 为 JSON 字符串
        String categoryIdsJson;
        try {
            categoryIdsJson = objectMapper.writeValueAsString(request.getCategoryIds());
        } catch (JsonProcessingException e) {
            return Result.error("分类格式错误");
        }

        // 2. 构建Activity实体
        Activity activity = Activity.builder()
                .initiatorId(request.getInitiatorId())
                .categoryIds(categoryIdsJson)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .images(imagesJson)
                .locationName(request.getLocationName().trim())
                .locationAddress(request.getLocationAddress() != null ? request.getLocationAddress().trim() : null)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .registrationEndTime(request.getRegistrationEndTime())
                .maxParticipants(
                        request.getMaxParticipants() != null ? request.getMaxParticipants() : DEFAULT_MAX_PARTICIPANTS)
                .paymentType(request.getPaymentType() != null ? request.getPaymentType() : DEFAULT_PAYMENT_TYPE)
                .budget(request.getBudget())
                .status(STATUS_RECRUITING)
                .build();

        // 3. 插入数据库（MP 自动回填 activityId 到 entity）
        int rows = activityMapper.insert(activity);
        if (rows == 0) {
            return Result.error("创建活动失败");
        }

        // 4. 查询并返回新创建的活动
        Activity created = activityMapper.selectById(activity.getActivityId());
        if (created != null) {
            return Result.success("发布成功", created);
        }
        return Result.error("活动创建成功但查询失败");
    }

    /**
     * 获取活动详情（包含已通过审核的参与者列表）
     */
    public Result<ActivityVO> getActivity(Long activityId) {
        ActivityVO activity = activityMapper.findByIdWithUser(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        // 查询已通过审核的参与者列表
        List<ParticipantVO> participants = participantMapper.findApprovedByActivityIdWithUser(activityId);
        activity.setParticipants(participants);
        return Result.success(activity);
    }

    /**
     * 获取用户发布的活动列表
     */
    public Result<List<ActivityVO>> getActivitiesByUser(Long userId) {
        List<ActivityVO> activities = activityMapper.findByInitiatorIdWithUser(userId);
        return Result.success(activities);
    }

    /**
     * 分页获取所有活动列表，支持按分类筛选
     */
    public Result<PageResult<ActivityVO>> getAllActivities(int page, int size, Integer categoryId) {
        int offset = page * size;
        List<ActivityVO> activities;
        long total;

        if (categoryId != null) {
            activities = activityMapper.findAllWithUserByCategory(categoryId, size, offset);
            total = activityMapper.countAllByCategory(categoryId);
        } else {
            activities = activityMapper.findAllWithUser(size, offset);
            total = activityMapper.countAll();
        }

        return Result.success(PageResult.of(activities, total, page, size));
    }
}
