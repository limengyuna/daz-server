package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ExpenseVO;
import com.limengyuan.partner.common.entity.ActivityExpense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动支出数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入支出（自动回填 expenseId）
 * - selectById(id)       → 根据ID查询
 * - deleteById(id)       → 根据ID删除
 * - selectCount(wrapper) → 条件统计
 */
@Mapper
public interface ExpenseMapper extends BaseMapper<ActivityExpense> {

    /**
     * 根据活动ID查询所有支出记录（包含付款人信息）
     */
    @Select("""
            SELECT e.*,
                   u.nickname AS payer_nickname,
                   u.avatar_url AS payer_avatar
            FROM activity_expenses e
            LEFT JOIN users u ON e.payer_id = u.user_id
            WHERE e.activity_id = #{activityId}
            ORDER BY e.created_at DESC
            """)
    List<ExpenseVO> findByActivityIdWithPayer(@Param("activityId") Long activityId);

    /**
     * 根据ID查询支出详情（包含付款人信息）
     */
    @Select("""
            SELECT e.*,
                   u.nickname AS payer_nickname,
                   u.avatar_url AS payer_avatar
            FROM activity_expenses e
            LEFT JOIN users u ON e.payer_id = u.user_id
            WHERE e.expense_id = #{expenseId}
            """)
    ExpenseVO findByIdWithPayer(@Param("expenseId") Long expenseId);

    /**
     * 统计某活动的总支出金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM activity_expenses WHERE activity_id = #{activityId}")
    BigDecimal sumAmountByActivityId(@Param("activityId") Long activityId);

    /**
     * 查询某用户在某活动中的垫付总额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM activity_expenses WHERE activity_id = #{activityId} AND payer_id = #{payerId}")
    BigDecimal sumAmountByActivityIdAndPayerId(@Param("activityId") Long activityId, @Param("payerId") Long payerId);
}
