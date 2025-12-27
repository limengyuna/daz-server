package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动分类实体类 - 对应 categories 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;
}
