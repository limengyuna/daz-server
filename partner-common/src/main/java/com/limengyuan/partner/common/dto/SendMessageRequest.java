package com.limengyuan.partner.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息请求 DTO
 * <p>
 * 私聊时需要指定 receiverId；群聊时需要指定 activityId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    /**
     * 接收者ID (私聊时必填)
     */
    private Long receiverId;

    /**
     * 活动ID (群聊时必填)
     */
    private Long activityId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息类型: 1-文本, 2-图片, 3-位置，默认文本
     */
    private Integer msgType = 1;
}
