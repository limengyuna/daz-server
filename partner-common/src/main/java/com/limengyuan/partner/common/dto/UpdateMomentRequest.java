package com.limengyuan.partner.common.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 编辑动态请求 DTO - 所有字段均为可选，只传需要修改的字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMomentRequest {

    /**
     * 动态正文（可选，传则更新）
     */
    @Size(min = 1, max = 1000, message = "动态内容长度需在1-1000字之间")
    private String content;

    /**
     * 配图URL列表（可选，传则全量替换）
     */
    @Size(max = 9, message = "配图最多9张")
    private List<String> images;

    /**
     * 地点名称（可选，传则更新）
     */
    private String locationName;

    /**
     * 详细地址（可选，传则更新）
     */
    private String locationAddress;

    /**
     * 可见范围（可选，传则更新）: 0-公开, 1-仅关注者, 2-仅自己
     */
    private Integer visibility;
}
