package com.limengyuan.partner.user.mapper;

import com.limengyuan.partner.common.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户关注关系数据访问层
 */
@Repository
public class UserFollowMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> User.builder()
            .userId(rs.getLong("user_id"))
            .username(rs.getString("username"))
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

    public UserFollowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 添加关注
     */
    public int follow(Long followerId, Long followeeId) {
        String sql = "INSERT INTO user_follows (follower_id, followee_id) VALUES (?, ?)";
        try {
            return jdbcTemplate.update(sql, followerId, followeeId);
        } catch (Exception e) {
            // 可能是重复关注，唯一索引冲突
            return 0;
        }
    }

    /**
     * 取消关注
     */
    public int unfollow(Long followerId, Long followeeId) {
        String sql = "DELETE FROM user_follows WHERE follower_id = ? AND followee_id = ?";
        return jdbcTemplate.update(sql, followerId, followeeId);
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long followerId, Long followeeId) {
        String sql = "SELECT COUNT(*) FROM user_follows WHERE follower_id = ? AND followee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, followerId, followeeId);
        return count != null && count > 0;
    }

    /**
     * 获取关注列表（我关注的人）
     */
    public List<User> getFollowingList(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                     "INNER JOIN user_follows f ON u.user_id = f.followee_id " +
                     "WHERE f.follower_id = ? " +
                     "ORDER BY f.created_at DESC";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, userId);
    }

    /**
     * 获取粉丝列表（关注我的人）
     */
    public List<User> getFollowersList(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                     "INNER JOIN user_follows f ON u.user_id = f.follower_id " +
                     "WHERE f.followee_id = ? " +
                     "ORDER BY f.created_at DESC";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, userId);
    }

    /**
     * 获取关注数量
     */
    public int getFollowingCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM user_follows WHERE follower_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * 获取粉丝数量
     */
    public int getFollowersCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM user_follows WHERE followee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }
}
