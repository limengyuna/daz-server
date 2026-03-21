package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.vo.TripPlanVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.service.ContentPolishService;
import com.limengyuan.partner.post.service.TripPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 功能控制器 — 内容润色、行程规划、智能搜索等 AI 相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ContentPolishService contentPolishService;
    private final TripPlanService tripPlanService;

    public AiController(ContentPolishService contentPolishService,
                        TripPlanService tripPlanService) {
        this.contentPolishService = contentPolishService;
        this.tripPlanService = tripPlanService;
    }

    /**
     * AI 内容润色
     * POST /api/ai/polish
     *
     * 请求体: { "content": "原始文本", "type": "activity" }
     * 返回: Result<String>，data 即润色后的文本
     */
    @PostMapping("/polish")
    public Result<String> polishContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String type = request.get("type");
        return contentPolishService.polishContent(content, type);
    }

    /**
     * AI 行程规划
     * POST /api/ai/plan-trip
     *
     * 用户输入一句自然语言，AI 返回结构化的活动信息，可直接填充到创建活动表单。
     *
     * 请求体: { "input": "下月3号想去塔公草原玩两天，找2个人" }
     * 返回: Result<TripPlanVO>，包含 title、description、locationName、startTime 等所有表单字段
     */
    @PostMapping("/plan-trip")
    public Result<TripPlanVO> planTrip(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        return tripPlanService.planTrip(input);
    }
}
