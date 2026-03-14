package com.limengyuan.partner.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.request.CreateExpenseRequest;
import com.limengyuan.partner.common.dto.vo.ExpenseSplitVO;
import com.limengyuan.partner.common.dto.vo.ExpenseVO;
import com.limengyuan.partner.common.dto.vo.SettlementVO;
import com.limengyuan.partner.common.entity.ActivityExpense;
import com.limengyuan.partner.common.entity.ExpenseSplit;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ExpenseMapper;
import com.limengyuan.partner.post.mapper.ExpenseSplitMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import com.limengyuan.partner.common.dto.vo.ParticipantVO;
import com.limengyuan.partner.common.entity.Activity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支出 & 分摊业务逻辑层
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseSplitMapper expenseSplitMapper;
    private final ParticipantMapper participantMapper;
    private final ActivityMapper activityMapper;
    private final ObjectMapper objectMapper;

    public ExpenseService(ExpenseMapper expenseMapper, ExpenseSplitMapper expenseSplitMapper,
                          ParticipantMapper participantMapper, ActivityMapper activityMapper,
                          ObjectMapper objectMapper) {
        this.expenseMapper = expenseMapper;
        this.expenseSplitMapper = expenseSplitMapper;
        this.participantMapper = participantMapper;
        this.activityMapper = activityMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 添加一笔支出并自动生成分摊记录
     */
    public Result<ExpenseVO> createExpense(Long activityId, Long payerId, CreateExpenseRequest request) {
        // 1. 处理图片为 JSON 字符串
        String imagesJson = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(request.getImages());
            } catch (JsonProcessingException e) {
                return Result.error("图片格式错误");
            }
        }

        // 2. 确定参与分摊的用户列表
        List<Long> splitUserIds = request.getSplitUserIds();
        if (splitUserIds == null || splitUserIds.isEmpty()) {
            // 默认：该活动所有已通过的参与者 + 发起人
            List<ParticipantVO> participants = participantMapper.findApprovedByActivityIdWithUser(activityId);
            List<Long> userIds = participants.stream()
                    .map(ParticipantVO::getUserId)
                    .collect(Collectors.toCollection(ArrayList::new));

            // 查询活动信息，将发起人也加入分摊列表
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null) {
                Long initiatorId = activity.getInitiatorId();
                if (!userIds.contains(initiatorId)) {
                    userIds.add(initiatorId);
                }
            }

            splitUserIds = userIds;
        }

        if (splitUserIds.isEmpty()) {
            return Result.error("没有可分摊的参与者");
        }

        // 3. 构建支出实体并插入（MP 自动回填 expenseId）
        ActivityExpense expense = ActivityExpense.builder()
                .activityId(activityId)
                .payerId(payerId)
                .title(request.getTitle().trim())
                .amount(request.getAmount())
                .category(request.getCategory() != null ? request.getCategory() : 0)
                .images(imagesJson)
                .remark(request.getRemark())
                .splitType(request.getSplitType() != null ? request.getSplitType() : 1)
                .build();

        int rows = expenseMapper.insert(expense);
        if (rows == 0) {
            return Result.error("添加支出失败");
        }
        Long expenseId = expense.getExpenseId();

        // 4. 根据分摊方式计算每人的分摊金额
        List<ExpenseSplit> splits = calculateSplits(expenseId, request.getAmount(),
                request.getSplitType() != null ? request.getSplitType() : 1,
                splitUserIds, request.getCustomAmounts());

        if (splits.isEmpty()) {
            return Result.error("计算分摊金额失败");
        }

        // 5. 逐条插入分摊记录
        for (ExpenseSplit split : splits) {
            expenseSplitMapper.insert(split);
        }

        // 6. 查询并返回完整的支出详情
        return getExpenseDetail(expenseId);
    }

    /**
     * 获取某活动的所有支出列表
     */
    public Result<List<ExpenseVO>> getExpenseList(Long activityId) {
        List<ExpenseVO> expenses = expenseMapper.findByActivityIdWithPayer(activityId);
        // 为每笔支出加载分摊明细
        for (ExpenseVO expense : expenses) {
            List<ExpenseSplitVO> splits = expenseSplitMapper.findByExpenseIdWithUser(expense.getExpenseId());
            expense.setSplits(splits);
        }
        return Result.success(expenses);
    }

    /**
     * 获取某笔支出的详情（含分摊明细）
     */
    public Result<ExpenseVO> getExpenseDetail(Long expenseId) {
        ExpenseVO expense = expenseMapper.findByIdWithPayer(expenseId);
        if (expense == null) {
            return Result.error("支出记录不存在");
        }
        List<ExpenseSplitVO> splits = expenseSplitMapper.findByExpenseIdWithUser(expenseId);
        expense.setSplits(splits);
        return Result.success(expense);
    }

    /**
     * 删除某笔支出（同时删除分摊记录）
     */
    public Result<Void> deleteExpense(Long expenseId, Long userId) {
        ActivityExpense expense = expenseMapper.selectById(expenseId);
        if (expense == null) {
            return Result.error("支出记录不存在");
        }
        if (!expense.getPayerId().equals(userId)) {
            return Result.error("只有付款人可以删除该支出");
        }
        // 先删除分摊记录，再删除支出记录
        QueryWrapper<ExpenseSplit> wrapper = new QueryWrapper<>();
        wrapper.eq("expense_id", expenseId);
        expenseSplitMapper.delete(wrapper);
        expenseMapper.deleteById(expenseId);
        return Result.success("删除成功", null);
    }

    /**
     * 标记某条分摊为已结清
     */
    public Result<Void> settleSplit(Long splitId) {
        boolean success = expenseSplitMapper.settle(splitId);
        if (success) {
            return Result.success("已标记为结清", null);
        }
        return Result.error("结清失败，记录不存在或已结清");
    }

    /**
     * 获取活动账单汇总（谁欠谁多少）
     */
    public Result<SettlementVO> getSettlement(Long activityId) {
        // 1. 获取活动所有支出
        List<ExpenseVO> expenses = expenseMapper.findByActivityIdWithPayer(activityId);
        if (expenses.isEmpty()) {
            return Result.success(SettlementVO.builder()
                    .activityId(activityId)
                    .totalAmount(BigDecimal.ZERO)
                    .expenseCount(0)
                    .debts(List.of())
                    .personalSummaries(List.of())
                    .build());
        }

        // 2. 计算总支出
        BigDecimal totalAmount = expenses.stream()
                .map(ExpenseVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 计算每个人的垫付总额和应分摊总额
        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();
        Map<Long, String> nicknameMap = new HashMap<>();
        Map<Long, String> avatarMap = new HashMap<>();

        for (ExpenseVO expense : expenses) {
            paidMap.merge(expense.getPayerId(), expense.getAmount(), BigDecimal::add);
            nicknameMap.putIfAbsent(expense.getPayerId(), expense.getPayerNickname());
            avatarMap.putIfAbsent(expense.getPayerId(), expense.getPayerAvatar());

            List<ExpenseSplitVO> splits = expenseSplitMapper.findByExpenseIdWithUser(expense.getExpenseId());
            for (ExpenseSplitVO split : splits) {
                owedMap.merge(split.getUserId(), split.getAmount(), BigDecimal::add);
                nicknameMap.putIfAbsent(split.getUserId(), split.getNickname());
                avatarMap.putIfAbsent(split.getUserId(), split.getAvatarUrl());
            }
        }

        // 4. 计算每个人的净额
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(paidMap.keySet());
        allUserIds.addAll(owedMap.keySet());

        for (Long userId : allUserIds) {
            BigDecimal paid = paidMap.getOrDefault(userId, BigDecimal.ZERO);
            BigDecimal owed = owedMap.getOrDefault(userId, BigDecimal.ZERO);
            balanceMap.put(userId, paid.subtract(owed));
        }

        // 5. 构建个人汇总
        List<SettlementVO.PersonalSummary> personalSummaries = allUserIds.stream()
                .map(userId -> SettlementVO.PersonalSummary.builder()
                        .userId(userId)
                        .nickname(nicknameMap.get(userId))
                        .avatarUrl(avatarMap.get(userId))
                        .totalPaid(paidMap.getOrDefault(userId, BigDecimal.ZERO))
                        .totalOwed(owedMap.getOrDefault(userId, BigDecimal.ZERO))
                        .balance(balanceMap.get(userId))
                        .build())
                .collect(Collectors.toList());

        // 6. 计算简化后的债务关系
        List<SettlementVO.DebtItem> debts = calculateDebts(balanceMap, nicknameMap, avatarMap);

        // 7. 构建并返回结算汇总
        SettlementVO settlement = SettlementVO.builder()
                .activityId(activityId)
                .totalAmount(totalAmount)
                .expenseCount(expenses.size())
                .debts(debts)
                .personalSummaries(personalSummaries)
                .build();

        return Result.success(settlement);
    }

    /**
     * 获取当前用户所有未结清的账单
     */
    public Result<List<ExpenseSplitVO>> getMyBills(Long userId) {
        List<ExpenseSplitVO> bills = expenseSplitMapper.findUnsettledByUserId(userId);
        return Result.success(bills);
    }

    // ==================== 私有方法 ====================

    /**
     * 根据分摊方式计算每个人的分摊金额
     */
    private List<ExpenseSplit> calculateSplits(Long expenseId, BigDecimal totalAmount,
                                               int splitType, List<Long> splitUserIds,
                                               Map<Long, BigDecimal> customAmounts) {
        List<ExpenseSplit> splits = new ArrayList<>();

        switch (splitType) {
            case ActivityExpense.SPLIT_EQUAL -> {
                int count = splitUserIds.size();
                BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
                BigDecimal remainder = totalAmount.subtract(perPerson.multiply(BigDecimal.valueOf(count)));

                for (int i = 0; i < splitUserIds.size(); i++) {
                    BigDecimal amount = perPerson;
                    if (i == splitUserIds.size() - 1) {
                        amount = amount.add(remainder);
                    }
                    splits.add(ExpenseSplit.builder()
                            .expenseId(expenseId)
                            .userId(splitUserIds.get(i))
                            .amount(amount)
                            .build());
                }
            }
            case ActivityExpense.SPLIT_CUSTOM -> {
                if (customAmounts == null || customAmounts.isEmpty()) {
                    return List.of();
                }
                for (Long userId : splitUserIds) {
                    BigDecimal amount = customAmounts.getOrDefault(userId, BigDecimal.ZERO);
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        splits.add(ExpenseSplit.builder()
                                .expenseId(expenseId)
                                .userId(userId)
                                .amount(amount)
                                .build());
                    }
                }
            }
            default -> {
                int count = splitUserIds.size();
                BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
                BigDecimal remainder = totalAmount.subtract(perPerson.multiply(BigDecimal.valueOf(count)));

                for (int i = 0; i < splitUserIds.size(); i++) {
                    BigDecimal amount = perPerson;
                    if (i == splitUserIds.size() - 1) {
                        amount = amount.add(remainder);
                    }
                    splits.add(ExpenseSplit.builder()
                            .expenseId(expenseId)
                            .userId(splitUserIds.get(i))
                            .amount(amount)
                            .build());
                }
            }
        }

        return splits;
    }

    /**
     * 使用贪心算法计算简化后的债务关系
     */
    private List<SettlementVO.DebtItem> calculateDebts(Map<Long, BigDecimal> balanceMap,
                                                        Map<Long, String> nicknameMap,
                                                        Map<Long, String> avatarMap) {
        List<SettlementVO.DebtItem> debts = new ArrayList<>();

        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : balanceMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().abs()));
            }
        }

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Long debtorId = debtors.get(i).getKey();
            BigDecimal debtAmount = debtors.get(i).getValue();
            Long creditorId = creditors.get(j).getKey();
            BigDecimal creditAmount = creditors.get(j).getValue();

            BigDecimal transferAmount = debtAmount.min(creditAmount);

            debts.add(SettlementVO.DebtItem.builder()
                    .debtorId(debtorId)
                    .debtorNickname(nicknameMap.get(debtorId))
                    .debtorAvatar(avatarMap.get(debtorId))
                    .creditorId(creditorId)
                    .creditorNickname(nicknameMap.get(creditorId))
                    .creditorAvatar(avatarMap.get(creditorId))
                    .amount(transferAmount)
                    .settled(false)
                    .build());

            debtors.get(i).setValue(debtAmount.subtract(transferAmount));
            creditors.get(j).setValue(creditAmount.subtract(transferAmount));

            if (debtors.get(i).getValue().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (creditors.get(j).getValue().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        return debts;
    }
}
