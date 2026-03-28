package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.vo.ReviewVO;
import com.limengyuan.partner.common.dto.request.SubmitReviewRequest;
import com.limengyuan.partner.common.dto.vo.UserReviewPageVO;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import com.limengyuan.partner.post.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器 - 用户评价相关接口
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 提交评价
     * POST /api/reviews
     *
     * 请求头需携带: Authorization: Bearer {token}
     * 请求体: { activityId, revieweeId, score(1-5), content(可选), tags(可选) }
     */
    @PostMapping
    public Result<Void> submitReview(@RequestBody SubmitReviewRequest request) {

        // 从上下文获取当前登录用户ID作为评价人
        Long reviewerId = UserContextHolder.getPrincipalId();
        if (reviewerId == null) {
            return Result.error("未登录或 Token 无效");
        }

        return reviewService.submitReview(reviewerId, request);
    }

    /**
     * 获取活动下的所有评价
     * GET /api/reviews/activity/{activityId}
     */
    @GetMapping("/activity/{activityId}")
    public Result<List<ReviewVO>> getActivityReviews(@PathVariable("activityId") Long activityId) {
        return reviewService.getActivityReviews(activityId);
    }

    /**
     * 获取某用户收到的评价（分页 + 统计）
     * GET /api/reviews/user/{userId}?page=0&size=10
     *
     * 返回数据包含：分页评价列表 + averageScore(平均分) + reviewCount(总评价数)
     */
    @GetMapping("/user/{userId}")
    public Result<UserReviewPageVO> getUserReviews(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        // 参数校验
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 10;
        }

        return reviewService.getUserReviews(userId, page, size);
    }

}
