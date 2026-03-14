package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分摊明细视图对象 - 包含分摊人信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitVO {

    /**
     * 分摊记录ID
     */
    private Long splitId;

    /**
     * 关联支出ID
     */
    private Long expenseId;

    /**
     * 分摊人ID
     */
    private Long userId;

    /**
     * 该用户应分摊的金额
     */
    private BigDecimal amount;

    /**
     * 是否已结清: 0-未结清, 1-已结清
     */
    private Integer isSettled;

    /**
     * 结清时间
     */
    private LocalDateTime settledAt;

    /**
     * 记录时间
     */
    private LocalDateTime createdAt;

    // ========== 分摊人信息 ==========

    /**
     * 分摊人昵称
     */
    private String nickname;

    /**
     * 分摊人头像
     */
    private String avatarUrl;
}
