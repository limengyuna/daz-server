package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息展示 VO
 * <p>
 * 包含发送者的昵称、头像，用于消息列表展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者头像
     */
    private String senderAvatarUrl;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 类型: 1-文本, 2-图片, 3-位置
     */
    private Integer msgType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
