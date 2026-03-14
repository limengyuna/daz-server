package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算汇总视图对象 - 展示活动中谁欠谁多少钱
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementVO {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动总支出
     */
    private BigDecimal totalAmount;

    /**
     * 支出笔数
     */
    private Integer expenseCount;

    /**
     * 债务关系列表（谁欠谁多少）
     */
    private List<DebtItem> debts;

    /**
     * 个人汇总列表（每个人总共花了多少、应付多少、实付多少）
     */
    private List<PersonalSummary> personalSummaries;

    /**
     * 单条债务关系
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DebtItem {

        /**
         * 欠款人ID
         */
        private Long debtorId;

        /**
         * 欠款人昵称
         */
        private String debtorNickname;

        /**
         * 欠款人头像
         */
        private String debtorAvatar;

        /**
         * 债权人ID（被欠钱的人）
         */
        private Long creditorId;

        /**
         * 债权人昵称
         */
        private String creditorNickname;

        /**
         * 债权人头像
         */
        private String creditorAvatar;

        /**
         * 欠款金额
         */
        private BigDecimal amount;

        /**
         * 是否已结清
         */
        private Boolean settled;
    }

    /**
     * 个人汇总
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalSummary {

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户昵称
         */
        private String nickname;

        /**
         * 用户头像
         */
        private String avatarUrl;

        /**
         * 垫付总额（该用户一共垫了多少钱）
         */
        private BigDecimal totalPaid;

        /**
         * 应承担总额（该用户应分摊多少钱）
         */
        private BigDecimal totalOwed;

        /**
         * 净额（正数=别人欠我，负数=我欠别人）
         */
        private BigDecimal balance;
    }
}
