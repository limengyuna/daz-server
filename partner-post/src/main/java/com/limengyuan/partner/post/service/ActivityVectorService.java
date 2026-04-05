package com.limengyuan.partner.post.service;

import com.limengyuan.partner.common.dto.vo.ActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 活动向量管理服务 - 负责活动数据在 Milvus 向量数据库中的增删查
 *
 * 核心职责：
 * 1. 活动创建时，将活动文本向量化并存入 Milvus
 * 2. 活动状态变更（满员/结束/取消）时，从 Milvus 删除
 * 3. 推荐时，根据用户画像做语义相似度搜索，召回最匹配的候选活动
 */
@Slf4j
@Service
public class ActivityVectorService {

    private final VectorStore vectorStore;

    public ActivityVectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 将活动写入向量数据库
     * 将活动的标题、描述、分类、地点等文本拼接后向量化存入 Milvus
     *
     * @param activity 活动信息
     */
    public void addActivity(ActivityVO activity) {
        try {
            String text = buildActivityText(activity);
            // 使用 activityId 作为文档 ID，便于后续删除
            String docId = String.valueOf(activity.getActivityId());

            Document document = new Document(docId, text, Map.of(
                    "activityId", activity.getActivityId(),
                    "title", activity.getTitle() != null ? activity.getTitle() : "",
                    "locationName", activity.getLocationName() != null ? activity.getLocationName() : ""
            ));

            vectorStore.add(List.of(document));
            log.info("[向量索引] 活动已写入 Milvus, activityId={}, title={}", 
                    activity.getActivityId(), activity.getTitle());
        } catch (Exception e) {
            log.error("[向量索引] 写入 Milvus 失败, activityId={}", activity.getActivityId(), e);
        }
    }

    /**
     * 批量将活动写入向量数据库（用于全量同步）
     *
     * @param activities 活动列表
     */
    public void addActivities(List<ActivityVO> activities) {
        if (activities == null || activities.isEmpty()) {
            return;
        }
        try {
            List<Document> documents = activities.stream()
                    .map(activity -> {
                        String text = buildActivityText(activity);
                        String docId = String.valueOf(activity.getActivityId());
                        return new Document(docId, text, Map.of(
                                "activityId", activity.getActivityId(),
                                "title", activity.getTitle() != null ? activity.getTitle() : "",
                                "locationName", activity.getLocationName() != null ? activity.getLocationName() : ""
                        ));
                    })
                    .toList();

            vectorStore.add(documents);
            log.info("[向量索引] 批量写入 Milvus 成功, 数量={}", documents.size());
        } catch (Exception e) {
            log.error("[向量索引] 批量写入 Milvus 失败, 数量={}", activities.size(), e);
        }
    }

    /**
     * 从向量数据库中删除指定活动
     * 当活动满员、结束或取消时调用
     *
     * @param activityId 活动ID
     */
    public void removeActivity(Long activityId) {
        try {
            String docId = String.valueOf(activityId);
            vectorStore.delete(List.of(docId));
            log.info("[向量索引] 活动已从 Milvus 删除, activityId={}", activityId);
        } catch (Exception e) {
            log.error("[向量索引] 从 Milvus 删除失败, activityId={}", activityId, e);
        }
    }

    /**
     * 批量删除向量（用于定时同步清理）
     *
     * @param activityIds 活动ID列表
     */
    public void removeActivities(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return;
        }
        try {
            List<String> docIds = activityIds.stream()
                    .map(String::valueOf)
                    .toList();
            vectorStore.delete(docIds);
            log.info("[向量索引] 批量删除 Milvus 成功, 数量={}", docIds.size());
        } catch (Exception e) {
            log.error("[向量索引] 批量删除 Milvus 失败, 数量={}", activityIds.size(), e);
        }
    }

    /**
     * 根据用户画像进行语义相似度搜索，召回最匹配的候选活动 ID
     *
     * @param userProfileText 用户画像文本（如："兴趣:户外,骑行 城市:杭州"）
     * @param topK            返回的最大结果数量
     * @return 匹配的活动 ID 列表（按相似度从高到低排序）
     */
    public List<Long> searchSimilar(String userProfileText, int topK) {
        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(userProfileText)
                            .topK(topK)
                            .build()
            );

            if (results == null || results.isEmpty()) {
                log.info("[向量召回] 未找到匹配的活动, query={}", userProfileText);
                return Collections.emptyList();
            }

            List<Long> activityIds = results.stream()
                    .map(doc -> {
                        Object idObj = doc.getMetadata().get("activityId");
                        if (idObj instanceof Number) {
                            return ((Number) idObj).longValue();
                        }
                        return Long.parseLong(doc.getId());
                    })
                    .toList();

            log.info("[向量召回] 召回活动数量={}, query={}", activityIds.size(), userProfileText);
            return activityIds;
        } catch (Exception e) {
            log.error("[向量召回] Milvus 搜索失败, query={}", userProfileText, e);
            return Collections.emptyList();
        }
    }

    /**
     * 拼接活动的文本摘要（用于向量化）
     * 只包含影响推荐匹配的语义信息，不包含图片/时间等结构化字段
     */
    private String buildActivityText(ActivityVO activity) {
        StringBuilder sb = new StringBuilder();
        if (activity.getTitle() != null) {
            sb.append(activity.getTitle()).append(" ");
        }
        if (activity.getDescription() != null) {
            sb.append(activity.getDescription()).append(" ");
        }
        if (activity.getCategoryIds() != null) {
            sb.append("分类:").append(activity.getCategoryIds()).append(" ");
        }
        if (activity.getLocationName() != null) {
            sb.append("地点:").append(activity.getLocationName());
        }
        return sb.toString().trim();
    }
}
