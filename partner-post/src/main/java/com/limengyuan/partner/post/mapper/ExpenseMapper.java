package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.vo.ExpenseVO;
import com.limengyuan.partner.common.entity.ActivityExpense;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * 活动支出数据访问层 - 封装 activity_expenses 表的所有数据库操作
 */
@Repository
public class ExpenseMapper {

    private final JdbcTemplate jdbcTemplate;

    public ExpenseMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 插入支出记录并返回生成的ID
     */
    public Long insert(ActivityExpense expense) {
        String sql = """
                INSERT INTO activity_expenses (activity_id, payer_id, title, amount, category, images, remark, split_type)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, expense.getActivityId());
            ps.setLong(2, expense.getPayerId());
            ps.setString(3, expense.getTitle());
            ps.setBigDecimal(4, expense.getAmount());
            ps.setInt(5, expense.getCategory() != null ? expense.getCategory() : 0);
            ps.setString(6, expense.getImages());
            ps.setString(7, expense.getRemark());
            ps.setInt(8, expense.getSplitType() != null ? expense.getSplitType() : 1);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 根据ID查询支出记录
     */
    public Optional<ActivityExpense> findById(Long expenseId) {
        String sql = "SELECT * FROM activity_expenses WHERE expense_id = ?";
        try {
            ActivityExpense expense = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(ActivityExpense.class), expenseId);
            return Optional.ofNullable(expense);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据活动ID查询所有支出记录（包含付款人信息）
     */
    public List<ExpenseVO> findByActivityIdWithPayer(Long activityId) {
        String sql = """
                SELECT e.*,
                       u.nickname AS payer_nickname,
                       u.avatar_url AS payer_avatar
                FROM activity_expenses e
                LEFT JOIN users u ON e.payer_id = u.user_id
                WHERE e.activity_id = ?
                ORDER BY e.created_at DESC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpenseVO.class), activityId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 根据ID查询支出详情（包含付款人信息）
     */
    public Optional<ExpenseVO> findByIdWithPayer(Long expenseId) {
        String sql = """
                SELECT e.*,
                       u.nickname AS payer_nickname,
                       u.avatar_url AS payer_avatar
                FROM activity_expenses e
                LEFT JOIN users u ON e.payer_id = u.user_id
                WHERE e.expense_id = ?
                """;
        try {
            ExpenseVO expense = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(ExpenseVO.class), expenseId);
            return Optional.ofNullable(expense);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据ID删除支出记录
     */
    public boolean deleteById(Long expenseId) {
        String sql = "DELETE FROM activity_expenses WHERE expense_id = ?";
        int rows = jdbcTemplate.update(sql, expenseId);
        return rows > 0;
    }

    /**
     * 统计某活动的总支出金额
     */
    public java.math.BigDecimal sumAmountByActivityId(Long activityId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM activity_expenses WHERE activity_id = ?";
        return jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class, activityId);
    }

    /**
     * 统计某活动的支出笔数
     */
    public int countByActivityId(Long activityId) {
        String sql = "SELECT COUNT(*) FROM activity_expenses WHERE activity_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, activityId);
        return count != null ? count : 0;
    }

    /**
     * 查询某用户在某活动中的垫付总额
     */
    public java.math.BigDecimal sumAmountByActivityIdAndPayerId(Long activityId, Long payerId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM activity_expenses WHERE activity_id = ? AND payer_id = ?";
        return jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class, activityId, payerId);
    }
}
