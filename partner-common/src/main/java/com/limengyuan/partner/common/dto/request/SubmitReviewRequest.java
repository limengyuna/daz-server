package com.limengyuan.partner.common.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 提交评价请求DTO
 */
@Data
public class SubmitReviewRequest {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 被评价人ID
     */
    private Long revieweeId;

    /**
     * 评分 1-5 星
     */
    private Integer score;

    /**
     * 评价内容（可选）
     */
    private String content;

    /**
     * 评价标签（可选），如: ["守时", "幽默"]
     */
    private List<String> tags;
}
