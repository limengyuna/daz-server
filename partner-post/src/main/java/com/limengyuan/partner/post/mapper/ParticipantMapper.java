package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.MyApplicationVO;
import com.limengyuan.partner.common.dto.ParticipantVO;
import com.limengyuan.partner.common.entity.Participant;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 活动参与记录数据访问层
 */
@Repository
public class ParticipantMapper {

    private final JdbcTemplate jdbcTemplate;

    public ParticipantMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 插入参与记录并返回生成的ID
     */
    public Long insert(Participant participant) {
        String sql = """
                INSERT INTO participants (activity_id, user_id, status, apply_msg)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, participant.getActivityId());
            ps.setLong(2, participant.getUserId());
            ps.setInt(3, participant.getStatus());
            ps.setString(4, participant.getApplyMsg());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 根据ID查询参与记录
     */
    public Optional<Participant> findById(Long participantId) {
        String sql = "SELECT * FROM participants WHERE participant_id = ?";
        try {
            Participant participant = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(Participant.class), participantId);
            return Optional.ofNullable(participant);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据活动ID和用户ID查询参与记录（检查是否重复申请）
     */
    public Optional<Participant> findByActivityIdAndUserId(Long activityId, Long userId) {
        String sql = "SELECT * FROM participants WHERE activity_id = ? AND user_id = ?";
        try {
            Participant participant = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(Participant.class), activityId, userId);
            return Optional.ofNullable(participant);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据活动ID查询所有参与者（带用户信息）
     */
    public List<ParticipantVO> findByActivityIdWithUser(Long activityId) {
        String sql = """
                SELECT p.*,
                       u.nickname,
                       u.avatar_url,
                       u.credit_score,
                       u.gender
                FROM participants p
                LEFT JOIN users u ON p.user_id = u.user_id
                WHERE p.activity_id = ?
                ORDER BY p.created_at ASC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ParticipantVO.class), activityId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 根据活动ID查询已通过的参与者（带用户信息）
     */
    public List<ParticipantVO> findApprovedByActivityIdWithUser(Long activityId) {
        String sql = """
                SELECT p.*,
                       u.nickname,
                       u.avatar_url,
                       u.credit_score,
                       u.gender
                FROM participants p
                LEFT JOIN users u ON p.user_id = u.user_id
                WHERE p.activity_id = ? AND p.status = 1
                ORDER BY p.created_at ASC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ParticipantVO.class), activityId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 更新参与状态
     */
    public boolean updateStatus(Long participantId, Integer status) {
        String sql = "UPDATE participants SET status = ?, updated_at = ? WHERE participant_id = ?";
        int rows = jdbcTemplate.update(sql, status, Timestamp.valueOf(LocalDateTime.now()), participantId);
        return rows > 0;
    }

    /**
     * 统计活动已通过的参与人数
     */
    public int countApprovedByActivityId(Long activityId) {
        String sql = "SELECT COUNT(*) FROM participants WHERE activity_id = ? AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, activityId);
        return count != null ? count : 0;
    }

    /**
     * 根据用户ID查询所有申请记录（带活动标题）
     */
    public List<MyApplicationVO> findByUserIdWithActivity(Long userId) {
        String sql = """
                SELECT p.participant_id,
                       p.activity_id,
                       a.title AS activity_title,
                       p.status,
                       p.created_at
                FROM participants p
                LEFT JOIN activities a ON p.activity_id = a.activity_id
                WHERE p.user_id = ?
                ORDER BY p.created_at DESC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MyApplicationVO.class), userId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 统计某活动的申请总数
     */
    public int countByActivityId(Long activityId) {
        String sql = "SELECT COUNT(*) FROM participants WHERE activity_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, activityId);
        return count != null ? count : 0;
    }

    /**
     * 分页查询活动的参与者（带用户信息）
     *
     * @param activityId 活动ID
     * @param offset     偏移量
     * @param limit      每页数量
     * @return 参与者列表
     */
    public List<ParticipantVO> findByActivityIdWithUserPaged(Long activityId, int offset, int limit) {
        String sql = """
                SELECT p.*,
                       u.nickname,
                       u.avatar_url,
                       u.credit_score,
                       u.gender
                FROM participants p
                LEFT JOIN users u ON p.user_id = u.user_id
                WHERE p.activity_id = ?
                ORDER BY p.created_at ASC
                LIMIT ? OFFSET ?
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ParticipantVO.class), activityId, limit, offset);
        } catch (Exception e) {
            return List.of();
        }
    }
}
