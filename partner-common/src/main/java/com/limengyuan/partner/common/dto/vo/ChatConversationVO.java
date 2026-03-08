package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私聊会话展示 VO
 * <p>
 * 包含对方用户的昵称、头像等信息，用于会话列表展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationVO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 对方昵称
     */
    private String otherNickname;

    /**
     * 对方头像
     */
    private String otherAvatarUrl;

    /**
     * 最后一条消息预览
     */
    private String lastMessageContent;

    /**
     * 最后聊天时间
     */
    private LocalDateTime lastMessageTime;
}
