package com.limengyuan.partner.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据访问层 - MyBatis-Plus
 *
 * 内置方法（继承自 BaseMapper）：
 * - selectById(id)       → 根据ID查询
 * - insert(entity)       → 插入
 * - updateById(entity)   → 根据ID更新
 * - deleteById(id)       → 根据ID删除
 * - selectList(wrapper)  → 条件查询列表
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 更新用户信息（不包含密码，只更新指定字段）
     */
    @Update("""
            UPDATE users SET nickname = #{nickname}, avatar_url = #{avatarUrl}, gender = #{gender},
                birthday = #{birthday}, city = #{city}, bio = #{bio}, tags = #{tags}
            WHERE user_id = #{userId}
            """)
    int updateUserInfo(User user);

    /**
     * 更新用户信誉分（设置为算法计算后的最终值）
     */
    @Update("UPDATE users SET credit_score = #{creditScore} WHERE user_id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("creditScore") int creditScore);
}
