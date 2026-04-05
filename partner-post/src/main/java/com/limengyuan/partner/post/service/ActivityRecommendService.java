package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.vo.RecommendedActivityVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 活动推荐服务 - 基于 DeepSeek + Spring AI 实现个性化推荐
 */
@Slf4j
@Service
public class ActivityRecommendService {

    /** 候选活动最大数量 */
    private static final int CANDIDATE_LIMIT = 20;
    /** 缓存 key 前缀 */
    private static final String CACHE_KEY_PREFIX = "recommend:user:";
    /** 缓存过期时间：30 分钟 */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final ChatClient chatClient;
    private final ActivityMapper activityMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final ActivityVectorService activityVectorService;

    public ActivityRecommendService(ChatClient.Builder chatClientBuilder,
                                     ActivityMapper activityMapper,
                                     ObjectMapper objectMapper,
                                     StringRedisTemplate redisTemplate,
                                     ActivityVectorService activityVectorService) {
        this.chatClient = chatClientBuilder.build();
        this.activityMapper = activityMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.activityVectorService = activityVectorService;
    }

    /**
     * 获取 AI 推荐的活动列表
     *
     * @param userId 当前用户ID
     * @return 推荐活动列表（包含推荐理由）
     */
    public Result<List<RecommendedActivityVO>> getRecommendations(Long userId) {
        // 1. 先查 Redis 缓存
        String cacheKey = CACHE_KEY_PREFIX + userId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("[AI推荐] 命中缓存, userId={}", userId);
                List<RecommendedActivityVO> cachedResult = objectMapper.readValue(
                        cached, new TypeReference<List<RecommendedActivityVO>>() {});
                return Result.success(cachedResult);
            }
        } catch (Exception e) {
            log.warn("[AI推荐] 读取缓存失败, 将重新调用AI, userId={}", userId, e);
        }

        // 2. 获取用户画像信息
        Map<String, Object> userProfile = activityMapper.findUserTagsAndCity(userId);
        if (userProfile == null) {
            return Result.error("用户不存在");
        }

        String userTags = (String) userProfile.get("tags");
        String userCity = (String) userProfile.get("city");

        // 3. 通过 Milvus 向量相似度搜索，召回与用户画像最匹配的候选活动
        String userProfileText = String.format("兴趣:%s 城市:%s",
                userTags != null ? userTags : "无",
                userCity != null ? userCity : "未知");
        List<Long> candidateIds = activityVectorService.searchSimilar(userProfileText, CANDIDATE_LIMIT);

        List<ActivityVO> candidates;
        if (candidateIds != null && !candidateIds.isEmpty()) {
            // 向量召回成功，回 MySQL 查完整活动信息
            List<ActivityVO> rawCandidates = activityMapper.findByIds(candidateIds);
            
            // 过滤掉已经在 MySQL 中变为非招募状态的脏数据活动（保障实效性）
            candidates = rawCandidates.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus() == 0)
                    .collect(Collectors.toList());
                    
            log.info("[AI推荐] 向量召回成功, userId={}, 原始召回={}, 过滤后有效={}", 
                    userId, rawCandidates.size(), candidates.size());

            if (candidates.isEmpty()) {
                // 召回的内容全都是过期活动的情况，触发兜底
                log.warn("[AI推荐] 向量召回活动已全部过期, 回退到SQL查询, userId={}", userId);
                candidates = activityMapper.findRecruitingActivities(CANDIDATE_LIMIT);
            }
        } else {
            // 向量召回为空时，回退到 SQL 查询作为兜底
            log.warn("[AI推荐] 向量召回为空, 回退到SQL查询, userId={}", userId);
            candidates = activityMapper.findRecruitingActivities(CANDIDATE_LIMIT);
        }

        if (candidates == null || candidates.isEmpty()) {
            return Result.success("暂无可推荐的活动", Collections.emptyList());
        }

        // 4. 构建候选活动摘要（给 AI 看的简化信息）
        String activitiesSummary = buildActivitiesSummary(candidates);

        // 5. 构造 Prompt
        String prompt = buildPrompt(userTags, userCity, activitiesSummary);

        // 6. 调用 DeepSeek AI，使用 Structured Output 自动解析为 Java 对象
        List<AiRecommendation> recommendations;
        try {
            recommendations = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<AiRecommendation>>() {});
            log.info("[AI推荐] 用户ID={}, AI返回推荐数量: {}", userId,
                    recommendations != null ? recommendations.size() : 0);
        } catch (Exception e) {
            log.error("[AI推荐] 调用 DeepSeek API 失败, userId={}", userId, e);
            return Result.error("AI 推荐服务暂时不可用，请稍后再试");
        }

        // 7. 将 AI 推荐结果与候选活动匹配，组装最终列表
        Map<Long, ActivityVO> activityMap = candidates.stream()
                .collect(Collectors.toMap(ActivityVO::getActivityId, a -> a));

        List<RecommendedActivityVO> result = new ArrayList<>();
        if (recommendations != null) {
            for (AiRecommendation rec : recommendations) {
                ActivityVO activity = activityMap.get(rec.activityId());
                if (activity != null) {
                    result.add(RecommendedActivityVO.builder()
                            .activity(activity)
                            .reason(rec.reason())
                            .build());
                }
            }
        }

        // 如果 AI 返回为空，兜底返回前5个候选活动
        if (result.isEmpty()) {
            result = candidates.stream()
                    .limit(5)
                    .map(a -> RecommendedActivityVO.builder()
                            .activity(a)
                            .reason("为你推荐")
                            .build())
                    .collect(Collectors.toList());
        }

        // 8. 将结果写入 Redis 缓存（30 分钟过期）
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
            log.info("[AI推荐] 缓存已写入, userId={}, TTL=30min", userId);
        } catch (Exception e) {
            log.warn("[AI推荐] 写入缓存失败, userId={}", userId, e);
        }

        return Result.success(result);
    }

    /**
     * 构建候选活动摘要（给 AI 参考的简化信息）
     */
    private String buildActivitiesSummary(List<ActivityVO> candidates) {
        StringBuilder sb = new StringBuilder();
        for (ActivityVO activity : candidates) {
            sb.append(String.format("ID:%d | 标题:%s | 分类:%s | 地点:%s | 时间:%s | 人数:%d/%d | 费用方式:%s\n",
                    activity.getActivityId(),
                    activity.getTitle(),
                    activity.getCategoryIds(),
                    activity.getLocationName(),
                    activity.getStartTime() != null ? activity.getStartTime().toString() : "未定",
                    activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0,
                    activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0,
                    getPaymentTypeText(activity.getPaymentType())
            ));
        }
        return sb.toString();
    }

    /**
     * 构造发送给 AI 的 Prompt
     */
    private String buildPrompt(String userTags, String userCity, String activitiesSummary) {
        // Structured Output 会自动在 Prompt 中追加 JSON Schema 约束，
        // 因此不需要手动写 JSON 格式说明
        return String.format("""
                你是一个活动推荐助手。请根据用户的兴趣标签和所在城市，从候选活动中推荐最合适的活动。
                
                ## 用户信息
                - 兴趣标签: %s
                - 所在城市: %s
                
                ## 候选活动列表
                %s
                
                ## 要求
                1. 从候选活动中选出最适合该用户的 3 个活动
                2. 按推荐优先级从高到低排列
                3. 每个推荐附带简短的推荐理由（一句话即可）
                4. activityId 必须是候选活动列表中真实存在的 ID
                """,
                userTags != null ? userTags : "无",
                userCity != null ? userCity : "未知",
                activitiesSummary
        );
    }

    /**
     * AI 推荐结果的结构化输出类型
     * Spring AI 会自动将 AI 的返回解析为此 record 的列表
     *
     * @param activityId 推荐的活动ID
     * @param reason     推荐理由
     */
    public record AiRecommendation(Long activityId, String reason) {}

    /**
     * 费用方式文本转换
     */
    private String getPaymentTypeText(Integer paymentType) {
        if (paymentType == null) return "未知";
        return switch (paymentType) {
            case 1 -> "AA制";
            case 2 -> "发起人请客";
            case 3 -> "免费";
            case 4 -> "各付各的";
            default -> "未知";
        };
    }
}
