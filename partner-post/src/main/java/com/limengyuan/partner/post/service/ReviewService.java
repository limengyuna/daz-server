package com.limengyuan.partner.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.request.SubmitReviewRequest;
import com.limengyuan.partner.common.dto.request.UpdateCreditRequest;
import com.limengyuan.partner.common.dto.vo.ReviewVO;
import com.limengyuan.partner.common.dto.vo.UserReviewPageVO;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.entity.Participant;
import com.limengyuan.partner.common.entity.Review;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.feign.UserServiceClient;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import com.limengyuan.partner.post.mapper.ReviewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价服务层 - 封装评价相关业务逻辑
 */
@Slf4j
@Service
@Transactional
public class ReviewService {

    /** 评价窗口期（天） */
    private static final int REVIEW_WINDOW_DAYS = 7;
    /** 活动状态：已结束 */
    private static final int ACTIVITY_STATUS_ENDED = 2;
    /** 参与者状态：已通过 */
    private static final int PARTICIPANT_STATUS_APPROVED = 1;

    private final ReviewMapper reviewMapper;
    private final ActivityMapper activityMapper;
    private final ParticipantMapper participantMapper;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;

    public ReviewService(ReviewMapper reviewMapper, ActivityMapper activityMapper,
                         ParticipantMapper participantMapper, ObjectMapper objectMapper,
                         UserServiceClient userServiceClient) {
        this.reviewMapper = reviewMapper;
        this.activityMapper = activityMapper;
        this.participantMapper = participantMapper;
        this.objectMapper = objectMapper;
        this.userServiceClient = userServiceClient;
    }

    /**
     * 提交评价
     */
    public Result<Void> submitReview(Long reviewerId, SubmitReviewRequest request) {
        // 1. 参数校验
        if (request.getActivityId() == null || request.getRevieweeId() == null || request.getScore() == null) {
            return Result.error("缺少必要参数");
        }
        if (request.getScore() < 1 || request.getScore() > 5) {
            return Result.error("评分必须在1-5之间");
        }

        // 2. 不能评价自己
        if (reviewerId.equals(request.getRevieweeId())) {
            return Result.error("不能评价自己");
        }

        // 3. 查询活动信息
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 4. 检查活动是否已结束
        boolean isEnded = activity.getStatus() == ACTIVITY_STATUS_ENDED;
        boolean isExpired = activity.getEndTime() != null && activity.getEndTime().isBefore(LocalDateTime.now());
        if (!isEnded && !isExpired) {
            return Result.error("活动尚未结束，暂时无法评价");
        }

        // 5. 检查是否在评价窗口期内
        LocalDateTime actualEndTime;
        if (isEnded) {
            actualEndTime = activity.getUpdatedAt();
        } else {
            actualEndTime = activity.getEndTime();
        }
        if (actualEndTime != null && LocalDateTime.now().isAfter(actualEndTime.plusDays(REVIEW_WINDOW_DAYS))) {
            return Result.error("评价窗口已关闭（活动结束后" + REVIEW_WINDOW_DAYS + "天内可评价）");
        }

        // 6. 检查评价人是否是该活动的参与者或发起人
        boolean isReviewerParticipant = isActivityMember(request.getActivityId(), reviewerId, activity.getInitiatorId());
        if (!isReviewerParticipant) {
            return Result.error("只有活动参与者才能评价");
        }

        // 7. 检查被评价人是否是该活动的参与者或发起人
        boolean isRevieweeParticipant = isActivityMember(request.getActivityId(), request.getRevieweeId(), activity.getInitiatorId());
        if (!isRevieweeParticipant) {
            return Result.error("被评价人不是该活动的参与者");
        }

        // 8. 检查是否重复评价
        QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
        reviewWrapper.eq("activity_id", request.getActivityId())
                     .eq("reviewer_id", reviewerId)
                     .eq("reviewee_id", request.getRevieweeId());
        Review existing = reviewMapper.selectOne(reviewWrapper);
        if (existing != null) {
            return Result.error("您已经评价过该用户了");
        }

        // 9. 处理标签为JSON字符串
        String tagsJson = null;
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                tagsJson = objectMapper.writeValueAsString(request.getTags());
            } catch (JsonProcessingException e) {
                return Result.error("标签格式错误");
            }
        }

        // 10. 构建评价实体并保存（MP 自动回填 reviewId）
        Review review = Review.builder()
                .activityId(request.getActivityId())
                .reviewerId(reviewerId)
                .revieweeId(request.getRevieweeId())
                .score(request.getScore())
                .content(request.getContent())
                .tags(tagsJson)
                .build();

        int rows = reviewMapper.insert(review);
        if (rows == 0) {
            return Result.error("评价提交失败");
        }

        // 11. 通过 OpenFeign 调用 user 模块重新计算被评价人的信誉分
        try {
            UpdateCreditRequest creditRequest = UpdateCreditRequest.builder()
                    .userId(request.getRevieweeId())
                    .reason("活动评价（评分：" + request.getScore() + "星）")
                    .build();
            userServiceClient.recalculateCreditScore(creditRequest);
        } catch (Exception e) {
            // 信誉分更新失败不影响评价提交
            log.warn("[信誉分更新失败] revieweeId={}, error={}", request.getRevieweeId(), e.getMessage());
        }

        return Result.success("评价成功", null);
    }

    /**
     * 获取活动下的所有评价
     */
    public Result<List<ReviewVO>> getActivityReviews(Long activityId) {
        List<ReviewVO> reviews = reviewMapper.findByActivityId(activityId);
        return Result.success(reviews);
    }

    /**
     * 获取某用户收到的评价（分页 + 统计信息）
     */
    public Result<UserReviewPageVO> getUserReviews(Long userId, int page, int size) {
        int offset = page * size;
        List<ReviewVO> reviews = reviewMapper.findByRevieweeIdPaged(userId, offset, size);

        // 用 QueryWrapper 统计评价数量
        QueryWrapper<Review> countWrapper = new QueryWrapper<>();
        countWrapper.eq("reviewee_id", userId);
        long total = reviewMapper.selectCount(countWrapper);

        Double avgScore = reviewMapper.getAverageScoreByRevieweeId(userId);
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        UserReviewPageVO vo = UserReviewPageVO.builder()
                .list(reviews)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .averageScore(avgScore != null ? Math.round(avgScore * 10) / 10.0 : null)
                .reviewCount(total)
                .build();

        return Result.success(vo);
    }

    /**
     * 判断用户是否是活动的成员（参与者或发起人）
     */
    private boolean isActivityMember(Long activityId, Long userId, Long initiatorId) {
        // 是发起人
        if (userId.equals(initiatorId)) {
            return true;
        }
        // 是已通过的参与者
        QueryWrapper<Participant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId);
        Participant p = participantMapper.selectOne(wrapper);
        return p != null && p.getStatus() == PARTICIPANT_STATUS_APPROVED;
    }
}
