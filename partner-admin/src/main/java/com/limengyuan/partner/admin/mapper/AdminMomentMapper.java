package com.limengyuan.partner.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.entity.Moment;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员端 - 动态管理 Mapper
 */
public interface AdminMomentMapper extends BaseMapper<Moment> {

    /**
     * 分页查询动态列表（支持按内容搜索和状态筛选）
     *
     * @param keyword 搜索关键词（可为 null）
     * @param status  动态状态（可为 null，null 则查全部）
     * @param limit   每页数量
     * @param offset  偏移量
     * @return 动态列表
     */
    @Select("<script>" +
            "SELECT m.moment_id, m.content, m.images, m.location_name, m.location_address, " +
            "  m.visibility, m.like_count, m.comment_count, m.view_count, " +
            "  m.status, m.created_at, " +
            "  u.user_id, u.nickname AS user_nickname, u.avatar_url AS user_avatar, " +
            "  u.credit_score AS user_credit_score " +
            "FROM moments m " +
            "LEFT JOIN users u ON m.user_id = u.user_id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND m.content LIKE CONCAT('%', #{keyword}, '%')" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND m.status = #{status}" +
            "  </if>" +
            "  <if test='userId != null'>" +
            "    AND m.user_id = #{userId}" +
            "  </if>" +
            "</where>" +
            "ORDER BY m.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<MomentVO> findMomentsPage(String keyword, Integer status, Long userId, int limit, int offset);

    /**
     * 统计动态总数（支持关键词和状态过滤）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM moments " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND content LIKE CONCAT('%', #{keyword}, '%')" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND status = #{status}" +
            "  </if>" +
            "  <if test='userId != null'>" +
            "    AND user_id = #{userId}" +
            "  </if>" +
            "</where>" +
            "</script>")
    long countMoments(String keyword, Integer status, Long userId);

    /**
     * 根据动态ID查询详情
     *
     * @param momentId 动态ID
     * @return 动态详情（包含发布者信息）
     */
    @Select("SELECT m.moment_id, m.content, m.images, m.location_name, m.location_address, " +
            "  m.visibility, m.like_count, m.comment_count, m.view_count, " +
            "  m.status, m.created_at, " +
            "  u.user_id, u.nickname AS user_nickname, u.avatar_url AS user_avatar, " +
            "  u.credit_score AS user_credit_score " +
            "FROM moments m " +
            "LEFT JOIN users u ON m.user_id = u.user_id " +
            "WHERE m.moment_id = #{momentId}")
    MomentVO findMomentById(Long momentId);

    /**
     * 更新动态状态（屏蔽/恢复）
     *
     * @param momentId 动态ID
     * @param status   状态: 0-已删除, 1-正常, 2-审核屏蔽
     */
    @Update("UPDATE moments SET status = #{status} WHERE moment_id = #{momentId}")
    int updateMomentStatus(Long momentId, Integer status);
}
