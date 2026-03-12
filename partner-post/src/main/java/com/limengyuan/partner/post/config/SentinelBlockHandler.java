package com.limengyuan.partner.post.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.result.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Sentinel 全局降级处理类 - 帖子/活动服务
 * <p>
 * 当接口触发 Sentinel 限流或熔断降级时，自动调用此类中对应的处理方法，
 * 返回友好的错误提示，而不是直接抛出异常。
 */
@Slf4j
public class SentinelBlockHandler {

    /**
     * 活动相关 - 降级处理
     */
    public static Result<?> activityBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 活动接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 活动详情 - 降级处理
     */
    public static Result<?> getActivityBlockHandler(Long activityId, BlockException ex) {
        log.warn("[Sentinel] 获取活动详情接口被限流/降级, activityId={}", activityId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 动态相关 - 降级处理
     */
    public static Result<?> momentBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 动态接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 参与者相关 - 降级处理
     */
    public static Result<?> participantBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 参与者接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 分类相关 - 降级处理
     */
    public static Result<?> categoryBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 分类接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 评价相关 - 降级处理
     */
    public static Result<?> reviewBlockHandler(BlockException ex) {
        log.warn("[Sentinel] 评价接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
