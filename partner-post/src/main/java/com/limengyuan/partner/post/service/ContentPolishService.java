package com.limengyuan.partner.post.service;

import com.limengyuan.partner.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI 内容润色服务
 * <p>
 * 基于 DeepSeek 大模型，对用户输入的文本进行智能润色优化，
 * 支持活动描述、个人介绍、动态内容等多种场景。
 */
@Slf4j
@Service
public class ContentPolishService {

    private final ChatClient chatClient;

    public ContentPolishService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * AI 内容润色
     *
     * @param content 原始文本
     * @param type    润色类型：activity（活动描述）、profile（个人介绍）、moment（动态内容）
     * @return 润色后的文本
     */
    public Result<String> polishContent(String content, String type) {
        // 参数校验
        if (content == null || content.isBlank()) {
            return Result.error("内容不能为空");
        }
        if (content.length() > 2000) {
            return Result.error("内容不能超过2000字");
        }

        // 根据类型选择润色风格
        String styleGuide = getStyleGuide(type);

        // 构造 Prompt
        String prompt = String.format("""
                你是一个文案润色助手。请对以下内容进行润色优化。
                
                ## 润色风格
                %s
                
                ## 原文
                %s
                
                ## 要求
                1. 保持原意不变，不要添加原文没有的信息
                2. 语言自然流畅，适当增加感染力
                3. 可以适当添加 emoji 表情
                4. 字数控制在原文的 1~2 倍以内
                5. 直接返回润色后的文本，不要添加任何解释或说明
                """, styleGuide, content);

        // 调用 AI
        try {
            String polished = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (polished == null || polished.isBlank()) {
                return Result.error("AI 润色返回为空，请稍后重试");
            }

            log.info("[AI润色] type={}, 原文长度={}, 润色后长度={}", type, content.length(), polished.length());
            return Result.success(polished);

        } catch (Exception e) {
            log.error("[AI润色] 调用 DeepSeek API 失败", e);
            return Result.error("AI 润色服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 根据润色类型返回对应的风格指南
     */
    private String getStyleGuide(String type) {
        if (type == null) type = "general";
        return switch (type) {
            case "activity" -> "活动描述类：突出活动亮点和吸引力，让人想参加，语气热情有活力";
            case "profile" -> "个人介绍类：展现个人特点和魅力，真诚自然，让人想认识你";
            case "moment" -> "社交动态类：生动有趣，有共鸣感，适合朋友圈风格";
            default -> "通用润色：使文本更流畅、更有感染力";
        };
    }
}
