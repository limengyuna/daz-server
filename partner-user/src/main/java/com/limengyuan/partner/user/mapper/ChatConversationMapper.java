package com.limengyuan.partner.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ChatConversationVO;
import com.limengyuan.partner.common.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天会话数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)      → 创建会话（自动回填 conversationId）
 * - selectById(id)      → 根据ID查询会话
 *
 * 复杂查询使用 @Select / @Update 注解
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 获取用户的所有私聊会话列表，按最后消息时间倒序
     * 通过 CASE WHEN 判断对方用户ID，JOIN users 取对方信息
     */
    @Select("""
            SELECT c.conversation_id,
                   c.last_message_content,
                   c.last_message_time,
                   CASE WHEN c.user_a_id = #{userId} THEN c.user_b_id ELSE c.user_a_id END AS other_user_id,
                   u.nickname AS other_nickname,
                   u.avatar_url AS other_avatar_url
            FROM chat_conversations c
            LEFT JOIN users u ON u.user_id = CASE WHEN c.user_a_id = #{userId} THEN c.user_b_id ELSE c.user_a_id END
            WHERE c.user_a_id = #{userId} OR c.user_b_id = #{userId}
            ORDER BY c.last_message_time DESC
            """)
    List<ChatConversationVO> findConversationsByUserId(@Param("userId") Long userId);

    /**
     * 查找两个用户之间的会话（user_a_id 始终为较小的ID）
     */
    @Select("SELECT * FROM chat_conversations WHERE user_a_id = #{userAId} AND user_b_id = #{userBId}")
    ChatConversation findConversationByUsers(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    /**
     * 更新会话最后消息内容和时间
     */
    @Update("UPDATE chat_conversations SET last_message_content = #{content}, last_message_time = NOW() WHERE conversation_id = #{conversationId}")
    void updateConversationLastMessage(@Param("conversationId") Long conversationId, @Param("content") String content);
}
