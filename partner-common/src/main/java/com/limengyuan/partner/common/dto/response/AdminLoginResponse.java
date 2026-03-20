package com.limengyuan.partner.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员登录响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像链接
     */
    private String avatarUrl;

    /**
     * 角色: 1-超级管理员, 2-普通管理员
     */
    private Integer role;

    /**
     * JWT Token
     */
    private String token;
}
