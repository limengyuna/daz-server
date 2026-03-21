package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 旅行回忆视频脚本 VO
 * <p>
 * 由 DeepSeek 大模型生成结构化视频脚本，前端根据此脚本渲染动画播放效果。
 * 图片不由 AI 生成，而是复用活动已有图片，AI 只返回图片索引（imageIndex）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelMemoryVO {

    /**
     * 视频标题（如"一起去看海的日子"）
     */
    private String title;

    /**
     * 副标题（如"2024年3月 · 成都 · 4人同行"）
     */
    private String subtitle;

    /**
     * 场景列表 — 每个场景对应视频中的一幕
     */
    private List<Scene> scenes;

    /**
     * 可用图片 URL 列表（从活动数据中提取，供前端按 imageIndex 取用）
     */
    private List<String> images;

    /**
     * 参与者信息列表（头像 + 昵称，用于片尾展示）
     */
    private List<Member> members;

    /**
     * 总花费摘要（如"￥356"，无账单时为 null）
     */
    private String totalExpense;

    // ==================== 内部结构 ====================

    /**
     * AI 生成的单个场景（视频分镜）
     *
     * @param text            旁白文字（1-3句话）
     * @param imageIndex      配图索引（对应 images 列表的下标，-1 表示无配图）
     * @param animation       动画类型建议：zoom_in（缓慢放大）/ pan_left（左移）/ fade_in（淡入）
     * @param durationSeconds 场景持续秒数（建议3-5秒）
     */
    public record Scene(
            String text,
            int imageIndex,
            String animation,
            int durationSeconds
    ) {}

    /**
     * 参与者信息（用于片尾"感谢同行"展示）
     *
     * @param nickname  昵称
     * @param avatarUrl 头像URL
     */
    public record Member(
            String nickname,
            String avatarUrl
    ) {}
}
