package com.limengyuan.partner.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信誉分更新请求 DTO
 * 用于微服务间调用，触发信誉分重新计算
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCreditRequest {

    /**
     * 被评价的用户ID
     */
    private Long userId;

    /**
     * 触发原因（如："活动评价"、"管理员调整"）
     */
    private String reason;
}
