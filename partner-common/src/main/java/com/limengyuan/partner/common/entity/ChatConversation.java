package com.limengyuan.partner.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私聊会话实体类 - 对应 chat_conversations 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_conversations")
public class ChatConversation {

    /**
     * 会话ID
     */
    @TableId(value = "conversation_id", type = IdType.AUTO)
    private Long conversationId;

    /**
     * 用户A (ID较小者)
     */
    private Long userAId;

    /**
     * 用户B (ID较大者)
     */
    private Long userBId;

    /**
     * 最后一条消息预览
     */
    private String lastMessageContent;

    /**
     * 最后聊天时间
     */
    private LocalDateTime lastMessageTime;
}
