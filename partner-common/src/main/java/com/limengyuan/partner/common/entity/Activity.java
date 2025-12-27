package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体类 - 对应 activities 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 标题
     */
    private String title;

    /**
     * 详细描述/要求
     */
    private String description;

    /**
     * 活动配图 - JSON数组
     */
    private String images;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 最大参与人数 (含发起人)
     */
    private Integer maxParticipants;

    /**
     * 费用方式: 1-AA制, 2-发起人请客, 3-免费, 4-各付各的
     */
    private Integer paymentType;

    /**
     * 状态: 0-招募中, 1-已满员, 2-活动结束, 3-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
