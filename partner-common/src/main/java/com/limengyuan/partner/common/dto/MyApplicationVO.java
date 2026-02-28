package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的申请记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyApplicationVO {

    /**
     * 参与记录ID
     */
    private Long participantId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动配图 - JSON数组
     */
    private String images;

    /**
     * 活动地点名称
     */
    private String locationName;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人昵称
     */
    private String initiatorNickname;

    /**
     * 发起人头像
     */
    private String initiatorAvatar;

    /**
     * 发起人信用分
     */
    private Integer initiatorCreditScore;

    /**
     * 申请状态: 0-申请中, 1-已通过, 2-已拒绝, 3-主动退出
     */
    private Integer status;

    /**
     * 申请留言
     */
    private String applyMsg;

    /**
     * 申请时间
     */
    private LocalDateTime createdAt;
}
