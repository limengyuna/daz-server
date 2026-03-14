package com.limengyuan.partner.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分摊明细实体类 - 对应 expense_splits 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("expense_splits")
public class ExpenseSplit {

    /**
     * 分摊记录ID
     */
    @TableId(value = "split_id", type = IdType.AUTO)
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

    // 结清状态常量
    public static final int SETTLED_NO = 0;   // 未结清
    public static final int SETTLED_YES = 1;  // 已结清
}
