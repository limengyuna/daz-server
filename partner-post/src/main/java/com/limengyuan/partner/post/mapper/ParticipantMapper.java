package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.MyApplicationVO;
import com.limengyuan.partner.common.dto.vo.ParticipantVO;
import com.limengyuan.partner.common.entity.Participant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 活动参与记录数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入参与记录（自动回填 participantId）
 * - selectById(id)       → 根据ID查询
 * - selectOne(wrapper)   → 条件查询单条
 * - selectCount(wrapper) → 条件统计
 */
@Mapper
public interface ParticipantMapper extends BaseMapper<Participant> {

    /**
     * 更新参与状态
     */
    @Update("UPDATE participants SET status = #{status}, updated_at = NOW() WHERE participant_id = #{participantId}")
    boolean updateStatus(@Param("participantId") Long participantId, @Param("status") Integer status);

    /**
     * 更新申请留言
     */
    @Update("UPDATE participants SET apply_msg = #{applyMsg}, updated_at = NOW() WHERE participant_id = #{participantId}")
    boolean updateApplyMsg(@Param("participantId") Long participantId, @Param("applyMsg") String applyMsg);

    /**
     * 根据活动ID查询所有参与者（带用户信息）
     */
    @Select("""
            SELECT p.*,
                   u.nickname,
                   u.avatar_url,
                   u.credit_score,
                   u.gender
            FROM participants p
            LEFT JOIN users u ON p.user_id = u.user_id
            WHERE p.activity_id = #{activityId}
            ORDER BY p.created_at ASC
            """)
    List<ParticipantVO> findByActivityIdWithUser(@Param("activityId") Long activityId);

    /**
     * 根据活动ID查询已通过的参与者（带用户信息）
     */
    @Select("""
            SELECT p.*,
                   u.nickname,
                   u.avatar_url,
                   u.credit_score,
                   u.gender
            FROM participants p
            LEFT JOIN users u ON p.user_id = u.user_id
            WHERE p.activity_id = #{activityId} AND p.status = 1
            ORDER BY p.created_at ASC
            """)
    List<ParticipantVO> findApprovedByActivityIdWithUser(@Param("activityId") Long activityId);

    /**
     * 根据用户ID查询所有申请记录（带活动标题）
     */
    @Select("""
            SELECT p.participant_id,
                   p.activity_id,
                   a.title         AS activity_title,
                   a.images,
                   a.location_name,
                   a.start_time,
                   a.initiator_id,
                   u.nickname      AS initiator_nickname,
                   u.avatar_url    AS initiator_avatar,
                   u.credit_score  AS initiator_credit_score,
                   p.status,
                   p.apply_msg,
                   p.created_at
            FROM participants p
            LEFT JOIN activities a ON p.activity_id = a.activity_id
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE p.user_id = #{userId}
            ORDER BY p.created_at DESC
            """)
    List<MyApplicationVO> findByUserIdWithActivity(@Param("userId") Long userId);

    /**
     * 分页查询活动的参与者（带用户信息）
     */
    @Select("""
            SELECT p.*,
                   u.nickname,
                   u.avatar_url,
                   u.credit_score,
                   u.gender
            FROM participants p
            LEFT JOIN users u ON p.user_id = u.user_id
            WHERE p.activity_id = #{activityId}
            ORDER BY p.created_at ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<ParticipantVO> findByActivityIdWithUserPaged(@Param("activityId") Long activityId,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);
}
