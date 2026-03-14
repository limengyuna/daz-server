package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ExpenseSplitVO;
import com.limengyuan.partner.common.entity.ExpenseSplit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分摊明细数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入分摊记录
 * - selectById(id)       → 根据ID查询
 * - delete(wrapper)      → 条件删除
 */
@Mapper
public interface ExpenseSplitMapper extends BaseMapper<ExpenseSplit> {

    /**
     * 标记某条分摊记录为已结清
     */
    @Update("UPDATE expense_splits SET is_settled = 1, settled_at = NOW() WHERE split_id = #{splitId} AND is_settled = 0")
    boolean settle(@Param("splitId") Long splitId);

    /**
     * 根据支出ID查询所有分摊记录（包含用户信息）
     */
    @Select("""
            SELECT s.*,
                   u.nickname,
                   u.avatar_url
            FROM expense_splits s
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE s.expense_id = #{expenseId}
            ORDER BY s.created_at ASC
            """)
    List<ExpenseSplitVO> findByExpenseIdWithUser(@Param("expenseId") Long expenseId);

    /**
     * 查询某用户所有未结清的分摊记录
     */
    @Select("""
            SELECT s.*,
                   u.nickname,
                   u.avatar_url
            FROM expense_splits s
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE s.user_id = #{userId} AND s.is_settled = 0
            ORDER BY s.created_at DESC
            """)
    List<ExpenseSplitVO> findUnsettledByUserId(@Param("userId") Long userId);

    /**
     * 查询某活动中某用户的应分摊总额
     */
    @Select("""
            SELECT COALESCE(SUM(s.amount), 0)
            FROM expense_splits s
            INNER JOIN activity_expenses e ON s.expense_id = e.expense_id
            WHERE e.activity_id = #{activityId} AND s.user_id = #{userId}
            """)
    BigDecimal sumAmountByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 查询某活动中所有分摊记录（用于结算汇总计算）
     */
    @Select("""
            SELECT s.*,
                   u.nickname,
                   u.avatar_url
            FROM expense_splits s
            INNER JOIN activity_expenses e ON s.expense_id = e.expense_id
            LEFT JOIN users u ON s.user_id = u.user_id
            WHERE e.activity_id = #{activityId}
            ORDER BY s.created_at ASC
            """)
    List<ExpenseSplitVO> findByActivityId(@Param("activityId") Long activityId);
}
