package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.request.CreateExpenseRequest;
import com.limengyuan.partner.common.dto.vo.ExpenseSplitVO;
import com.limengyuan.partner.common.dto.vo.ExpenseVO;
import com.limengyuan.partner.common.dto.vo.SettlementVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.post.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动支出 & 分摊控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * 添加一笔支出（自动生成分摊记录）
     * POST /api/activities/{activityId}/expenses
     */
    @PostMapping("/activities/{activityId}/expenses")
    public Result<ExpenseVO> createExpense(
            @PathVariable("activityId") Long activityId,
            @Valid @RequestBody CreateExpenseRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return expenseService.createExpense(activityId, userId, request);
    }

    /**
     * 获取某活动的所有支出列表
     * GET /api/activities/{activityId}/expenses
     */
    @GetMapping("/activities/{activityId}/expenses")
    public Result<List<ExpenseVO>> getExpenseList(@PathVariable("activityId") Long activityId) {
        return expenseService.getExpenseList(activityId);
    }

    /**
     * 获取某笔支出详情（含分摊明细）
     * GET /api/activities/{activityId}/expenses/{expenseId}
     */
    @GetMapping("/activities/{activityId}/expenses/{expenseId}")
    public Result<ExpenseVO> getExpenseDetail(
            @PathVariable("activityId") Long activityId,
            @PathVariable("expenseId") Long expenseId) {
        return expenseService.getExpenseDetail(expenseId);
    }

    /**
     * 删除某笔支出（同时删除分摊记录）
     * DELETE /api/activities/{activityId}/expenses/{expenseId}
     */
    @DeleteMapping("/activities/{activityId}/expenses/{expenseId}")
    public Result<Void> deleteExpense(
            @PathVariable("activityId") Long activityId,
            @PathVariable("expenseId") Long expenseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return expenseService.deleteExpense(expenseId, userId);
    }

    /**
     * 获取活动账单汇总（谁欠谁多少）
     * GET /api/activities/{activityId}/settlement
     */
    @GetMapping("/activities/{activityId}/settlement")
    public Result<SettlementVO> getSettlement(@PathVariable("activityId") Long activityId) {
        return expenseService.getSettlement(activityId);
    }

    /**
     * 标记某条分摊为已结清
     * PUT /api/expenses/{expenseId}/splits/{splitId}/settle
     */
    @PutMapping("/expenses/{expenseId}/splits/{splitId}/settle")
    public Result<Void> settleSplit(
            @PathVariable("expenseId") Long expenseId,
            @PathVariable("splitId") Long splitId) {
        return expenseService.settleSplit(splitId);
    }

    /**
     * 获取当前用户所有待结清的账单
     * GET /api/user/bills
     */
    @GetMapping("/user/bills")
    public Result<List<ExpenseSplitVO>> getMyBills(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return expenseService.getMyBills(userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 从请求头中解析用户ID
     */
    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return JwtUtils.getUserIdFromToken(authHeader);
    }
}
