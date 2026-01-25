package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的申请记录视图对象 - 简化版
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
     * 申请状态: 0-申请中, 1-已通过, 2-已拒绝, 3-主动退出
     */
    private Integer status;

    /**
     * 申请时间
     */
    private LocalDateTime createdAt;
}
