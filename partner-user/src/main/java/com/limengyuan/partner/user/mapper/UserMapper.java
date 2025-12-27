package com.limengyuan.partner.user.mapper;

import com.limengyuan.partner.common.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public class UserMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> User.builder()
            .userId(rs.getLong("user_id"))
            .username(rs.getString("username"))
            .passwordHash(rs.getString("password_hash"))
            .nickname(rs.getString("nickname"))
            .avatarUrl(rs.getString("avatar_url"))
            .gender(rs.getInt("gender"))
            .birthday(rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null)
            .city(rs.getString("city"))
            .bio(rs.getString("bio"))
            .tags(rs.getString("tags"))
            .creditScore(rs.getInt("credit_score"))
            .status(rs.getInt("status"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
            .build();

    public UserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据ID查询用户
     */
    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, userId);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据用户名查询用户
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    /**
     * 更新用户信息（不包含密码）
     */
    public int update(User user) {
        String sql = """
                UPDATE users SET nickname = ?, avatar_url = ?, gender = ?, birthday = ?,
                    city = ?, bio = ?, tags = ?
                WHERE user_id = ?
                """;
        return jdbcTemplate.update(sql,
                user.getNickname(),
                user.getAvatarUrl(),
                user.getGender(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getCity(),
                user.getBio(),
                user.getTags(),
                user.getUserId());
    }

    /**
     * 删除用户
     */
    public int deleteById(Long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }
}
