package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价返回VO - 包含评价人和被评价人信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 评价人ID
     */
    private Long reviewerId;

    /**
     * 评价人昵称
     */
    private String reviewerNickname;

    /**
     * 评价人头像
     */
    private String reviewerAvatar;

    /**
     * 被评价人ID
     */
    private Long revieweeId;

    /**
     * 被评价人昵称
     */
    private String revieweeNickname;

    /**
     * 被评价人头像
     */
    private String revieweeAvatar;

    /**
     * 评分 1-5 星
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签 - JSON字符串
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
