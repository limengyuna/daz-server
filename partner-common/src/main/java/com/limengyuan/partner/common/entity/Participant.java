package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动参与记录实体类 - 对应 participants 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    /**
     * 参与记录ID
     */
    private Long participantId;

    /**
     * 关联活动ID
     */
    private Long activityId;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 状态: 0-申请中, 1-已通过(群成员), 2-已拒绝, 3-主动退出
     */
    private Integer status;

    /**
     * 申请留言
     */
    private String applyMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 状态常量
    public static final int STATUS_PENDING = 0; // 申请中
    public static final int STATUS_APPROVED = 1; // 已通过
    public static final int STATUS_REJECTED = 2; // 已拒绝
    public static final int STATUS_LEFT = 3; // 主动退出
}
