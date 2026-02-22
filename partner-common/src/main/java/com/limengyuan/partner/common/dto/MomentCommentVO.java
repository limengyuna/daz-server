package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态评论视图对象 - 支持两级评论结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentCommentVO {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 所属动态ID
     */
    private Long momentId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID, NULL 表示一级评论
     */
    private Long parentId;

    /**
     * 被回复人的用户ID (用于前端显示"回复 @xxx")
     */
    private Long replyToId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态: 0-已删除, 1-正常
     */
    private Integer status;

    /**
     * 评论时间
     */
    private LocalDateTime createdAt;

    // ==================== 评论人信息 (JOIN 查询) ====================

    /**
     * 评论人昵称
     */
    private String userNickname;

    /**
     * 评论人头像
     */
    private String userAvatar;

    // ==================== 被回复人信息 (JOIN 查询) ====================

    /**
     * 被回复人昵称 (用于前端显示"回复 @xxx")
     */
    private String replyToNickname;

    // ==================== 子评论列表 (回复列表) ====================

    /**
     * 该评论下的回复列表 (仅一级评论才有, 二级评论此字段为 null)
     */
    private List<MomentCommentVO> replies;
}
