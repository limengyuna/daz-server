package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 行程规划 VO
 * <p>
 * AI 根据用户的自然语言描述，生成结构化的活动信息，
 * 可直接填充到前端"创建活动"表单中。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripPlanVO {

    /**
     * 活动标题（AI 生成）
     */
    private String title;

    /**
     * 活动详细描述 / 行程安排（AI 生成，含每日行程）
     */
    private String description;

    /**
     * 地点名称（如"塔公草原"）
     */
    private String locationName;

    /**
     * 详细地址（如"四川省甘孜州康定市塔公镇"）
     */
    private String locationAddress;

    /**
     * 活动开始时间（ISO 格式，如"2026-04-03T09:00:00"）
     */
    private String startTime;

    /**
     * 活动结束时间
     */
    private String endTime;

    /**
     * 报名截止时间（默认出发前2天）
     */
    private String registrationEndTime;

    /**
     * 推荐最大参与人数（含发起人）
     */
    private Integer maxParticipants;

    /**
     * 推荐费用方式: 1-AA制, 2-发起人请客, 3-免费, 4-各付各的
     */
    private Integer paymentType;

    /**
     * 人均预算（元）
     */
    private BigDecimal budget;

    /**
     * 推荐分类ID列表
     */
    private List<Integer> categoryIds;
}
