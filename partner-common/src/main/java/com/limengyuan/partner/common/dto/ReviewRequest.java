package com.limengyuan.partner.common.dto;

import lombok.Data;

/**
 * 审核申请请求
 */
@Data
public class ReviewRequest {

    /**
     * 审核动作: "approve" 或 "reject"
     */
    private String action;

    /**
     * 判断是否是通过操作
     */
    public boolean isApprove() {
        return "approve".equalsIgnoreCase(action);
    }

    /**
     * 判断是否是拒绝操作
     */
    public boolean isReject() {
        return "reject".equalsIgnoreCase(action);
    }
}
