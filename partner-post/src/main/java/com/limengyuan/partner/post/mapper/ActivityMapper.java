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
import java.util.ArrayList;
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
                INSERT INTO activities (initiator_id, category_ids, title, description, images,
                    location_name, location_address, start_time, end_time, registration_end_time,
                    max_participants, payment_type, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, activity.getInitiatorId());
            ps.setString(2, activity.getCategoryIds());
            ps.setString(3, activity.getTitle());
            ps.setString(4, activity.getDescription());
            ps.setString(5, activity.getImages());
            ps.setString(6, activity.getLocationName());
            ps.setString(7, activity.getLocationAddress());
            ps.setTimestamp(8, Timestamp.valueOf(activity.getStartTime()));
            ps.setTimestamp(9, activity.getEndTime() != null ? Timestamp.valueOf(activity.getEndTime()) : null);
            ps.setTimestamp(10, activity.getRegistrationEndTime() != null ? Timestamp.valueOf(activity.getRegistrationEndTime()) : null);
            ps.setInt(11, activity.getMaxParticipants());
            ps.setInt(12, activity.getPaymentType());
            ps.setInt(13, activity.getStatus());
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
     * 根据ID查询活动，包含发布者信息
     */
    public Optional<ActivityVO> findByIdWithUser(Long activityId) {
        String sql = """
                SELECT a.*,
                       u.nickname AS initiator_nickname,
                       u.avatar_url AS initiator_avatar,
                       u.credit_score AS initiator_credit_score,
                       (SELECT COUNT(*) FROM participants p
                        WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
                FROM activities a
                LEFT JOIN users u ON a.initiator_id = u.user_id
                WHERE a.activity_id = ?
                """;
        try {
            ActivityVO activity = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(ActivityVO.class), activityId);
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
                SELECT a.*,
                       u.nickname AS initiator_nickname,
                       u.avatar_url AS initiator_avatar,
                       u.credit_score AS initiator_credit_score,
                       (SELECT COUNT(*) FROM participants p
                        WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
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

    /**
     * 分页查询所有活动，包含发布者信息，支持按分类筛选
     * 
     * @param page       页码 (从0开始)
     * @param size       每页数量
     * @param categoryId 分类ID，为null时查询所有分类
     */
    public List<ActivityVO> findAllWithUser(int page, int size, Integer categoryId) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.*,
                       u.nickname AS initiator_nickname,
                       u.avatar_url AS initiator_avatar,
                       u.credit_score AS initiator_credit_score,
                       (SELECT COUNT(*) FROM participants p
                        WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants
                FROM activities a
                LEFT JOIN users u ON a.initiator_id = u.user_id
                """);

        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append("WHERE JSON_CONTAINS(a.category_ids, JSON_ARRAY(?)) ");
            params.add(categoryId);
        }
        sql.append("ORDER BY a.created_at DESC LIMIT ? OFFSET ?");
        int offset = page * size;
        params.add(size);
        params.add(offset);

        try {
            return jdbcTemplate.query(sql.toString(),
                    new BeanPropertyRowMapper<>(ActivityVO.class), params.toArray());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 查询活动总数，支持按分类筛选
     *
     * @param categoryId 分类ID，为null时统计所有
     */
    public long countAll(Integer categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM activities");
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" WHERE JSON_CONTAINS(category_ids, JSON_ARRAY(?))");
            params.add(categoryId);
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }
}
