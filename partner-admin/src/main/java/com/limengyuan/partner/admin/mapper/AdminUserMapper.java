package com.limengyuan.partner.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.entity.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员端 - 用户管理 Mapper
 */
public interface AdminUserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表（支持按关键词搜索用户名/昵称）
     *
     * @param keyword 搜索关键词（可为 null）
     * @param limit   每页数量
     * @param offset  偏移量
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT user_id, username, nickname, avatar_url, gender, city, credit_score, status, created_at " +
            "FROM users " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<User> findUsersPage(String keyword, int limit, int offset);

    /**
     * 统计用户总数（支持关键词过滤）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM users " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "</script>")
    long countUsers(String keyword);

    /**
     * 更新用户状态（封禁/解封）
     *
     * @param userId 用户ID
     * @param status 状态: 0-封禁, 1-正常
     */
    @Update("UPDATE users SET status = #{status} WHERE user_id = #{userId}")
    int updateUserStatus(Long userId, Integer status);
}
