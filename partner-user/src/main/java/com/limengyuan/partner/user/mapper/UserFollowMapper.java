package com.limengyuan.partner.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注关系数据访问层 - MyBatis-Plus
 *
 * 简单操作使用 BaseMapper 内置方法 + QueryWrapper：
 * - insert(entity)           → 添加关注
 * - delete(wrapper)          → 取消关注
 * - selectCount(wrapper)     → 统计关注/粉丝数
 *
 * 复杂联查使用 @Select 注解
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 获取关注列表（我关注的人）- 分页
     */
    @Select("""
            SELECT u.* FROM users u
            INNER JOIN user_follows f ON u.user_id = f.followee_id
            WHERE f.follower_id = #{userId}
            ORDER BY f.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<User> getFollowingList(@Param("userId") Long userId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 获取粉丝列表（关注我的人）- 分页
     */
    @Select("""
            SELECT u.* FROM users u
            INNER JOIN user_follows f ON u.user_id = f.follower_id
            WHERE f.followee_id = #{userId}
            ORDER BY f.created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<User> getFollowersList(@Param("userId") Long userId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);
}
