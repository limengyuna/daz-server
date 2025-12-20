package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应 users 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名/登录账号
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String passwordHash;

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

    /**
     * 信用分/靠谱值
     */
    private Integer creditScore;

    /**
     * 状态: 0-封禁, 1-正常
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
