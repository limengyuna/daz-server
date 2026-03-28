package com.limengyuan.partner.post.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.limengyuan.partner.common.dto.request.CreateMomentRequest;
import com.limengyuan.partner.common.dto.vo.MomentCommentVO;
import com.limengyuan.partner.common.dto.vo.MomentVO;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.request.UpdateMomentRequest;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
import com.limengyuan.partner.post.service.MomentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/moments")
public class MomentController {

    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }

    // ==================== 动态 CRUD ====================

    /**
     * 发布动态
     * POST /api/moments
     */
    @PostMapping
    public Result<MomentVO> createMoment(@Valid @RequestBody CreateMomentRequest request) {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.createMoment(userId, request);
    }

    /**
     * 动态广场 - 分页获取所有公开动态
     */
    @GetMapping
    @SentinelResource(value = "listMoments", blockHandler = "listMomentsBlockHandler")
    public Result<PageResult<MomentVO>> getMomentList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return momentService.getMomentList(page, size);
    }

    /**
     * 查看动态详情（同时增加浏览数）
     * GET /api/moments/{id}
     */
    @GetMapping("/{id}")
    @SentinelResource(value = "getMoment", blockHandler = "getMomentBlockHandler")
    public Result<MomentVO> getMoment(@PathVariable("id") Long momentId) {
        Long currentUserId = UserContextHolder.getPrincipalId();
        return momentService.getMoment(momentId, currentUserId);
    }

    /**
     * 获取当前登录用户自己的动态列表
     * GET /api/moments/my
     */
    @GetMapping("/my")
    public Result<List<MomentVO>> getMyMoments() {
        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.getMyMoments(userId);
    }

    /**
     * 查看指定用户的动态列表（公开主页）
     * GET /api/moments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<List<MomentVO>> getUserMoments(@PathVariable("userId") Long userId) {
        return momentService.getUserMoments(userId);
    }

    /**
     * 编辑动态（只有发布者本人可以操作）
     * PUT /api/moments/{id}
     */
    @PutMapping("/{id}")
    public Result<MomentVO> updateMoment(
            @PathVariable("id") Long momentId,
            @Valid @RequestBody UpdateMomentRequest request) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.updateMoment(momentId, userId, request);
    }

    /**
     * 删除动态（软删除，只有发布者本人可以操作）
     * DELETE /api/moments/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMoment(@PathVariable("id") Long momentId) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.deleteMoment(momentId, userId);
    }

    // ==================== 点赞 ====================

    /**
     * 点赞动态
     * POST /api/moments/{id}/like
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeMoment(@PathVariable("id") Long momentId) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.likeMoment(momentId, userId);
    }

    /**
     * 取消点赞
     * DELETE /api/moments/{id}/like
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeMoment(@PathVariable("id") Long momentId) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.unlikeMoment(momentId, userId);
    }

    // ==================== 评论 ====================

    /**
     * 获取动态评论列表（两级结构）
     * GET /api/moments/{id}/comments
     */
    @GetMapping("/{id}/comments")
    public Result<List<MomentCommentVO>> getComments(@PathVariable("id") Long momentId) {
        return momentService.getComments(momentId);
    }

    /**
     * 发表评论或回复
     * POST /api/moments/{id}/comments
     */
    @PostMapping("/{id}/comments")
    public Result<Long> addComment(
            @PathVariable("id") Long momentId,
            @RequestBody CommentRequest body) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.addComment(momentId, userId, body.getParentId(), body.getReplyToId(), body.getContent());
    }

    // ==================== 内部静态类 ====================

    /**
     * 评论请求体
     */
    static class CommentRequest {
        private String content;
        private Long parentId;
        private Long replyToId;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Long getReplyToId() { return replyToId; }
        public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }
    }

    // ==================== Sentinel 降级处理方法 ====================

    /**
     * 获取动态列表 - 限流降级处理
     */
    public Result<PageResult<MomentVO>> listMomentsBlockHandler(
            int page, int size, BlockException ex) {
        log.warn("[Sentinel] 获取动态列表接口被限流/降级", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 获取动态详情 - 限流降级处理
     */
    public Result<MomentVO> getMomentBlockHandler(
            Long momentId, BlockException ex) {
        // [修复参数签名错位问题]
        log.warn("[Sentinel] 获取动态详情接口被限流/降级, momentId={}", momentId, ex);
        return Result.error("系统繁忙，请稍后再试");
    }

}
