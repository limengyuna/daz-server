package com.limengyuan.partner.common.dto;

import lombok.Data;

/**
 * 申请加入活动请求
 */
@Data
public class JoinActivityRequest {

    /**
     * 申请留言 (可选，如: "我有空，离得近")
     */
    private String applyMsg;
}
