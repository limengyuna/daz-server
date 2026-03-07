package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群聊列表展示 VO
 * <p>
 * 包含活动基本信息和最后一条群聊消息，用于消息页展示群聊列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatVO {

    /**
     * 活动ID（同时也是群聊标识）
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动配图（JSON数组，取第一张作为群聊头像）
     */
    private String activityImages;

    /**
     * 活动状态: 0-招募中, 1-已满员, 2-活动结束, 3-已取消
     */
    private Integer activityStatus;

    /**
     * 群聊成员数（已通过审核的参与者 + 发起人）
     */
    private Integer memberCount;

    /**
     * 最后一条群聊消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息的发送者昵称
     */
    private String lastMessageSenderNickname;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
}
