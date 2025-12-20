package com.limengyuan.partner.common.dto;

import lombok.Data;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {

    /**
     * 用户名/登录账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 所在城市
     */
    private String city;
}
