package com.limengyuan.partner.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发布动态请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomentRequest {

    /**
     * 发布用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 动态正文
     */
    @NotBlank(message = "动态内容不能为空")
    @Size(max = 1000, message = "动态内容不能超过1000字")
    private String content;

    /**
     * 配图URL列表 (最多9张)
     */
    @Size(max = 9, message = "配图最多9张")
    private List<String> images;

    /**
     * 地点名称
     */
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 可见范围: 0-公开(默认), 1-仅关注者, 2-仅自己
     */
    private Integer visibility;
}
