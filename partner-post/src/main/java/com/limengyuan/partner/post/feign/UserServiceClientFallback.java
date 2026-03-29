package com.limengyuan.partner.post.feign;

import com.limengyuan.partner.common.dto.request.UpdateCreditRequest;
import com.limengyuan.partner.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 客户端降级处理
 *
 * 当 user 模块不可用时，评价功能本身不受影响，
 * 只是信誉分更新会延迟，避免因为信誉分计算失败导致评价提交也失败
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Result<Integer> recalculateCreditScore(UpdateCreditRequest request) {
        log.warn("[Feign降级] 用户服务不可用，信誉分更新延迟。userId={}, reason={}",
                request.getUserId(), request.getReason());
        return Result.error("用户服务暂时不可用，信誉分将稍后更新");
    }
}
