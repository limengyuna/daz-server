package com.limengyuan.partner.common.dto;

import lombok.Data;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {

    /**
     * 用户名/登录账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
