package com.limengyuan.partner.common.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户评价分页结果 - 包含分页评价列表 + 统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewPageVO {

    /**
     * 评价列表
     */
    private List<ReviewVO> list;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码 (从0开始)
     */
    private int page;

    /**
     * 每页数量
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 平均评分（保留1位小数），无评价时为 null
     */
    private Double averageScore;

    /**
     * 总评价数量
     */
    private long reviewCount;
}
