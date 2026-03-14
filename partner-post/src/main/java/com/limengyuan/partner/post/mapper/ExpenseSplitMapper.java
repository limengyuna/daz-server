package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.vo.ExpenseSplitVO;
import com.limengyuan.partner.common.entity.ExpenseSplit;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分摊明细数据访问层 - 封装 expense_splits 表的所有数据库操作
 */
@Repository
public class ExpenseSplitMapper {

    private final JdbcTemplate jdbcTemplate;

    public ExpenseSplitMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 批量插入分摊记录
     */
    public void batchInsert(List<ExpenseSplit> splits) {
        String sql = """
                INSERT INTO expense_splits (expense_id, user_id, amount, is_settled)
                VALUES (?, ?, ?, 0)
                """;
        jdbcTemplate.batchUpdate(sql, splits, splits.size(), (ps, split) -> {
            ps.setLong(1, split.getExpenseId());
            ps.setLong(2, split.getUserId());
            ps.setBigDecimal(3, split.getAmount());
        });
    }

    /**
     * 根据支出ID查询所有分摊记录（包含用户信息）
     */
    public List<ExpenseSplitVO> findByExpenseIdWithUser(Long expenseId) {
        String sql = """
                SELECT s.*,
                       u.nickname,
                       u.avatar_url
                FROM expense_splits s
                LEFT JOIN users u ON s.user_id = u.user_id
                WHERE s.expense_id = ?
                ORDER BY s.created_at ASC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseSplitVO.class), expenseId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 根据支出ID删除所有分摊记录
     */
    public void deleteByExpenseId(Long expenseId) {
        String sql = "DELETE FROM expense_splits WHERE expense_id = ?";
        jdbcTemplate.update(sql, expenseId);
    }

    /**
     * 标记某条分摊记录为已结清
     */
    public boolean settle(Long splitId) {
        String sql = "UPDATE expense_splits SET is_settled = 1, settled_at = ? WHERE split_id = ? AND is_settled = 0";
        int rows = jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), splitId);
        return rows > 0;
    }

    /**
     * 根据ID查询分摊记录
     */
    public ExpenseSplit findById(Long splitId) {
        String sql = "SELECT * FROM expense_splits WHERE split_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ExpenseSplit.class), splitId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询某用户所有未结清的分摊记录（我的账单）
     * 包含支出信息和付款人信息
     */
    public List<ExpenseSplitVO> findUnsettledByUserId(Long userId) {
        String sql = """
                SELECT s.*,
                       u.nickname,
                       u.avatar_url
                FROM expense_splits s
                LEFT JOIN users u ON s.user_id = u.user_id
                WHERE s.user_id = ? AND s.is_settled = 0
                ORDER BY s.created_at DESC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseSplitVO.class), userId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 查询某活动中某用户的应分摊总额
     */
    public BigDecimal sumAmountByActivityAndUser(Long activityId, Long userId) {
        String sql = """
                SELECT COALESCE(SUM(s.amount), 0)
                FROM expense_splits s
                INNER JOIN activity_expenses e ON s.expense_id = e.expense_id
                WHERE e.activity_id = ? AND s.user_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, activityId, userId);
    }

    /**
     * 查询某活动中所有分摊记录（用于结算汇总计算）
     */
    public List<ExpenseSplitVO> findByActivityId(Long activityId) {
        String sql = """
                SELECT s.*,
                       u.nickname,
                       u.avatar_url
                FROM expense_splits s
                INNER JOIN activity_expenses e ON s.expense_id = e.expense_id
                LEFT JOIN users u ON s.user_id = u.user_id
                WHERE e.activity_id = ?
                ORDER BY s.created_at ASC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseSplitVO.class), activityId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
