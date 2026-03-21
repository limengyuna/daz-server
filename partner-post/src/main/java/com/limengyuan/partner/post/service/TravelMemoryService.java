package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.vo.*;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ExpenseMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI 旅行回忆生成服务
 * <p>
 * 融合活动元数据、群聊记录、账单数据等多源信息，
 * 通过 Prompt Engineering 驱动 DeepSeek 大模型生成结构化视频脚本，
 * 前端根据脚本渲染 Ken Burns 动画和场景播放效果。
 */
@Slf4j
@Service
public class TravelMemoryService {

    /** 群聊消息采集上限 */
    private static final int CHAT_MESSAGE_LIMIT = 30;
    /** 群聊图片采集上限 */
    private static final int CHAT_IMAGE_LIMIT = 20;
    /** 缓存 key 前缀 */
    private static final String CACHE_KEY_PREFIX = "travel_memory:activity:";
    /** 缓存过期时间：24 小时 */
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    /** 活动状态：已结束 */
    private static final int ACTIVITY_STATUS_ENDED = 2;
    /** 日期格式化 */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日");

    private final ChatClient chatClient;
    private final ActivityMapper activityMapper;
    private final ParticipantMapper participantMapper;
    private final ExpenseMapper expenseMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public TravelMemoryService(ChatClient.Builder chatClientBuilder,
                                ActivityMapper activityMapper,
                                ParticipantMapper participantMapper,
                                ExpenseMapper expenseMapper,
                                ObjectMapper objectMapper,
                                StringRedisTemplate redisTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.activityMapper = activityMapper;
        this.participantMapper = participantMapper;
        this.expenseMapper = expenseMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成 AI 旅行回忆视频脚本
     *
     * @param activityId 活动ID（必须是已结束的活动）
     * @param userId     当前用户ID（必须是参与者或发起人）
     * @return 结构化视频脚本
     */
    public Result<TravelMemoryVO> generateTravelMemory(Long activityId, Long userId) {

        // ========== 1. 查 Redis 缓存 ==========
        String cacheKey = CACHE_KEY_PREFIX + activityId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("[旅行回忆] 命中缓存, activityId={}", activityId);
                TravelMemoryVO cachedResult = objectMapper.readValue(cached, TravelMemoryVO.class);
                return Result.success(cachedResult);
            }
        } catch (Exception e) {
            log.warn("[旅行回忆] 读取缓存失败, activityId={}", activityId, e);
        }

        // ========== 2. 获取活动信息 ==========
        ActivityVO activity = activityMapper.findByIdWithUser(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 校验活动已结束
        if (activity.getStatus() != ACTIVITY_STATUS_ENDED) {
            return Result.error("只有已结束的活动才能生成旅行回忆");
        }

        // ========== 3. 获取参与者列表 ==========
        List<ParticipantVO> participants = participantMapper.findApprovedByActivityIdWithUser(activityId);

        // 校验当前用户是参与者或发起人
        boolean isMember = activity.getInitiatorId().equals(userId)
                || participants.stream().anyMatch(p -> p.getUserId().equals(userId));
        if (!isMember) {
            return Result.error("只有活动参与者才能生成旅行回忆");
        }

        // 构建参与者昵称列表（含发起人）
        List<String> memberNames = new ArrayList<>();
        memberNames.add(activity.getInitiatorNickname()); // 发起人
        participants.forEach(p -> memberNames.add(p.getNickname()));

        // 构建 Member 列表（用于前端展示）
        List<TravelMemoryVO.Member> members = new ArrayList<>();
        members.add(new TravelMemoryVO.Member(
                activity.getInitiatorNickname(), activity.getInitiatorAvatar()));
        participants.forEach(p -> members.add(new TravelMemoryVO.Member(
                p.getNickname(), p.getAvatarUrl())));

        // ========== 4. 获取群聊记录 ==========
        List<ChatMessageVO> chatMessages = activityMapper.findGroupTextMessages(activityId, CHAT_MESSAGE_LIMIT);
        String chatSummary = buildChatSummary(chatMessages);

        // ========== 5. 获取账单数据 ==========
        List<ExpenseVO> expenses = expenseMapper.findByActivityIdWithPayer(activityId);
        BigDecimal totalAmount = expenseMapper.sumAmountByActivityId(activityId);
        String expenseSummary = buildExpenseSummary(expenses, totalAmount);

        // ========== 6. 多源图片采集（活动图片 + 群聊图片 + 账单凭证图片）==========
        List<String> images = new ArrayList<>();

        // 6.1 活动封面图片
        images.addAll(parseImages(activity.getImages()));

        // 6.2 群聊中发送的图片消息（msg_type=2，content 即图片 URL）
        List<String> chatImageUrls = activityMapper.findGroupImageUrls(activityId, CHAT_IMAGE_LIMIT);
        if (chatImageUrls != null) {
            images.addAll(chatImageUrls);
        }

        // 6.3 账单凭证/小票图片
        for (ExpenseVO expense : expenses) {
            images.addAll(parseImages(expense.getImages()));
        }

        int imageCount = images.size();
        log.info("[旅行回忆] activityId={}, 图片池大小: {}（活动+群聊+账单）", activityId, imageCount);

        // ========== 7. 构造 Prompt ==========
        String prompt = buildPrompt(activity, memberNames, chatSummary, expenseSummary, imageCount);

        // ========== 8. 调用 DeepSeek AI ==========
        List<AiScene> aiScenes;
        try {
            aiScenes = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(new org.springframework.core.ParameterizedTypeReference<List<AiScene>>() {});
            log.info("[旅行回忆] activityId={}, AI返回场景数: {}", activityId,
                    aiScenes != null ? aiScenes.size() : 0);
        } catch (Exception e) {
            log.error("[旅行回忆] 调用 DeepSeek API 失败, activityId={}", activityId, e);
            return Result.error("AI 生成服务暂时不可用，请稍后再试");
        }

        // ========== 9. 组装最终结果 ==========
        List<TravelMemoryVO.Scene> scenes = new ArrayList<>();
        if (aiScenes != null) {
            for (int i = 0; i < aiScenes.size(); i++) {
                AiScene aiScene = aiScenes.get(i);
                // 确保每个场景都有配图：越界时循环复用图片
                int safeIndex;
                if (imageCount > 0) {
                    safeIndex = (aiScene.imageIndex() >= 0 && aiScene.imageIndex() < imageCount)
                            ? aiScene.imageIndex()
                            : i % imageCount; // 循环复用
                } else {
                    safeIndex = -1; // 完全没有图片时才允许 -1
                }
                scenes.add(new TravelMemoryVO.Scene(
                        aiScene.text(),
                        safeIndex,
                        aiScene.animation() != null ? aiScene.animation() : "fade_in",
                        aiScene.durationSeconds() > 0 ? aiScene.durationSeconds() : 4
                ));
            }
        }

        // 如果 AI 返回为空，生成兜底内容
        if (scenes.isEmpty()) {
            scenes.add(new TravelMemoryVO.Scene(
                    "一段美好的旅程，留下了珍贵的回忆。", 0, "zoom_in", 5));
        }

        // 构建副标题
        String subtitle = buildSubtitle(activity, members.size());

        TravelMemoryVO result = TravelMemoryVO.builder()
                .title(activity.getTitle())
                .subtitle(subtitle)
                .scenes(scenes)
                .images(images)
                .members(members)
                .totalExpense(totalAmount.compareTo(BigDecimal.ZERO) > 0
                        ? "￥" + totalAmount.setScale(2).toPlainString() : null)
                .build();

        // ========== 10. 写入 Redis 缓存 ==========
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
            log.info("[旅行回忆] 缓存已写入, activityId={}, TTL=24h", activityId);
        } catch (Exception e) {
            log.warn("[旅行回忆] 写入缓存失败, activityId={}", activityId, e);
        }

        return Result.success(result);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * AI 场景的结构化输出类型
     */
    public record AiScene(
            String text,
            int imageIndex,
            String animation,
            int durationSeconds
    ) {}

    /**
     * 构建群聊摘要（给 AI 参考）
     */
    private String buildChatSummary(List<ChatMessageVO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "暂无群聊记录";
        }
        StringBuilder sb = new StringBuilder();
        for (ChatMessageVO msg : messages) {
            sb.append(String.format("%s: %s\n",
                    msg.getSenderNickname() != null ? msg.getSenderNickname() : "匿名",
                    msg.getContent()));
        }
        return sb.toString();
    }

    /**
     * 构建账单摘要（给 AI 参考）
     */
    private String buildExpenseSummary(List<ExpenseVO> expenses, BigDecimal totalAmount) {
        if (expenses == null || expenses.isEmpty()) {
            return "暂无账单记录";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("总花费: ￥%s\n", totalAmount.setScale(2).toPlainString()));
        for (ExpenseVO expense : expenses) {
            sb.append(String.format("- %s 垫付了「%s」￥%s\n",
                    expense.getPayerNickname(),
                    expense.getTitle(),
                    expense.getAmount().setScale(2).toPlainString()));
        }
        return sb.toString();
    }

    /**
     * 解析活动图片 JSON 数组为 URL 列表
     */
    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("[旅行回忆] 解析图片JSON失败: {}", imagesJson, e);
            return List.of();
        }
    }

    /**
     * 构建副标题
     */
    private String buildSubtitle(ActivityVO activity, int memberCount) {
        StringBuilder sb = new StringBuilder();
        if (activity.getStartTime() != null) {
            sb.append(activity.getStartTime().format(DATE_FMT));
        }
        if (activity.getLocationName() != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(activity.getLocationName());
        }
        sb.append(" · ").append(memberCount).append("人同行");
        return sb.toString();
    }

    /**
     * 构造发送给 AI 的 Prompt
     */
    private String buildPrompt(ActivityVO activity, List<String> memberNames,
                                String chatSummary, String expenseSummary, int imageCount) {
        return String.format("""
                你是一个温暖的旅行回忆故事作家。请根据以下活动数据，生成一段感人的旅行回忆视频脚本。
                
                ## 活动信息
                - 标题：%s
                - 地点：%s
                - 时间：%s 至 %s
                - 参与者：%s
                
                ## 群聊精选（大家的真实对话）
                %s
                
                ## 账单数据
                %s
                
                ## 可用配图数量：%d 张（包含活动封面、群聊照片、账单凭证）
                
                ## 要求
                1. 生成 4~6 个场景，每个场景是视频中的一幕
                2. 每个场景包含 1-3 句旁白文字，要温暖、有画面感
                3. 自然地融入群聊中有趣或温馨的对话片段（直接引用）
                4. 如果有账单数据，巧妙地融入花费细节（如"XX大方地请了一顿火锅"）
                5. 每个场景必须配图！imageIndex 从 0 到 %d，表示配第几张图片。当图片不够时，可以重复使用同一张图片
                6. animation 从以下选择：zoom_in（缓慢放大）、pan_left（左移）、fade_in（淡入）
                7. durationSeconds 每个场景建议 3-5 秒
                8. 第一个场景作为开头（如"那一天..."），最后一个场景作为结尾（如"期待下次再聚"）
                """,
                activity.getTitle(),
                activity.getLocationName() != null ? activity.getLocationName() : "未知",
                activity.getStartTime() != null ? activity.getStartTime().format(DATE_FMT) : "未定",
                activity.getEndTime() != null ? activity.getEndTime().format(DATE_FMT) : "未定",
                String.join("、", memberNames),
                chatSummary,
                expenseSummary,
                imageCount,
                Math.max(imageCount - 1, 0)
        );
    }
}
