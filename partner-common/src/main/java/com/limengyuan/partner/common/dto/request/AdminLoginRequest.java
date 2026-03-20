package com.limengyuan.partner.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求体
 */
@Data
public class AdminLoginRequest {

    /**
     * 登录账号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
