package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ReviewVO;
import com.limengyuan.partner.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;

/**
 * 评价数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入评价（自动回填 reviewId）
 * - selectOne(wrapper)   → 条件查询单条（如检查重复评价）
 * - selectCount(wrapper) → 条件统计
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 查询某个活动下的所有评价（带评价人和被评价人信息）
     */
    @Select("""
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
            WHERE r.activity_id = #{activityId}
            ORDER BY r.created_at DESC
            """)
    List<ReviewVO> findByActivityId(@Param("activityId") Long activityId);

    /**
     * 分页查询某用户收到的评价（带评价人信息和活动标题）
     */
    @Select("""
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
            WHERE r.reviewee_id = #{revieweeId}
            ORDER BY r.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<ReviewVO> findByRevieweeIdPaged(@Param("revieweeId") Long revieweeId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 查询某用户的平均评分
     */
    @Select("SELECT AVG(score) FROM reviews WHERE reviewee_id = #{revieweeId}")
    Double getAverageScoreByRevieweeId(@Param("revieweeId") Long revieweeId);
}
