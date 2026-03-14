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
 * 聊天消息实体类 - 对应 chat_messages 表
 * <p>
 * 私聊: receiverId 有值, activityId 为 null
 * 群聊: activityId 有值, receiverId 为 null
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_messages")
public class ChatMessage {

    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID (私聊时必填, 群聊为null)
     */
    private Long receiverId;

    /**
     * 活动ID (群聊时必填, 私聊为null)
     */
    private Long activityId;

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
