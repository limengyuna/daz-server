package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支出详情视图对象 - 包含分摊明细列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseVO {

    /**
     * 支出记录ID
     */
    private Long expenseId;

    /**
     * 关联活动ID
     */
    private Long activityId;

    /**
     * 付款人ID
     */
    private Long payerId;

    /**
     * 支出标题
     */
    private String title;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 消费分类
     */
    private Integer category;

    /**
     * 凭证/小票图片 - JSON数组
     */
    private String images;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分摊方式
     */
    private Integer splitType;

    /**
     * 记录时间
     */
    private LocalDateTime createdAt;

    // ========== 付款人信息 ==========

    /**
     * 付款人昵称
     */
    private String payerNickname;

    /**
     * 付款人头像
     */
    private String payerAvatar;

    // ========== 分摊明细列表 ==========

    /**
     * 分摊明细列表
     */
    private List<ExpenseSplitVO> splits;
}
