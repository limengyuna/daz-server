package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价实体类 - 对应 reviews 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 关联活动ID
     */
    private Long activityId;

    /**
     * 评价人ID
     */
    private Long reviewerId;

    /**
     * 被评价人ID
     */
    private Long revieweeId;

    /**
     * 评分 1-5 星
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签 - JSON格式 (如: ["守时", "幽默"])
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
