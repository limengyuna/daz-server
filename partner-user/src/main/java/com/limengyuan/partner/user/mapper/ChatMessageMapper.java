package com.limengyuan.partner.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ChatMessageVO;
import com.limengyuan.partner.common.dto.vo.GroupChatVO;
import com.limengyuan.partner.common.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)  → 插入消息（自动回填 messageId）
 *
 * 复杂联查使用 @Select 注解
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页获取私聊消息（通过会话中的两个用户ID匹配）
     * 查询同时匹配 sender_id/receiver_id 双方向的消息
     */
    @Select("""
            SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                   u.nickname AS sender_nickname,
                   u.avatar_url AS sender_avatar_url
            FROM chat_messages m
            LEFT JOIN users u ON m.sender_id = u.user_id
            WHERE m.activity_id IS NULL
              AND ((m.sender_id = #{userAId} AND m.receiver_id = #{userBId})
                OR (m.sender_id = #{userBId} AND m.receiver_id = #{userAId}))
            ORDER BY m.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ChatMessageVO> findPrivateMessages(@Param("userAId") Long userAId,
                                            @Param("userBId") Long userBId,
                                            @Param("size") int size,
                                            @Param("offset") int offset);

    /**
     * 分页获取群聊消息
     */
    @Select("""
            SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                   u.nickname AS sender_nickname,
                   u.avatar_url AS sender_avatar_url
            FROM chat_messages m
            LEFT JOIN users u ON m.sender_id = u.user_id
            WHERE m.activity_id = #{activityId}
            ORDER BY m.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ChatMessageVO> findGroupMessages(@Param("activityId") Long activityId,
                                          @Param("size") int size,
                                          @Param("offset") int offset);

    /**
     * 查询用户参与的所有群聊列表
     * 包括：用户发起的活动 + 用户参与并已通过审核的活动
     * 通过子查询获取每个群聊的最后一条消息和成员数
     */
    @Select("""
            SELECT a.activity_id,
                   a.title AS activity_title,
                   a.images AS activity_images,
                   a.status AS activity_status,
                   (SELECT COUNT(*) + 1 FROM participants p2
                    WHERE p2.activity_id = a.activity_id AND p2.status = 1) AS member_count,
                   last_msg.content AS last_message_content,
                   last_msg.sender_nickname AS last_message_sender_nickname,
                   last_msg.created_at AS last_message_time
            FROM activities a
            LEFT JOIN (
                SELECT m.activity_id,
                       m.content,
                       u.nickname AS sender_nickname,
                       m.created_at,
                       ROW_NUMBER() OVER (PARTITION BY m.activity_id ORDER BY m.created_at DESC) AS rn
                FROM chat_messages m
                LEFT JOIN users u ON m.sender_id = u.user_id
                WHERE m.activity_id IS NOT NULL
            ) last_msg ON last_msg.activity_id = a.activity_id AND last_msg.rn = 1
            WHERE (a.initiator_id = #{userId}
               OR a.activity_id IN (
                   SELECT p.activity_id FROM participants p
                   WHERE p.user_id = #{userId} AND p.status = 1
               ))
              AND EXISTS (
                   SELECT 1 FROM chat_messages cm
                   WHERE cm.activity_id = a.activity_id
              )
            ORDER BY COALESCE(last_msg.created_at, a.created_at) DESC
            """)
    List<GroupChatVO> findGroupChatsByUserId(@Param("userId") Long userId);
}
