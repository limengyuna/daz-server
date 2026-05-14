package com.limengyuan.partner.post.service;

import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 向量数据定时同步任务
 *
 * 同步策略：
 * 1. 服务启动时，执行一次全量同步（确保 Milvus 与 MySQL 完全一致）
 * 2. 每 2 小时执行一次增量同步（只查询上次同步后有变更的活动，避免全表扫描）
 *    - 状态仍为招募中(status=0)的 → 写入/覆盖 Milvus
 *    - 状态变为非招募中(status!=0)的 → 从 Milvus 删除
 */
@Slf4j
@Component
public class VectorSyncTask {

    /** 全量同步时每次查询的最大数量 */
    private static final int SYNC_BATCH_SIZE = 500;

    /** 上次同步时间（用于增量同步），初始为 null 表示未执行过 */
    private volatile LocalDateTime lastSyncTime;

    private final ActivityMapper activityMapper;
    private final ActivityVectorService activityVectorService;

    public VectorSyncTask(ActivityMapper activityMapper,
                          ActivityVectorService activityVectorService) {
        this.activityMapper = activityMapper;
        this.activityVectorService = activityVectorService;
    }

    /**
     * 服务启动完成后执行一次全量同步
     * 使用 ApplicationReadyEvent 确保所有 Bean 初始化完毕后再同步
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("[向量同步] 服务启动，开始全量同步活动向量...");
        fullSync();
    }

    /**
     * 每 2 小时执行一次增量同步
     * 只查询上次同步后有变更的活动，避免全表扫描
     */
    @Scheduled(fixedRate = 7200000)  // 每 2 小时执行一次
    public void scheduledSync() {
        if (lastSyncTime == null) {
            // 如果启动时全量同步还未完成或失败，降级为全量同步
            log.info("[向量同步] 定时任务触发，lastSyncTime 为空，执行全量同步...");
            fullSync();
        } else {
            log.info("[向量同步] 定时任务触发，执行增量同步，上次同步时间={}", lastSyncTime);
            incrementalSync();
        }
    }

    /**
     * 全量同步：将 MySQL 中所有招募中的活动写入 Milvus
     * 仅在服务启动时执行，保证 Milvus 数据完整
     */
    private void fullSync() {
        try {
            // 记录同步开始时间（在查询之前取，防止遗漏同步期间的变更）
            LocalDateTime syncStartTime = LocalDateTime.now();

            // 查询所有招募中的活动（status=0）并批量写入 Milvus
            List<ActivityVO> recruitingActivities = activityMapper.findRecruitingActivities(SYNC_BATCH_SIZE);
            if (recruitingActivities != null && !recruitingActivities.isEmpty()) {
                activityVectorService.addActivities(recruitingActivities);
                log.info("[向量同步] 全量同步写入完成，共同步 {} 条活动向量", recruitingActivities.size());
            } else {
                log.info("[向量同步] 当前无招募中的活动，跳过写入");
            }

            // 更新同步时间，后续定时任务将使用增量同步
            lastSyncTime = syncStartTime;
            log.info("[向量同步] 全量同步完成，lastSyncTime 已更新为 {}", lastSyncTime);
        } catch (Exception e) {
            log.error("[向量同步] 全量同步失败", e);
        }
    }

    /**
     * 增量同步：只查询上次同步后状态发生变更的活动
     * - 变更后仍为招募中(status=0) → 写入/覆盖 Milvus（可能是内容更新）
     * - 变更后为非招募中(status!=0) → 从 Milvus 删除
     *
     * 相比全量同步，避免了对整张 activities 表的全表扫描
     */
    private void incrementalSync() {
        try {
            LocalDateTime syncStartTime = LocalDateTime.now();

            // 只查询上次同步后有变更的活动（利用 updated_at 索引）
            List<Activity> changedActivities = activityMapper.findChangedSince(lastSyncTime);

            if (changedActivities == null || changedActivities.isEmpty()) {
                log.info("[向量同步] 增量同步：无变更数据，跳过");
                lastSyncTime = syncStartTime;
                return;
            }

            // 按状态分组：招募中的需要写入，非招募中的需要删除
            List<Long> toAddIds = changedActivities.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus() == 0)
                    .map(Activity::getActivityId)
                    .toList();

            List<Long> toRemoveIds = changedActivities.stream()
                    .filter(a -> a.getStatus() == null || a.getStatus() != 0)
                    .map(Activity::getActivityId)
                    .toList();

            // 写入招募中的活动到 Milvus
            if (!toAddIds.isEmpty()) {
                List<ActivityVO> toAddActivities = activityMapper.findByIds(toAddIds);
                activityVectorService.addActivities(toAddActivities);
                log.info("[向量同步] 增量写入 {} 条活动向量", toAddActivities.size());
            }

            // 从 Milvus 删除非招募中的活动
            if (!toRemoveIds.isEmpty()) {
                activityVectorService.removeActivities(toRemoveIds);
                log.info("[向量同步] 增量删除 {} 条非招募状态的活动向量", toRemoveIds.size());
            }

            lastSyncTime = syncStartTime;
            log.info("[向量同步] 增量同步完成，变更总数={}，写入={}，删除={}，lastSyncTime={}",
                    changedActivities.size(), toAddIds.size(), toRemoveIds.size(), lastSyncTime);
        } catch (Exception e) {
            log.error("[向量同步] 增量同步失败", e);
        }
    }
}
