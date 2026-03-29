package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.entity.Review;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.ReviewQueryMapper;
import com.limengyuan.partner.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 信誉分计算服务
 *
 * 算法说明（加权衰减算法）：
 * ─────────────────────────────────
 * 1. 基础分 = 70
 * 2. 取用户所有评价，按时间衰减计算加权平均分
 *    - 时间衰减权重 = 1 / (1 + 天数差 / 30)
 *    - 30天前的评价权重约 0.5，90天前约 0.25
 * 3. 加权平均分映射为加成值：
 *    - 加成 = (加权平均分 - 3.0) / 2.0 × 30
 *    - 平均分 5.0 → +30 → 总分 100
 *    - 平均分 3.0 → +0  → 总分 70
 *    - 平均分 1.0 → -30 → 总分 40
 * 4. 最终信誉分 = clamp(基础分 + 加成, 0, 100)
 * ─────────────────────────────────
 */
@Slf4j
@Service
@Transactional
public class CreditScoreService {

    /** 基础信誉分 */
    private static final int BASE_SCORE = 70;
    /** 信誉分上限 */
    private static final int MAX_SCORE = 100;
    /** 信誉分下限 */
    private static final int MIN_SCORE = 0;
    /** 评价加成最大值（正负） */
    private static final double MAX_BONUS = 30.0;
    /** 时间衰减半衰期（天）：30天后权重衰减到约 0.5 */
    private static final double HALF_LIFE_DAYS = 30.0;

    private final ReviewQueryMapper reviewQueryMapper;
    private final UserMapper userMapper;

    public CreditScoreService(ReviewQueryMapper reviewQueryMapper, UserMapper userMapper) {
        this.reviewQueryMapper = reviewQueryMapper;
        this.userMapper = userMapper;
    }

    /**
     * 重新计算指定用户的信誉分
     *
     * @param userId 被评价的用户ID
     * @param reason 触发原因（用于日志记录）
     * @return 计算结果
     */
    public Result<Integer> recalculate(Long userId, String reason) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. 查询该用户收到的所有评价
        List<Review> reviews = reviewQueryMapper.findAllByRevieweeId(userId);

        // 3. 计算信誉分
        int newScore;
        if (reviews == null || reviews.isEmpty()) {
            // 没有评价，保持基础分
            newScore = BASE_SCORE;
        } else {
            newScore = calculateScore(reviews);
        }

        // 4. 更新数据库
        int oldScore = user.getCreditScore() != null ? user.getCreditScore() : BASE_SCORE;
        userMapper.updateCreditScore(userId, newScore);

        log.info("[信誉分更新] userId={}, 旧分={}, 新分={}, 原因={}, 评价数={}",
                userId, oldScore, newScore, reason, reviews != null ? reviews.size() : 0);

        return Result.success("信誉分更新成功", newScore);
    }

    /**
     * 根据评价列表计算信誉分（加权衰减算法）
     */
    private int calculateScore(List<Review> reviews) {
        LocalDateTime now = LocalDateTime.now();
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Review review : reviews) {
            // 计算评价距今的天数
            long daysDiff = ChronoUnit.DAYS.between(review.getCreatedAt(), now);
            if (daysDiff < 0) {
                daysDiff = 0;
            }

            // 时间衰减权重：越近的评价权重越高
            double weight = 1.0 / (1.0 + daysDiff / HALF_LIFE_DAYS);

            weightedSum += review.getScore() * weight;
            totalWeight += weight;
        }

        // 计算加权平均分
        double weightedAvg = totalWeight > 0 ? weightedSum / totalWeight : 3.0;

        // 映射为信誉分加成（范围 -30 ~ +30）
        double bonus = (weightedAvg - 3.0) / 2.0 * MAX_BONUS;

        // 计算最终信誉分并限制范围
        int finalScore = (int) Math.round(BASE_SCORE + bonus);
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, finalScore));
    }
}
