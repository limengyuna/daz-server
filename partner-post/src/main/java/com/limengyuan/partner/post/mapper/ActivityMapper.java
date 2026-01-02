package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.dto.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 活动数据访问层 - 封装所有数据库操作
 */
@Repository
public class ActivityMapper {

    private final JdbcTemplate jdbcTemplate;

    public ActivityMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 插入活动并返回生成的ID
     */
    public Long insert(Activity activity) {
        String sql = """
                INSERT INTO activities (initiator_id, category_id, title, description, images,
                    location_name, location_address, start_time, max_participants, payment_type, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, activity.getInitiatorId());
            ps.setInt(2, activity.getCategoryId());
            ps.setString(3, activity.getTitle());
            ps.setString(4, activity.getDescription());
            ps.setString(5, activity.getImages());
            ps.setString(6, activity.getLocationName());
            ps.setString(7, activity.getLocationAddress());
            ps.setTimestamp(8, Timestamp.valueOf(activity.getStartTime()));
            ps.setInt(9, activity.getMaxParticipants());
            ps.setInt(10, activity.getPaymentType());
            ps.setInt(11, activity.getStatus());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * 根据ID查询活动
     */
    public Optional<Activity> findById(Long activityId) {
        String sql = "SELECT * FROM activities WHERE activity_id = ?";
        try {
            Activity activity = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(Activity.class), activityId);
            return Optional.ofNullable(activity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据发起人ID查询活动列表
     */
    public List<Activity> findByInitiatorId(Long initiatorId) {
        String sql = "SELECT * FROM activities WHERE initiator_id = ? ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Activity.class), initiatorId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 根据发起人ID查询活动列表，包含发布者信息
     */
    public List<ActivityVO> findByInitiatorIdWithUser(Long initiatorId) {
        String sql = """
                SELECT a.*, u.nickname AS initiator_nickname, u.avatar_url AS initiator_avatar, u.credit_score AS initiator_credit_score
                FROM activities a
                LEFT JOIN users u ON a.initiator_id = u.user_id
                WHERE a.initiator_id = ?
                ORDER BY a.created_at DESC
                """;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ActivityVO.class), initiatorId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
