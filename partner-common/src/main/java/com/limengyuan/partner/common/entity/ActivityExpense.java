package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动支出记录实体类 - 对应 activity_expenses 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExpense {

    /**
     * 支出记录ID
     */
    private Long expenseId;

    /**
     * 关联活动ID
     */
    private Long activityId;

    /**
     * 付款人ID（谁先垫的钱）
     */
    private Long payerId;

    /**
     * 支出标题（如：午餐、门票、打车费）
     */
    private String title;

    /**
     * 金额（元）
     */
    private BigDecimal amount;

    /**
     * 消费分类: 0-其他, 1-餐饮, 2-交通, 3-住宿, 4-门票, 5-购物
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
     * 分摊方式: 1-均摊, 2-按比例, 3-指定金额
     */
    private Integer splitType;

    /**
     * 记录时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 分类常量
    public static final int CATEGORY_OTHER = 0;      // 其他
    public static final int CATEGORY_FOOD = 1;        // 餐饮
    public static final int CATEGORY_TRANSPORT = 2;   // 交通
    public static final int CATEGORY_HOTEL = 3;       // 住宿
    public static final int CATEGORY_TICKET = 4;      // 门票
    public static final int CATEGORY_SHOPPING = 5;    // 购物

    // 分摊方式常量
    public static final int SPLIT_EQUAL = 1;          // 均摊
    public static final int SPLIT_RATIO = 2;          // 按比例
    public static final int SPLIT_CUSTOM = 3;         // 指定金额
}
