package com.limengyuan.partner.user.mapper;

import com.limengyuan.partner.common.dto.ChatConversationVO;
import com.limengyuan.partner.common.dto.ChatMessageVO;
import com.limengyuan.partner.common.dto.GroupChatVO;
import com.limengyuan.partner.common.entity.ChatConversation;
import com.limengyuan.partner.common.entity.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 聊天数据访问层 - 封装会话和消息的数据库操作
 */
@Repository
public class ChatMapper {

    private final JdbcTemplate jdbcTemplate;

    public ChatMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================
    // 会话相关 RowMapper
    // ============================

    /**
     * 会话列表 VO 的 RowMapper
     * 需要配合查询 SQL 中的别名使用
     */
    private static final RowMapper<ChatConversationVO> CONVERSATION_VO_ROW_MAPPER = (rs, rowNum) -> ChatConversationVO
            .builder()
            .conversationId(rs.getLong("conversation_id"))
            .otherUserId(rs.getLong("other_user_id"))
            .otherNickname(rs.getString("other_nickname"))
            .otherAvatarUrl(rs.getString("other_avatar_url"))
            .lastMessageContent(rs.getString("last_message_content"))
            .lastMessageTime(rs.getTimestamp("last_message_time") != null
                    ? rs.getTimestamp("last_message_time").toLocalDateTime()
                    : null)
            .build();

    /**
     * 聊天消息 VO 的 RowMapper
     */
    private static final RowMapper<ChatMessageVO> MESSAGE_VO_ROW_MAPPER = (rs, rowNum) -> ChatMessageVO.builder()
            .messageId(rs.getLong("message_id"))
            .senderId(rs.getLong("sender_id"))
            .senderNickname(rs.getString("sender_nickname"))
            .senderAvatarUrl(rs.getString("sender_avatar_url"))
            .content(rs.getString("content"))
            .msgType(rs.getInt("msg_type"))
            .createdAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime()
                    : null)
            .build();

    // ============================
    // 会话操作
    // ============================

    /**
     * 获取用户的所有私聊会话列表，按最后消息时间倒序
     * 通过 CASE WHEN 判断对方用户ID，JOIN users 取对方信息
     *
     * @param userId 当前用户ID
     * @return 会话列表（包含对方用户信息）
     */
    public List<ChatConversationVO> findConversationsByUserId(Long userId) {
        String sql = """
                SELECT c.conversation_id,
                       c.last_message_content,
                       c.last_message_time,
                       CASE WHEN c.user_a_id = ? THEN c.user_b_id ELSE c.user_a_id END AS other_user_id,
                       u.nickname AS other_nickname,
                       u.avatar_url AS other_avatar_url
                FROM chat_conversations c
                LEFT JOIN users u ON u.user_id = CASE WHEN c.user_a_id = ? THEN c.user_b_id ELSE c.user_a_id END
                WHERE c.user_a_id = ? OR c.user_b_id = ?
                ORDER BY c.last_message_time DESC
                """;
        try {
            return jdbcTemplate.query(sql, CONVERSATION_VO_ROW_MAPPER, userId, userId, userId, userId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 根据会话ID查询会话
     */
    public Optional<ChatConversation> findConversationById(Long conversationId) {
        String sql = "SELECT * FROM chat_conversations WHERE conversation_id = ?";
        try {
            ChatConversation conv = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> ChatConversation.builder()
                    .conversationId(rs.getLong("conversation_id"))
                    .userAId(rs.getLong("user_a_id"))
                    .userBId(rs.getLong("user_b_id"))
                    .lastMessageContent(rs.getString("last_message_content"))
                    .lastMessageTime(rs.getTimestamp("last_message_time") != null
                            ? rs.getTimestamp("last_message_time").toLocalDateTime()
                            : null)
                    .build(), conversationId);
            return Optional.ofNullable(conv);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 查找两个用户之间的会话（user_a_id 始终为较小的ID）
     *
     * @param userAId 较小的用户ID
     * @param userBId 较大的用户ID
     * @return 会话（如果存在）
     */
    public Optional<ChatConversation> findConversationByUsers(Long userAId, Long userBId) {
        String sql = "SELECT * FROM chat_conversations WHERE user_a_id = ? AND user_b_id = ?";
        try {
            ChatConversation conv = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> ChatConversation.builder()
                    .conversationId(rs.getLong("conversation_id"))
                    .userAId(rs.getLong("user_a_id"))
                    .userBId(rs.getLong("user_b_id"))
                    .lastMessageContent(rs.getString("last_message_content"))
                    .lastMessageTime(rs.getTimestamp("last_message_time") != null
                            ? rs.getTimestamp("last_message_time").toLocalDateTime()
                            : null)
                    .build(), userAId, userBId);
            return Optional.ofNullable(conv);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 创建新的会话并返回会话ID
     *
     * @param userAId 较小的用户ID
     * @param userBId 较大的用户ID
     * @return 新创建的会话ID
     */
    public Long insertConversation(Long userAId, Long userBId) {
        String sql = "INSERT INTO chat_conversations (user_a_id, user_b_id) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userAId);
            ps.setLong(2, userBId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 更新会话最后消息内容和时间
     */
    public void updateConversationLastMessage(Long conversationId, String content) {
        String sql = "UPDATE chat_conversations SET last_message_content = ?, last_message_time = NOW() WHERE conversation_id = ?";
        // 截取消息预览（最多255字符）
        String preview = content != null && content.length() > 255 ? content.substring(0, 255) : content;
        jdbcTemplate.update(sql, preview, conversationId);
    }

    // ============================
    // 消息操作
    // ============================

    /**
     * 分页获取私聊消息（通过会话中的两个用户ID匹配）
     * 查询同时匹配 sender_id/receiver_id 双方向的消息
     *
     * @param userAId 会话中ID较小的用户
     * @param userBId 会话中ID较大的用户
     * @param page    页码（从0开始）
     * @param size    每页数量
     * @return 消息列表（包含发送者信息）
     */
    public List<ChatMessageVO> findPrivateMessages(Long userAId, Long userBId, int page, int size) {
        String sql = """
                SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                       u.nickname AS sender_nickname,
                       u.avatar_url AS sender_avatar_url
                FROM chat_messages m
                LEFT JOIN users u ON m.sender_id = u.user_id
                WHERE m.activity_id IS NULL
                  AND ((m.sender_id = ? AND m.receiver_id = ?)
                    OR (m.sender_id = ? AND m.receiver_id = ?))
                ORDER BY m.created_at DESC
                LIMIT ? OFFSET ?
                """;
        int offset = page * size;
        try {
            return jdbcTemplate.query(sql, MESSAGE_VO_ROW_MAPPER,
                    userAId, userBId, userBId, userAId, size, offset);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 分页获取群聊消息
     *
     * @param activityId 活动ID
     * @param page       页码（从0开始）
     * @param size       每页数量
     * @return 消息列表（包含发送者信息）
     */
    public List<ChatMessageVO> findGroupMessages(Long activityId, int page, int size) {
        String sql = """
                SELECT m.message_id, m.sender_id, m.content, m.msg_type, m.created_at,
                       u.nickname AS sender_nickname,
                       u.avatar_url AS sender_avatar_url
                FROM chat_messages m
                LEFT JOIN users u ON m.sender_id = u.user_id
                WHERE m.activity_id = ?
                ORDER BY m.created_at DESC
                LIMIT ? OFFSET ?
                """;
        int offset = page * size;
        try {
            return jdbcTemplate.query(sql, MESSAGE_VO_ROW_MAPPER, activityId, size, offset);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 插入消息并返回消息ID
     */
    public Long insertMessage(ChatMessage message) {
        String sql = """
                INSERT INTO chat_messages (sender_id, receiver_id, activity_id, content, msg_type)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, message.getSenderId());
            // receiverId 可能为 null（群聊场景）
            if (message.getReceiverId() != null) {
                ps.setLong(2, message.getReceiverId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            // activityId 可能为 null（私聊场景）
            if (message.getActivityId() != null) {
                ps.setLong(3, message.getActivityId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setString(4, message.getContent());
            ps.setInt(5, message.getMsgType() != null ? message.getMsgType() : 1);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    // ============================
    // 群聊列表查询
    // ============================

    /**
     * 查询用户参与的所有群聊列表
     * 包括：用户发起的活动 + 用户参与并已通过审核的活动
     * 通过子查询获取每个群聊的最后一条消息和成员数
     *
     * @param userId 当前用户ID
     * @return 群聊列表
     */
    public List<GroupChatVO> findGroupChatsByUserId(Long userId) {
        String sql = """
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
                WHERE (a.initiator_id = ?
                   OR a.activity_id IN (
                       SELECT p.activity_id FROM participants p
                       WHERE p.user_id = ? AND p.status = 1
                   ))
                  AND EXISTS (
                       SELECT 1 FROM chat_messages cm
                       WHERE cm.activity_id = a.activity_id
                  )
                ORDER BY COALESCE(last_msg.created_at, a.created_at) DESC
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> GroupChatVO.builder()
                    .activityId(rs.getLong("activity_id"))
                    .activityTitle(rs.getString("activity_title"))
                    .activityImages(rs.getString("activity_images"))
                    .activityStatus(rs.getInt("activity_status"))
                    .memberCount(rs.getInt("member_count"))
                    .lastMessageContent(rs.getString("last_message_content"))
                    .lastMessageSenderNickname(rs.getString("last_message_sender_nickname"))
                    .lastMessageTime(rs.getTimestamp("last_message_time") != null
                            ? rs.getTimestamp("last_message_time").toLocalDateTime()
                            : null)
                    .build(), userId, userId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
