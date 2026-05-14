package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.dto.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
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
            WHERE a.status = 0
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
            WHERE a.status = 0 AND JSON_CONTAINS(a.category_ids, JSON_ARRAY(#{categoryId}))
            ORDER BY a.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ActivityVO> findAllWithUserByCategory(@Param("categoryId") Integer categoryId,
                                               @Param("size") int size,
                                               @Param("offset") int offset);

    /**
     * 查询活动总数（全部）
     */
    @Select("SELECT COUNT(*) FROM activities WHERE status = 0")
    long countAll();

    /**
     * 查询指定分类的活动总数
     */
    @Select("SELECT COUNT(*) FROM activities WHERE status = 0 AND JSON_CONTAINS(category_ids, JSON_ARRAY(#{categoryId}))")
    long countAllByCategory(@Param("categoryId") Integer categoryId);

    /**
     * 分页查询活动（支持关键词搜索 + 分类筛选）
     * 关键词匹配 title、description、location_name
     */
    @Select("""
            <script>
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE a.status = 0
            <if test="categoryId != null">
                AND JSON_CONTAINS(a.category_ids, JSON_ARRAY(#{categoryId}))
            </if>
            <if test="keyword != null and keyword != ''">
                AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                     OR a.description LIKE CONCAT('%', #{keyword}, '%')
                     OR a.location_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY a.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<ActivityVO> findAllWithUserFiltered(@Param("categoryId") Integer categoryId,
                                             @Param("keyword") String keyword,
                                             @Param("size") int size,
                                             @Param("offset") int offset);

    /**
     * 查询活动总数（支持关键词搜索 + 分类筛选）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM activities a
            WHERE a.status = 0
            <if test="categoryId != null">
                AND JSON_CONTAINS(a.category_ids, JSON_ARRAY(#{categoryId}))
            </if>
            <if test="keyword != null and keyword != ''">
                AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                     OR a.description LIKE CONCAT('%', #{keyword}, '%')
                     OR a.location_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countAllFiltered(@Param("categoryId") Integer categoryId,
                           @Param("keyword") String keyword);

    /**
     * 查询用户的标签和城市信息（用于 AI 推荐构建用户画像）
     */
    @Select("SELECT tags, city, bio FROM users WHERE user_id = #{userId}")
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

    /**
     * 根据活动ID列表批量查询活动（包含发布者信息）
     * 用于 RAG 向量召回后，回查完整的活动业务数据
     */
    @Select("""
            <script>
            SELECT a.*,
                   u.nickname AS initiator_nickname,
                   u.avatar_url AS initiator_avatar,
                   u.credit_score AS initiator_credit_score,
                   (SELECT COUNT(*) FROM participants p
                    WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
            FROM activities a
            LEFT JOIN users u ON a.initiator_id = u.user_id
            WHERE a.activity_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    List<ActivityVO> findByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有招募中活动的 ID 列表（用于向量同步对比）
     */
    @Select("SELECT activity_id FROM activities WHERE status = 0")
    List<Long> findRecruitingActivityIds();

    /**
     * 增量查询：获取指定时间之后状态发生变更的活动
     * 用于定时增量同步，只扫描有变化的数据，避免全表扫描
     *
     * @param since 上次同步时间
     * @return 变更的活动列表（只包含 activity_id 和 status）
     */
    @Select("SELECT activity_id, status FROM activities WHERE updated_at > #{since}")
    List<Activity> findChangedSince(@Param("since") LocalDateTime since);

    /**
     * 更新活动状态
     * @param activityId 活动ID
     * @param status 新状态: 0-招募中, 1-已满员, 2-活动结束, 3-已取消
     */
    @Update("UPDATE activities SET status = #{status}, updated_at = NOW() WHERE activity_id = #{activityId}")
    boolean updateStatus(@Param("activityId") Long activityId, @Param("status") Integer status);

    /**
     * 查询已过期但状态未更新的活动 ID 列表
     * 条件：end_time 已过且 status 仍为招募中(0)或已满员(1)
     *
     * @return 需要自动结束的活动 ID 列表
     */
    @Select("SELECT activity_id FROM activities WHERE end_time < NOW() AND status IN (0, 1)")
    List<Long> findExpiredActivityIds();

    /**
     * 批量将活动状态更新为已结束(status=2)
     * 用于定时任务自动结束过期活动
     *
     * @param ids 活动 ID 列表
     * @return 更新的行数
     */
    @Update("""
            <script>
            UPDATE activities SET status = 2, updated_at = NOW()
            WHERE activity_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int batchUpdateStatusToEnded(@Param("ids") List<Long> ids);

    /**
     * 查询活动群聊中的文本消息（用于 AI 旅行回忆生成）
     * 只取文本消息（msg_type=1），按时间正序排列
     */
    @Select("""
            SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                   u.nickname AS sender_nickname,
                   u.avatar_url AS sender_avatar_url
            FROM chat_messages m
            LEFT JOIN users u ON m.sender_id = u.user_id
            WHERE m.activity_id = #{activityId} AND m.msg_type = 1
            ORDER BY m.created_at ASC
            LIMIT #{limit}
            """)
    List<ChatMessageVO> findGroupTextMessages(@Param("activityId") Long activityId,
                                               @Param("limit") int limit);

    /**
     * 查询活动群聊中的图片消息（用于 AI 旅行回忆图片池）
     * 只取图片消息（msg_type=2），content 即图片 URL
     */
    @Select("""
            SELECT m.content
            FROM chat_messages m
            WHERE m.activity_id = #{activityId} AND m.msg_type = 2
            ORDER BY m.created_at ASC
            LIMIT #{limit}
            """)
    List<String> findGroupImageUrls(@Param("activityId") Long activityId,
                                     @Param("limit") int limit);

    /**
     * 查询活动群聊中的图片消息（带发送者信息）
     * 用于 AI 旅行回忆生成时，为每张群聊图片提供来源元数据，
     * 帮助 AI 将文案与图片精准匹配。
     */
    @Select("""
            SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                   u.nickname AS sender_nickname,
                   u.avatar_url AS sender_avatar_url
            FROM chat_messages m
            LEFT JOIN users u ON m.sender_id = u.user_id
            WHERE m.activity_id = #{activityId} AND m.msg_type = 2
            ORDER BY m.created_at ASC
            LIMIT #{limit}
            """)
    List<ChatMessageVO> findGroupImageMessages(@Param("activityId") Long activityId,
                                                @Param("limit") int limit);
}
