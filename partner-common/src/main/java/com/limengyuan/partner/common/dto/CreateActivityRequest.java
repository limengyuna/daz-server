package com.limengyuan.partner.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布活动请求 DTO
 * 注意: 不包含 latitude、longitude 参数，这些由后端处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {

    /**
     * 发起人ID
     */
    @NotNull(message = "发起人ID不能为空")
    private Long initiatorId;

    /**
     * 分类ID列表（支持多选）
     */
    @NotNull(message = "分类不能为空")
    @Size(min = 1, message = "至少选择一个分类")
    private List<Integer> categoryIds;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 详细描述/要求
     */
    @NotBlank(message = "描述不能为空")
    private String description;

    /**
     * 活动配图URL列表
     */
    private List<String> images;

    /**
     * 地点名称
     */
    @NotBlank(message = "地点名称不能为空")
    private String locationName;

    /**
     * 详细地址
     */
    private String locationAddress;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;
    /**
     * 活动结束时间
     */    
    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;
    /**
     * 报名截止时间
     */   
    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime registrationEndTime;

    /**
     * 最大参与人数 (含发起人), 默认2
     */
    private Integer maxParticipants;

    /**
     * 费用方式: 1-AA制, 2-发起人请客, 3-免费, 4-各付各的, 默认1
     */
    private Integer paymentType;
}
