package com.limengyuan.partner.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量数据定时同步任务
 *
 * 职责：
 * 1. 服务启动时，从 MySQL 全量同步招募中的活动到 Milvus
 * 2. 每小时定时执行一次全量同步，作为兜底保障
 *    确保 Milvus 中的数据与 MySQL 保持一致（清理脏数据、补充遗漏数据）
 */
@Slf4j
@Component
public class VectorSyncTask {

    /** 全量同步时每次查询的最大数量 */
    private static final int SYNC_BATCH_SIZE = 500;

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
     * 每两天定时执行全量同步（兜底保障）
     * 确保因网络抖动或异常导致的数据不一致能被修复
     */
    @Scheduled(fixedRate = 172800000)  // 每两天执行一次（48小时）
    public void scheduledSync() {
        log.info("[向量同步] 定时任务触发，开始全量同步活动向量...");
        fullSync();
    }

    private void fullSync() {
        try {
            // 1. 同步增加/覆盖招募中的活动
            // 查询所有招募中的活动（status=0）
            List<ActivityVO> recruitingActivities = activityMapper.findRecruitingActivities(SYNC_BATCH_SIZE);

            if (recruitingActivities != null && !recruitingActivities.isEmpty()) {
                // 批量写入 Milvus（相同 ID 会覆盖，无需先删后增）
                activityVectorService.addActivities(recruitingActivities);
                log.info("[向量同步] 全量同步写入完成，共同步 {} 条活动向量", recruitingActivities.size());
            } else {
                log.info("[向量同步] 当前无招募中的活动，跳过写入");
            }

            // 2. 校验并清理不在招募状态的脏数据
            // 查询所有非招募状态的活动（status != 0）
            QueryWrapper<Activity> wrapper = new QueryWrapper<>();
            wrapper.ne("status", 0).select("activity_id");
            List<Activity> expiredList = activityMapper.selectList(wrapper);

            if (expiredList != null && !expiredList.isEmpty()) {
                List<Long> expiredIds = expiredList.stream()
                        .map(Activity::getActivityId)
                        .toList();
                // 批量从 Milvus 中删除，防止因手动下架等原因遗留脏数据
                activityVectorService.removeActivities(expiredIds);
                log.info("[向量同步] 脏数据清理完成，共移除 {} 条非招募状态的活动向量", expiredIds.size());
            }

        } catch (Exception e) {
            log.error("[向量同步] 全量同步或清理失败", e);
        }
    }
}
