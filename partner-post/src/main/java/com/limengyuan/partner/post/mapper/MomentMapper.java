package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.MomentCommentVO;
import com.limengyuan.partner.common.dto.MomentVO;
import com.limengyuan.partner.common.entity.Moment;
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
 * 动态数据访问层
 */
@Repository
public class MomentMapper {

    private final JdbcTemplate jdbcTemplate;

    public MomentMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== moments 表操作 ====================

    /**
     * 插入动态，返回生成的主键
     */
    public Long insert(Moment moment) {
        String sql = """
                INSERT INTO moments (user_id, content, images, location_name, location_address,
                    visibility, like_count, comment_count, view_count, status)
                VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, 1)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, moment.getUserId());
            ps.setString(2, moment.getContent());
            ps.setString(3, moment.getImages());
            ps.setString(4, moment.getLocationName());
            ps.setString(5, moment.getLocationAddress());
            ps.setInt(6, moment.getVisibility() != null ? moment.getVisibility() : 0);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 根据ID查询动态（含发布者信息）
     */
    public Optional<MomentVO> findByIdWithUser(Long momentId) {
        String sql = """
                SELECT m.*,
                       u.nickname  AS user_nickname,
                       u.avatar_url AS user_avatar,
                       u.credit_score AS user_credit_score
                FROM moments m
                LEFT JOIN users u ON m.user_id = u.user_id
                WHERE m.moment_id = ? AND m.status = 1
                """;
        try {
            MomentVO vo = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(MomentVO.class), momentId);
            return Optional.ofNullable(vo);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 分页查询所有公开动态（含发布者信息），按发布时间倒序
     */
    public List<MomentVO> findAllPublicWithUser(int page, int size) {
        String sql = """
                SELECT m.*,
                       u.nickname   AS user_nickname,
                       u.avatar_url AS user_avatar,
                       u.credit_score AS user_credit_score
                FROM moments m
                LEFT JOIN users u ON m.user_id = u.user_id
                WHERE m.status = 1 AND m.visibility = 0
                ORDER BY m.created_at DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MomentVO.class),
                size, (long) page * size);
    }

    /**
     * 统计所有公开动态数量
     */
    public long countAllPublic() {
        String sql = "SELECT COUNT(*) FROM moments WHERE status = 1 AND visibility = 0";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询指定用户发布的动态列表（含发布者信息），按时间倒序
     */
    public List<MomentVO> findByUserIdWithUser(Long userId) {
        String sql = """
                SELECT m.*,
                       u.nickname   AS user_nickname,
                       u.avatar_url AS user_avatar,
                       u.credit_score AS user_credit_score
                FROM moments m
                LEFT JOIN users u ON m.user_id = u.user_id
                WHERE m.user_id = ? AND m.status = 1
                ORDER BY m.created_at DESC
                """;
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MomentVO.class), userId);
    }

    /**
     * 浏览数 +1
     */
    public void incrementViewCount(Long momentId) {
        jdbcTemplate.update(
                "UPDATE moments SET view_count = view_count + 1 WHERE moment_id = ?",
                momentId);
    }

    /**
     * 点赞数 +1
     */
    public void incrementLikeCount(Long momentId) {
        jdbcTemplate.update(
                "UPDATE moments SET like_count = like_count + 1 WHERE moment_id = ?",
                momentId);
    }

    /**
     * 点赞数 -1（最小为0）
     */
    public void decrementLikeCount(Long momentId) {
        jdbcTemplate.update(
                "UPDATE moments SET like_count = GREATEST(like_count - 1, 0) WHERE moment_id = ?",
                momentId);
    }

    /**
     * 评论数 +1
     */
    public void incrementCommentCount(Long momentId) {
        jdbcTemplate.update(
                "UPDATE moments SET comment_count = comment_count + 1 WHERE moment_id = ?",
                momentId);
    }

    /**
     * 动态编辑更新（只更新非 null 字段，仅发布者本人可操作）
     */
    public int update(Long momentId, Long userId, String content, String imagesJson,
                      String locationName, String locationAddress, Integer visibility) {
        // 动态拼接 SET 子句，只更新传入的字段
        StringBuilder sql = new StringBuilder("UPDATE moments SET updated_at = NOW()");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (content != null) {
            sql.append(", content = ?");
            params.add(content);
        }
        if (imagesJson != null) {
            sql.append(", images = ?");
            params.add(imagesJson);
        }
        if (locationName != null) {
            sql.append(", location_name = ?");
            params.add(locationName);
        }
        if (locationAddress != null) {
            sql.append(", location_address = ?");
            params.add(locationAddress);
        }
        if (visibility != null) {
            sql.append(", visibility = ?");
            params.add(visibility);
        }

        sql.append(" WHERE moment_id = ? AND user_id = ? AND status = 1");
        params.add(momentId);
        params.add(userId);

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 软删除动态（只有发布者本人可以删除）
     */
    public int deleteSoft(Long momentId, Long userId) {
        String sql = "UPDATE moments SET status = 0 WHERE moment_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, momentId, userId);
    }

    // ==================== moment_likes 表操作 ====================

    /**
     * 插入点赞记录
     */
    public void insertLike(Long momentId, Long userId) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO moment_likes (moment_id, user_id) VALUES (?, ?)",
                momentId, userId);
    }

    /**
     * 删除点赞记录
     */
    public int deleteLike(Long momentId, Long userId) {
        return jdbcTemplate.update(
                "DELETE FROM moment_likes WHERE moment_id = ? AND user_id = ?",
                momentId, userId);
    }

    /**
     * 查询当前用户是否已点赞
     */
    public boolean existsLike(Long momentId, Long userId) {
        String sql = "SELECT COUNT(*) FROM moment_likes WHERE moment_id = ? AND user_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, momentId, userId);
        return count != null && count > 0;
    }

    // ==================== moment_comments 表操作 ====================

    /**
     * 插入评论，返回生成主键
     */
    public Long insertComment(Long momentId, Long userId, Long parentId, Long replyToId, String content) {
        String sql = """
                INSERT INTO moment_comments (moment_id, user_id, parent_id, reply_to_id, content, status)
                VALUES (?, ?, ?, ?, ?, 1)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, momentId);
            ps.setLong(2, userId);
            if (parentId != null) ps.setLong(3, parentId); else ps.setNull(3, java.sql.Types.BIGINT);
            if (replyToId != null) ps.setLong(4, replyToId); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setString(5, content);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 查询某动态的所有一级评论（含评论人信息）
     */
    public List<MomentCommentVO> findTopCommentsByMomentId(Long momentId) {
        String sql = """
                SELECT c.*,
                       u.nickname    AS user_nickname,
                       u.avatar_url  AS user_avatar
                FROM moment_comments c
                LEFT JOIN users u ON c.user_id = u.user_id
                WHERE c.moment_id = ? AND c.parent_id IS NULL AND c.status = 1
                ORDER BY c.created_at ASC
                """;
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MomentCommentVO.class), momentId);
    }

    /**
     * 查询某条一级评论下的所有回复（含评论人和被回复人信息）
     */
    public List<MomentCommentVO> findRepliesByParentId(Long parentId) {
        String sql = """
                SELECT c.*,
                       u.nickname    AS user_nickname,
                       u.avatar_url  AS user_avatar,
                       ru.nickname   AS reply_to_nickname
                FROM moment_comments c
                LEFT JOIN users u  ON c.user_id      = u.user_id
                LEFT JOIN users ru ON c.reply_to_id  = ru.user_id
                WHERE c.parent_id = ? AND c.status = 1
                ORDER BY c.created_at ASC
                """;
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MomentCommentVO.class), parentId);
    }
}
