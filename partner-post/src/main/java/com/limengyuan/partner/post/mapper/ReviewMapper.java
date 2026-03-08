package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.ReviewVO;
import com.limengyuan.partner.common.entity.Review;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * 评价数据访问层 - 封装所有评价相关数据库操作
 */
@Repository
public class ReviewMapper {

    private final JdbcTemplate jdbcTemplate;

    public ReviewMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 插入评价并返回生成的ID
     */
    public Long insert(Review review) {
        String sql = """
                INSERT INTO reviews (activity_id, reviewer_id, reviewee_id, score, content, tags)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getActivityId());
            ps.setLong(2, review.getReviewerId());
            ps.setLong(3, review.getRevieweeId());
            ps.setInt(4, review.getScore());
            ps.setString(5, review.getContent());
            ps.setString(6, review.getTags());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 查询某个活动中，评价人对被评价人的评价记录（用于检查是否重复评价）
     */
    public Optional<Review> findByActivityAndReviewerAndReviewee(Long activityId, Long reviewerId, Long revieweeId) {
        String sql = "SELECT * FROM reviews WHERE activity_id = ? AND reviewer_id = ? AND reviewee_id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(Review.class), activityId, reviewerId, revieweeId);
            return Optional.ofNullable(review);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 查询某个活动下的所有评价（带评价人和被评价人信息）
     */
    public List<ReviewVO> findByActivityId(Long activityId) {
        String sql = """
                SELECT r.*,
                       a.title AS activity_title,
                       reviewer.nickname AS reviewer_nickname,
                       reviewer.avatar_url AS reviewer_avatar,
                       reviewee.nickname AS reviewee_nickname,
                       reviewee.avatar_url AS reviewee_avatar
                FROM reviews r
                LEFT JOIN activities a ON r.activity_id = a.activity_id
                LEFT JOIN users reviewer ON r.reviewer_id = reviewer.user_id
                LEFT JOIN users reviewee ON r.reviewee_id = reviewee.user_id
                WHERE r.activity_id = ?
                ORDER BY r.created_at DESC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ReviewVO.class), activityId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 分页查询某用户收到的评价（带评价人信息和活动标题）
     *
     * @param revieweeId 被评价人ID
     * @param offset     偏移量
     * @param limit      每页数量
     */
    public List<ReviewVO> findByRevieweeIdPaged(Long revieweeId, int offset, int limit) {
        String sql = """
                SELECT r.*,
                       a.title AS activity_title,
                       reviewer.nickname AS reviewer_nickname,
                       reviewer.avatar_url AS reviewer_avatar,
                       reviewee.nickname AS reviewee_nickname,
                       reviewee.avatar_url AS reviewee_avatar
                FROM reviews r
                LEFT JOIN activities a ON r.activity_id = a.activity_id
                LEFT JOIN users reviewer ON r.reviewer_id = reviewer.user_id
                LEFT JOIN users reviewee ON r.reviewee_id = reviewee.user_id
                WHERE r.reviewee_id = ?
                ORDER BY r.created_at DESC
                LIMIT ? OFFSET ?
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ReviewVO.class), revieweeId, limit, offset);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 统计某用户收到的评价总数
     */
    public long countByRevieweeId(Long revieweeId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE reviewee_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, revieweeId);
        return count != null ? count : 0;
    }

    /**
     * 查询某用户的平均评分
     *
     * @return 平均评分，无评价时返回 null
     */
    public Double getAverageScoreByRevieweeId(Long revieweeId) {
        String sql = "SELECT AVG(score) FROM reviews WHERE reviewee_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, revieweeId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 更新被评价人的信用分
     * 信用分最低为0，不会变成负数
     *
     * @param userId 用户ID
     * @param delta  信用分变化量（可为负）
     */
    public void updateCreditScore(Long userId, int delta) {
        String sql = "UPDATE users SET credit_score = GREATEST(0, credit_score + ?) WHERE user_id = ?";
        jdbcTemplate.update(sql, delta, userId);
    }
}
