package com.limengyuan.partner.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员实体类 - 对应 admins 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admins")
public class Admin {

    /**
     * 管理员ID
     */
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String passwordHash;

    /**
     * 管理员昵称
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
     * 状态: 0-禁用, 1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}
