package com.limengyuan.partner.user.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.result.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 全局降级处理类 - 用户服务
 * <p>
 * 当接口触发 Sentinel 限流或熔断降级时，自动调用此类中对应的处理方法，
 * 返回友好的错误提示，而不是直接抛出异常。
 */
@Slf4j
public class SentinelBlockHandler {

    /**
     * 获取用户信息 - 降级处理
     */
    public static Result<?> getUserBlockHandler(Long userId, BlockException ex) {
        log.warn("[Sentinel] 获取用户信息接口被限流/降级, userId={}", userId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取当前登录用户信息 - 降级处理
     */
    public static Result<?> getCurrentUserBlockHandler(String authHeader, String cookieToken, BlockException ex) {
        log.warn("[Sentinel] 获取当前用户信息接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取用户列表 - 降级处理
     */
    public static Result<?> listUsersBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 获取用户列表接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 聊天相关 - 降级处理
     */
    public static Result<?> chatBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 聊天接口被限流/降级", ex);
        return Result.error("聊天服务繁忙，请稍后再试");
    }

    /**
     * 认证相关 - 降级处理
     */
    public static Result<?> authBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 认证接口被限流/降级", ex);
        return Result.error("登录服务繁忙，请稍后再试");
    }
}
