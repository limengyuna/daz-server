package com.limengyuan.partner.common.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建支出请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {

    /**
     * 支出标题（如：午餐、门票、打车费）
     */
    @NotBlank(message = "支出标题不能为空")
    private String title;

    /**
     * 总金额（元）
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    /**
     * 消费分类: 0-其他, 1-餐饮, 2-交通, 3-住宿, 4-门票, 5-购物
     */
    private Integer category;

    /**
     * 凭证/小票图片URL列表
     */
    private List<String> images;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分摊方式: 1-均摊, 2-按比例, 3-指定金额
     */
    private Integer splitType;

    /**
     * 参与分摊的用户ID列表
     * 为空时默认为该活动所有已通过的参与者（包括发起人）
     */
    private List<Long> splitUserIds;

    /**
     * 指定金额分摊时使用：用户ID -> 分摊金额
     * splitType = 3 时必填
     */
    private Map<Long, BigDecimal> customAmounts;
}
