package com.limengyuan.partner.post.service;

import com.limengyuan.partner.common.dto.vo.TripPlanVO;
import com.limengyuan.partner.common.entity.Category;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 行程规划服务
 * <p>
 * 用户输入一句自然语言描述（如"下月3号想去塔公草原玩两天"），
 * AI 自动生成结构化的行程方案，返回值可直接填充到前端"创建活动"表单。
 */
@Slf4j
@Service
public class TripPlanService {

    private final ChatClient chatClient;
    private final CategoryMapper categoryMapper;

    public TripPlanService(ChatClient.Builder chatClientBuilder,
                           CategoryMapper categoryMapper) {
        this.chatClient = chatClientBuilder.build();
        this.categoryMapper = categoryMapper;
    }

    /**
     * AI 行程规划
     *
     * @param userInput 用户的自然语言描述
     * @return 结构化的行程方案（可直接填充到创建活动表单）
     */
    public Result<TripPlanVO> planTrip(String userInput) {
        // 参数校验
        if (userInput == null || userInput.isBlank()) {
            return Result.error("请输入行程描述");
        }
        if (userInput.length() > 500) {
            return Result.error("描述不能超过500字");
        }

        // 查询所有可用分类（给 AI 供参）
        List<Category> categories = categoryMapper.selectList(null);
        String categoryInfo = categories.stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .map(c -> c.getCategoryId() + "=" + c.getName())
                .collect(Collectors.joining(", "));

        // 获取当前日期（供 AI 推算具体日期）
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 构造 Prompt
        String prompt = buildPrompt(userInput, categoryInfo, today);

        // 调用 AI
        try {
            TripPlanVO plan = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(TripPlanVO.class);

            if (plan == null) {
                return Result.error("AI 行程规划返回为空，请稍后重试");
            }

            log.info("[AI行程规划] 生成成功, title={}, location={}", plan.getTitle(), plan.getLocationName());
            return Result.success(plan);

        } catch (Exception e) {
            log.error("[AI行程规划] 调用 DeepSeek API 失败", e);
            return Result.error("AI 行程规划服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 构造 Prompt
     */
    private String buildPrompt(String userInput, String categoryInfo, String today) {
        return String.format("""
                你是一个旅行行程规划助手。请根据用户的描述，生成一个结构化的活动方案。
                
                ## 用户输入
                %s
                
                ## 今天的日期
                %s
                
                ## 可用活动分类（ID=名称）
                %s
                
                ## 要求
                请返回以下字段：
                
                1. **title**: 一个简洁有吸引力的活动标题（15字以内）
                2. **description**: 详细的行程安排，格式如下：
                   - 第一段：总体介绍（2-3句）
                   - 然后每天的行程用 "📅 Day1：标题" 格式
                   - 每个时间段包含时间、活动内容和小贴士
                   - 最后一段：注意事项
                3. **locationName**: 主要目的地名称（简短，如"塔公草原"）
                4. **locationAddress**: 详细地址（如"四川省甘孜州康定市塔公镇"）
                5. **startTime**: 活动开始时间，ISO格式 "yyyy-MM-ddTHH:mm:ss"，根据用户描述和今天日期推算
                6. **endTime**: 活动结束时间，同格式
                7. **registrationEndTime**: 报名截止时间（默认出发前2天，同格式）
                8. **maxParticipants**: 推荐最大参与人数（含发起人），根据用户描述推算，没提到就默认4
                9. **paymentType**: 费用方式 1=AA制 2=发起人请客 3=免费 4=各付各的，默认1
                10. **budget**: 人均预算估算（整数，单位元），根据目的地和天数合理估算
                11. **categoryIds**: 从可用分类中选择最匹配的1-2个分类ID，返回整数数组
                """,
                userInput, today, categoryInfo);
    }
}
