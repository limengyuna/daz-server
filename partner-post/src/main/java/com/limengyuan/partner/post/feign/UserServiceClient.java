package com.limengyuan.partner.post.feign;

import com.limengyuan.partner.common.dto.request.UpdateCreditRequest;
import com.limengyuan.partner.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务 Feign 客户端
 *
 * 通过 OpenFeign 调用 partner-user 模块的内部接口
 * url 指向 user 服务地址，本地开发为 localhost:8081
 */
@FeignClient(
        name = "partner-user",
        url = "${feign.client.user-service.url:http://localhost:8081}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * 通知 user 模块重新计算指定用户的信誉分
     * 调用 user 模块的内部接口 PUT /internal/user/credit/recalculate
     */
    @PutMapping("/internal/user/credit/recalculate")
    Result<Integer> recalculateCreditScore(@RequestBody UpdateCreditRequest request);
}
