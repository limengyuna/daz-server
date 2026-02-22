package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 动态实体类 - 对应 moments 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Moment {

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
     * 配图URL列表 - JSON数组
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
     * 点赞数 (冗余字段)
     */
    private Integer likeCount;

    /**
     * 评论数 (冗余字段)
     */
    private Integer commentCount;

    /**
     * 浏览数 (冗余字段)
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

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
