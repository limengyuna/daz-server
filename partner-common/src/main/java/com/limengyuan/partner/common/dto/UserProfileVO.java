package com.limengyuan.partner.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户公开信息 VO - 用于查看其他用户信息时返回
 * 不包含密码、登录账号等敏感字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /**
     * 用户ID
     */
    private Long userId;

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
     * 注册时间
     */
    private LocalDateTime createdAt;
}
