package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 参与者分页返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantPageVO {

    /**
     * 参与者列表
     */
    private List<ParticipantVO> list;

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 当前页码（从0开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;
}
