package com.limengyuan.partner.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow {
    
    /**
     * 关注记录ID
     */
    private Long followId;
    
    /**
     * 关注者ID (谁关注了)
     */
    private Long followerId;
    
    /**
     * 被关注者ID (被谁关注)
     */
    private Long followeeId;
    
    /**
     * 关注时间
     */
    private LocalDateTime createdAt;
}
