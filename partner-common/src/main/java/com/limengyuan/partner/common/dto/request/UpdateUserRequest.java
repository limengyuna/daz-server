package com.limengyuan.partner.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户更新请求 DTO
 * 只包含用户可自行编辑的字段，防止通过接口篡改 creditScore、status 等受保护字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像链接
     */
    private String avatarUrl;

    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 个性签名/简介
     */
    private String bio;

    /**
     * 个人标签 - JSON格式
     */
    private String tags;
}
