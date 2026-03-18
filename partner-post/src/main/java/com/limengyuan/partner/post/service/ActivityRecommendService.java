package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.vo.RecommendedActivityVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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

    public ActivityRecommendService(ChatClient.Builder chatClientBuilder,
                                     ActivityMapper activityMapper,
                                     ObjectMapper objectMapper,
                                     StringRedisTemplate redisTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.activityMapper = activityMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
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

        // 3. 获取所有招募中的活动作为候选集
        List<ActivityVO> candidates = activityMapper.findRecruitingActivities(CANDIDATE_LIMIT);
        if (candidates == null || candidates.isEmpty()) {
            return Result.success("暂无可推荐的活动", Collections.emptyList());
        }

        // 4. 构建候选活动摘要（给 AI 看的简化信息）
        String activitiesSummary = buildActivitiesSummary(candidates);

        // 5. 构造 Prompt
        String prompt = buildPrompt(userTags, userCity, activitiesSummary);

        // 6. 调用 DeepSeek AI
        String aiResponse;
        try {
            aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("[AI推荐] 用户ID={}, AI返回结果: {}", userId, aiResponse);
        } catch (Exception e) {
            log.error("[AI推荐] 调用 DeepSeek API 失败, userId={}", userId, e);
            return Result.error("AI 推荐服务暂时不可用，请稍后再试");
        }

        // 7. 解析 AI 返回结果，组装推荐列表
        List<RecommendedActivityVO> result = parseAIResponse(aiResponse, candidates);

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
        return String.format("""
                你是一个活动推荐助手。请根据用户的兴趣标签和所在城市，从候选活动中推荐最合适的活动。
                
                ## 用户信息
                - 兴趣标签: %s
                - 所在城市: %s
                
                ## 候选活动列表
                %s
                
                ## 要求
                1. 从候选活动中选出最适合该用户的 5~10 个活动
                2. 按推荐优先级从高到低排列
                3. 每个推荐附带简短的推荐理由（一句话即可）
                4. 必须严格按照以下 JSON 格式返回，不要包含任何其他内容：
                
                ```json
                [
                  {"activityId": 活动ID, "reason": "推荐理由"},
                  {"activityId": 活动ID, "reason": "推荐理由"}
                ]
                ```
                
                注意：只返回 JSON 数组，不要有任何多余的文字说明。
                """,
                userTags != null ? userTags : "无",
                userCity != null ? userCity : "未知",
                activitiesSummary
        );
    }

    /**
     * 解析 AI 返回的 JSON 结果，匹配候选活动组装最终列表
     */
    private List<RecommendedActivityVO> parseAIResponse(String aiResponse, List<ActivityVO> candidates) {
        // 提取 JSON 内容（AI 可能会返回 markdown 代码块包裹的 JSON）
        String jsonContent = extractJson(aiResponse);

        try {
            List<Map<String, Object>> recommendations = objectMapper.readValue(
                    jsonContent, new TypeReference<List<Map<String, Object>>>() {});

            // 将候选活动按 ID 建立索引
            Map<Long, ActivityVO> activityMap = candidates.stream()
                    .collect(Collectors.toMap(ActivityVO::getActivityId, a -> a));

            List<RecommendedActivityVO> result = new ArrayList<>();
            for (Map<String, Object> rec : recommendations) {
                Long activityId = Long.valueOf(rec.get("activityId").toString());
                String reason = (String) rec.get("reason");

                ActivityVO activity = activityMap.get(activityId);
                if (activity != null) {
                    result.add(RecommendedActivityVO.builder()
                            .activity(activity)
                            .reason(reason)
                            .build());
                }
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("[AI推荐] 解析 AI 返回结果失败: {}", aiResponse, e);
            // 解析失败时，返回原始候选列表（无推荐理由）
            return candidates.stream()
                    .limit(5)
                    .map(a -> RecommendedActivityVO.builder()
                            .activity(a)
                            .reason("为你推荐")
                            .build())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 从 AI 返回中提取 JSON 内容（处理 markdown 代码块包裹的情况）
     */
    private String extractJson(String response) {
        if (response == null) return "[]";
        String trimmed = response.trim();
        // 处理 ```json ... ``` 格式
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }
        // 处理 ``` ... ``` 格式
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("\n") + 1;
            int end = trimmed.lastIndexOf("```");
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }
        return trimmed;
    }

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
