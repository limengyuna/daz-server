package com.limengyuan.partner.post.service;

import com.limengyuan.partner.post.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 活动自动过期定时任务
 *
 * 职责：每小时扫描一次，将 end_time 已过但 status 仍为招募中(0)或已满员(1)的活动
 * 自动标记为已结束(status=2)，并从 Milvus 向量索引中移除
 */
@Slf4j
@Component
public class ActivityExpireTask {

    private final ActivityMapper activityMapper;
    private final ActivityVectorService activityVectorService;

    public ActivityExpireTask(ActivityMapper activityMapper,
                              ActivityVectorService activityVectorService) {
        this.activityMapper = activityMapper;
        this.activityVectorService = activityVectorService;
    }

    /**
     * 每小时执行一次，自动结束过期活动
     */
    @Scheduled(fixedRate = 3600000)  // 每 1 小时执行一次
    public void expireActivities() {
        try {
            // 1. 查询已过期但状态未更新的活动
            List<Long> expiredIds = activityMapper.findExpiredActivityIds();

            if (expiredIds == null || expiredIds.isEmpty()) {
                log.debug("[活动过期] 无需处理的过期活动");
                return;
            }

            // 2. 批量更新 MySQL 状态为已结束(status=2)
            int updated = activityMapper.batchUpdateStatusToEnded(expiredIds);
            log.info("[活动过期] 已将 {} 条过期活动标记为已结束", updated);

            // 3. 从 Milvus 向量索引中移除，不再参与推荐
            activityVectorService.removeActivities(expiredIds);
            log.info("[活动过期] 已从 Milvus 移除 {} 条过期活动向量", expiredIds.size());

        } catch (Exception e) {
            log.error("[活动过期] 定时任务执行失败", e);
        }
    }
}
