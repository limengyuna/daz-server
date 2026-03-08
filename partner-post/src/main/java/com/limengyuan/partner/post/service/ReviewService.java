package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.vo.ReviewVO;
import com.limengyuan.partner.common.dto.vo.UserReviewPageVO;
import com.limengyuan.partner.common.dto.request.SubmitReviewRequest;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.entity.Review;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import com.limengyuan.partner.post.mapper.ReviewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 评价服务层 - 封装评价相关业务逻辑
 */
@Service
@Transactional
public class ReviewService {

    /** 评价窗口期（天） */
    private static final int REVIEW_WINDOW_DAYS = 7;

    /** 活动状态：已结束 */
    private static final int ACTIVITY_STATUS_ENDED = 2;

    /** 参与者状态：已通过 */
    private static final int PARTICIPANT_STATUS_APPROVED = 1;

    /** 信用分变化映射：评分 -> 信用分变化量 */
    private static final Map<Integer, Integer> CREDIT_SCORE_DELTA = Map.of(
            5, 2,   // 5星: +2分
            4, 1,   // 4星: +1分
            3, 0,   // 3星: 不变
            2, -1,  // 2星: -1分
            1, -2   // 1星: -2分
    );

    private final ReviewMapper reviewMapper;
    private final ActivityMapper activityMapper;
    private final ParticipantMapper participantMapper;
    private final ObjectMapper objectMapper;

    public ReviewService(ReviewMapper reviewMapper, ActivityMapper activityMapper,
                         ParticipantMapper participantMapper, ObjectMapper objectMapper) {
        this.reviewMapper = reviewMapper;
        this.activityMapper = activityMapper;
        this.participantMapper = participantMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 提交评价
     *
     * @param reviewerId 评价人ID（从Token中获取）
     * @param request    评价请求
     * @return 提交结果
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
        Optional<Activity> activityOpt = activityMapper.findById(request.getActivityId());
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }
        Activity activity = activityOpt.get();

        // 4. 检查活动是否已结束（status=2 或 end_time 已过期）
        boolean isEnded = activity.getStatus() == ACTIVITY_STATUS_ENDED;
        boolean isExpired = activity.getEndTime() != null && activity.getEndTime().isBefore(LocalDateTime.now());
        if (!isEnded && !isExpired) {
            return Result.error("活动尚未结束，暂时无法评价");
        }

        // 5. 检查是否在评价窗口期内（7天）
        // 以实际结束时间为准：如果 status=2，用 updatedAt；否则用 endTime
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
        Optional<Review> existing = reviewMapper.findByActivityAndReviewerAndReviewee(
                request.getActivityId(), reviewerId, request.getRevieweeId());
        if (existing.isPresent()) {
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

        // 10. 构建评价实体并保存
        Review review = Review.builder()
                .activityId(request.getActivityId())
                .reviewerId(reviewerId)
                .revieweeId(request.getRevieweeId())
                .score(request.getScore())
                .content(request.getContent())
                .tags(tagsJson)
                .build();

        Long reviewId = reviewMapper.insert(review);
        if (reviewId == null) {
            return Result.error("评价提交失败");
        }

        // 11. 更新被评价人的信用分
        int delta = CREDIT_SCORE_DELTA.getOrDefault(request.getScore(), 0);
        if (delta != 0) {
            reviewMapper.updateCreditScore(request.getRevieweeId(), delta);
        }

        return Result.success("评价成功", null);
    }

    /**
     * 获取活动下的所有评价
     *
     * @param activityId 活动ID
     * @return 评价列表
     */
    public Result<List<ReviewVO>> getActivityReviews(Long activityId) {
        List<ReviewVO> reviews = reviewMapper.findByActivityId(activityId);
        return Result.success(reviews);
    }

    /**
     * 获取某用户收到的评价（分页 + 统计信息）
     *
     * @param userId 用户ID
     * @param page   页码（从0开始）
     * @param size   每页数量
     * @return 分页评价列表 + 平均评分 + 评价总数
     */
    public Result<UserReviewPageVO> getUserReviews(Long userId, int page, int size) {
        int offset = page * size;
        List<ReviewVO> reviews = reviewMapper.findByRevieweeIdPaged(userId, offset, size);
        long total = reviewMapper.countByRevieweeId(userId);
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
     *
     * @param activityId  活动ID
     * @param userId      用户ID
     * @param initiatorId 活动发起人ID
     * @return 是否是活动成员
     */
    private boolean isActivityMember(Long activityId, Long userId, Long initiatorId) {
        // 是发起人
        if (userId.equals(initiatorId)) {
            return true;
        }
        // 是已通过的参与者
        return participantMapper.findByActivityIdAndUserId(activityId, userId)
                .map(p -> p.getStatus() == PARTICIPANT_STATUS_APPROVED)
                .orElse(false);
    }
}
