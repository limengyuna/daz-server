package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.CreateActivityRequest;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
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
    private final ObjectMapper objectMapper;

    public ActivityService(ActivityMapper activityMapper, ObjectMapper objectMapper) {
        this.activityMapper = activityMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建活动
     * 
     * @param request 创建活动请求
     * @return 创建结果
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

        // 2. 构建Activity实体
        Activity activity = Activity.builder()
                .initiatorId(request.getInitiatorId())
                .categoryId(request.getCategoryId())
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .images(imagesJson)
                .locationName(request.getLocationName().trim())
                .locationAddress(request.getLocationAddress() != null ? request.getLocationAddress().trim() : null)
                .startTime(request.getStartTime())
                .maxParticipants(
                        request.getMaxParticipants() != null ? request.getMaxParticipants() : DEFAULT_MAX_PARTICIPANTS)
                .paymentType(request.getPaymentType() != null ? request.getPaymentType() : DEFAULT_PAYMENT_TYPE)
                .status(STATUS_RECRUITING)
                .build();

        // 3. 插入数据库
        Long activityId = activityMapper.insert(activity);
        if (activityId == null) {
            return Result.error("创建活动失败");
        }

        // 4. 查询并返回新创建的活动
        return activityMapper.findById(activityId)
                .map(a -> Result.success("发布成功", a))
                .orElse(Result.error("活动创建成功但查询失败"));
    }

    /**
     * 获取活动详情
     * 
     * @param activityId 活动ID
     * @return 活动详情
     */
    public Result<Activity> getActivity(Long activityId) {
        return activityMapper.findById(activityId)
                .map(Result::success)
                .orElse(Result.error("活动不存在"));
    }

    /**
     * 获取用户发布的活动列表
     * 
     * @param userId 用户ID
     * @return 活动列表
     */
    public Result<List<Activity>> getActivitiesByUser(Long userId) {
        List<Activity> activities = activityMapper.findByInitiatorId(userId);
        return Result.success(activities);
    }
}
