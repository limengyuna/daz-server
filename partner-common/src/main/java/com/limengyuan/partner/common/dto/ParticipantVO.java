package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 参与者视图对象 - 包含用户详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantVO {

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
     * 状态: 0-申请中, 1-已通过, 2-已拒绝, 3-主动退出
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

    // ========== 用户信息 ==========

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户信用分
     */
    private Integer creditScore;

    /**
     * 用户性别
     */
    private Integer gender;
}
