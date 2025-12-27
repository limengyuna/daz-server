package com.limengyuan.partner.common.dto;

import com.limengyuan.partner.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取当前用户响应（支持 Token 刷新）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeResponse {

    /**
     * 用户信息
     */
    private User user;

    /**
     * 新 Token（仅当需要刷新时返回，前端需更新本地存储）
     */
    private String newToken;
}
