package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 动态视图对象 - 包含动态信息和发布者基础信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentVO {

    // ==================== 动态基础信息 ====================

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 动态正文
     */
    private String content;

    /**
     * 配图URL列表 - JSON数组字符串
     */
    private String images;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 可见范围: 0-公开, 1-仅关注者, 2-仅自己
     */
    private Integer visibility;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 状态: 0-已删除, 1-正常, 2-审核屏蔽
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime createdAt;

    // ==================== 发布者信息 (JOIN 查询) ====================

    /**
     * 发布者昵称
     */
    private String userNickname;

    /**
     * 发布者头像
     */
    private String userAvatar;

    /**
     * 发布者信用分
     */
    private Integer userCreditScore;

    // ==================== 当前请求者状态 ====================

    /**
     * 当前用户是否已点赞 (需要业务层查询后填充)
     */
    private Boolean liked;
}
