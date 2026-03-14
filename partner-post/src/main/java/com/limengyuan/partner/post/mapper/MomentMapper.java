package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.dto.vo.MomentCommentVO;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.entity.Moment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 动态数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - insert(entity)       → 插入动态（自动回填 momentId）
 * - selectById(id)       → 根据ID查询
 *
 * 本 Mapper 同时操作 moments、moment_likes、moment_comments 三张表
 */
@Mapper
public interface MomentMapper extends BaseMapper<Moment> {

    // ==================== moments 表查询 ====================

    /**
     * 根据ID查询动态（含发布者信息）
     */
    @Select("""
            SELECT m.*,
                   u.nickname  AS user_nickname,
                   u.avatar_url AS user_avatar,
                   u.credit_score AS user_credit_score
            FROM moments m
            LEFT JOIN users u ON m.user_id = u.user_id
            WHERE m.moment_id = #{momentId} AND m.status = 1
            """)
    MomentVO findByIdWithUser(@Param("momentId") Long momentId);

    /**
     * 分页查询所有公开动态（含发布者信息）
     */
    @Select("""
            SELECT m.*,
                   u.nickname   AS user_nickname,
                   u.avatar_url AS user_avatar,
                   u.credit_score AS user_credit_score
            FROM moments m
            LEFT JOIN users u ON m.user_id = u.user_id
            WHERE m.status = 1 AND m.visibility = 0
            ORDER BY m.created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<MomentVO> findAllPublicWithUser(@Param("size") int size, @Param("offset") long offset);

    /**
     * 统计所有公开动态数量
     */
    @Select("SELECT COUNT(*) FROM moments WHERE status = 1 AND visibility = 0")
    long countAllPublic();

    /**
     * 查询指定用户发布的动态列表（含发布者信息）
     */
    @Select("""
            SELECT m.*,
                   u.nickname   AS user_nickname,
                   u.avatar_url AS user_avatar,
                   u.credit_score AS user_credit_score
            FROM moments m
            LEFT JOIN users u ON m.user_id = u.user_id
            WHERE m.user_id = #{userId} AND m.status = 1
            ORDER BY m.created_at DESC
            """)
    List<MomentVO> findByUserIdWithUser(@Param("userId") Long userId);

    // ==================== moments 表更新 ====================

    /**
     * 浏览数 +1
     */
    @Update("UPDATE moments SET view_count = view_count + 1 WHERE moment_id = #{momentId}")
    void incrementViewCount(@Param("momentId") Long momentId);

    /**
     * 点赞数 +1
     */
    @Update("UPDATE moments SET like_count = like_count + 1 WHERE moment_id = #{momentId}")
    void incrementLikeCount(@Param("momentId") Long momentId);

    /**
     * 点赞数 -1（最小为0）
     */
    @Update("UPDATE moments SET like_count = GREATEST(like_count - 1, 0) WHERE moment_id = #{momentId}")
    void decrementLikeCount(@Param("momentId") Long momentId);

    /**
     * 评论数 +1
     */
    @Update("UPDATE moments SET comment_count = comment_count + 1 WHERE moment_id = #{momentId}")
    void incrementCommentCount(@Param("momentId") Long momentId);

    /**
     * 软删除动态（只有发布者本人可以删除）
     */
    @Update("UPDATE moments SET status = 0 WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int deleteSoft(@Param("momentId") Long momentId, @Param("userId") Long userId);

    // ==================== moment_likes 表操作 ====================

    /**
     * 插入点赞记录
     */
    @Insert("INSERT IGNORE INTO moment_likes (moment_id, user_id) VALUES (#{momentId}, #{userId})")
    void insertLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    /**
     * 删除点赞记录
     */
    @Delete("DELETE FROM moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    int deleteLike(@Param("momentId") Long momentId, @Param("userId") Long userId);

    /**
     * 查询当前用户是否已点赞
     */
    @Select("SELECT COUNT(*) FROM moment_likes WHERE moment_id = #{momentId} AND user_id = #{userId}")
    Long existsLikeCount(@Param("momentId") Long momentId, @Param("userId") Long userId);

    // ==================== moment_comments 表操作 ====================

    /**
     * 插入评论，返回生成主键（通过 @Options 设置 useGeneratedKeys）
     */
    @Insert("""
            INSERT INTO moment_comments (moment_id, user_id, parent_id, reply_to_id, content, status)
            VALUES (#{momentId}, #{userId}, #{parentId}, #{replyToId}, #{content}, 1)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "commentId", keyColumn = "comment_id")
    void insertComment(MomentComment momentComment);

    /**
     * 查询某动态的所有一级评论（含评论人信息）
     */
    @Select("""
            SELECT c.*,
                   u.nickname    AS user_nickname,
                   u.avatar_url  AS user_avatar
            FROM moment_comments c
            LEFT JOIN users u ON c.user_id = u.user_id
            WHERE c.moment_id = #{momentId} AND c.parent_id IS NULL AND c.status = 1
            ORDER BY c.created_at ASC
            """)
    List<MomentCommentVO> findTopCommentsByMomentId(@Param("momentId") Long momentId);

    /**
     * 查询某条一级评论下的所有回复（含评论人和被回复人信息）
     */
    @Select("""
            SELECT c.*,
                   u.nickname    AS user_nickname,
                   u.avatar_url  AS user_avatar,
                   ru.nickname   AS reply_to_nickname
            FROM moment_comments c
            LEFT JOIN users u  ON c.user_id      = u.user_id
            LEFT JOIN users ru ON c.reply_to_id  = ru.user_id
            WHERE c.parent_id = #{parentId} AND c.status = 1
            ORDER BY c.created_at ASC
            """)
    List<MomentCommentVO> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 评论参数内部类（用于 insertComment 方法的参数传递）
     */
    class MomentComment {
        private Long commentId;
        private Long momentId;
        private Long userId;
        private Long parentId;
        private Long replyToId;
        private String content;

        public MomentComment() {}

        public MomentComment(Long momentId, Long userId, Long parentId, Long replyToId, String content) {
            this.momentId = momentId;
            this.userId = userId;
            this.parentId = parentId;
            this.replyToId = replyToId;
            this.content = content;
        }

        public Long getCommentId() { return commentId; }
        public void setCommentId(Long commentId) { this.commentId = commentId; }
        public Long getMomentId() { return momentId; }
        public void setMomentId(Long momentId) { this.momentId = momentId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Long getReplyToId() { return replyToId; }
        public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
