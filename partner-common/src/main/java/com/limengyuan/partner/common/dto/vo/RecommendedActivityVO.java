package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 推荐活动视图对象 - 包含活动信息和 AI 推荐理由
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedActivityVO {

    /**
     * 活动详情
     */
    private ActivityVO activity;

    /**
     * AI 推荐理由
     */
    private String reason;
}
