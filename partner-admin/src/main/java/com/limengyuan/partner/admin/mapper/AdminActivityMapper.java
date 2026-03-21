package com.limengyuan.partner.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员端 - 活动管理 Mapper
 */
public interface AdminActivityMapper extends BaseMapper<Activity> {

    /**
     * 分页查询活动列表（支持按标题搜索和状态筛选）
     *
     * @param keyword 搜索关键词（可为 null）
     * @param status  活动状态（可为 null，null 则查全部）
     * @param limit   每页数量
     * @param offset  偏移量
     * @return 活动列表
     */
    @Select("<script>" +
            "SELECT a.activity_id, a.title, a.description, a.images, " +
            "  a.location_name, a.location_address, a.category_ids, " +
            "  a.start_time, a.end_time, a.max_participants, a.payment_type, " +
            "  a.status, a.created_at, " +
            "  u.user_id AS initiator_id, u.nickname AS initiator_nickname, u.avatar_url AS initiator_avatar, " +
            "  (SELECT COUNT(*) FROM participants p WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants " +
            "FROM activities a " +
            "LEFT JOIN users u ON a.initiator_id = u.user_id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND a.title LIKE CONCAT('%', #{keyword}, '%')" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND a.status = #{status}" +
            "  </if>" +
            "  <if test='userId != null'>" +
            "    AND a.initiator_id = #{userId}" +
            "  </if>" +
            "</where>" +
            "ORDER BY a.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<ActivityVO> findActivitiesPage(String keyword, Integer status, Long userId, int limit, int offset);

    /**
     * 统计活动总数（支持关键词和状态过滤）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM activities " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND title LIKE CONCAT('%', #{keyword}, '%')" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND status = #{status}" +
            "  </if>" +
            "  <if test='userId != null'>" +
            "    AND initiator_id = #{userId}" +
            "  </if>" +
            "</where>" +
            "</script>")
    long countActivities(String keyword, Integer status, Long userId);

    /**
     * 根据活动ID查询详情
     *
     * @param activityId 活动ID
     * @return 活动详情（包含发起人信息）
     */
    @Select("SELECT a.activity_id, a.title, a.description, a.images, " +
            "  a.location_name, a.location_address, a.category_ids, " +
            "  a.start_time, a.end_time, a.max_participants, a.payment_type, " +
            "  a.status, a.created_at, " +
            "  u.user_id AS initiator_id, u.nickname AS initiator_nickname, u.avatar_url AS initiator_avatar, " +
            "  u.credit_score AS initiator_credit_score, " +
            "  (SELECT COUNT(*) FROM participants p WHERE p.activity_id = a.activity_id AND p.status = 1) AS current_participants " +
            "FROM activities a " +
            "LEFT JOIN users u ON a.initiator_id = u.user_id " +
            "WHERE a.activity_id = #{activityId}")
    ActivityVO findActivityById(Long activityId);

    /**
     * 更新活动状态（下架/恢复）
     *
     * @param activityId 活动ID
     * @param status     状态: 0-招募中, 1-已满员, 2-已结束, 3-已取消
     */
    @Update("UPDATE activities SET status = #{status} WHERE activity_id = #{activityId}")
    int updateActivityStatus(Long activityId, Integer status);
}
