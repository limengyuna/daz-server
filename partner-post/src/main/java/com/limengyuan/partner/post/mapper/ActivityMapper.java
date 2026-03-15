package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入活动（自动回填 activityId）
 * - selectById(id)       → 根据ID查询
 * - selectList(wrapper)  → 条件查询
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 根据ID查询活动，包含发布者信息
     */
    @Select("""
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE a.activity_id = #{activityId}
            """)
    ActivityVO findByIdWithUser(@Param("activityId") Long activityId);

    /**
     * 根据发起人ID查询活动列表，包含发布者信息
     */
    @Select("""
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE a.initiator_id = #{initiatorId}
            ORDER BY a.created_at DESC
            """)
    List<ActivityVO> findByInitiatorIdWithUser(@Param("initiatorId") Long initiatorId);

    /**
     * 分页查询所有活动，包含发布者信息（不按分类筛选）
     */
    @Select("""
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            ORDER BY a.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ActivityVO> findAllWithUser(@Param("size") int size, @Param("offset") int offset);

    /**
     * 分页查询指定分类的活动，包含发布者信息
     */
    @Select("""
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE JSON_CONTAINS(a.category_ids, JSON_ARRAY(#{categoryId}))
            ORDER BY a.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ActivityVO> findAllWithUserByCategory(@Param("categoryId") Integer categoryId,
                                               @Param("size") int size,
                                               @Param("offset") int offset);

    /**
     * 查询活动总数（全部）
     */
    @Select("SELECT COUNT(*) FROM activities")
    long countAll();

    /**
     * 查询指定分类的活动总数
     */
    @Select("SELECT COUNT(*) FROM activities WHERE JSON_CONTAINS(category_ids, JSON_ARRAY(#{categoryId}))")
    long countAllByCategory(@Param("categoryId") Integer categoryId);

    /**
     * 查询用户的标签和城市信息（用于 AI 推荐构建用户画像）
     */
    @Select("SELECT tags, city FROM users WHERE user_id = #{userId}")
    java.util.Map<String, Object> findUserTagsAndCity(@Param("userId") Long userId);

    /**
     * 查询所有招募中的活动（status=0），按创建时间倒序，取最近 limit 条
     * 作为 AI 推荐的候选集
     */
    @Select("""
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE a.status = 0
            ORDER BY a.created_at DESC
            LIMIT #{limit}
            """)
    List<ActivityVO> findRecruitingActivities(@Param("limit") int limit);
}
