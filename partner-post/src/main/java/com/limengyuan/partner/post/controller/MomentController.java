package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.CreateMomentRequest;
import com.limengyuan.partner.common.dto.MomentCommentVO;
import com.limengyuan.partner.common.dto.MomentVO;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.UpdateMomentRequest;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.post.service.MomentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态控制器
 * <p>
 * 接口列表:
 * POST   /api/moments                        发布动态
 * GET    /api/moments                        动态广场（分页列表）
 * GET    /api/moments/{id}                   查看动态详情
 * GET    /api/moments/my                     我的动态列表
 * GET    /api/moments/user/{userId}          查看指定用户的动态
 * DELETE /api/moments/{id}                   删除动态
 * POST   /api/moments/{id}/like              点赞
 * DELETE /api/moments/{id}/like              取消点赞
 * GET    /api/moments/{id}/comments          获取评论列表
 * POST   /api/moments/{id}/comments          发表评论/回复
 */
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
        return momentService.createMoment(request);
    }

    /**
     * 动态广场 - 分页获取所有公开动态
     * GET /api/moments?page=0&size=10
     *
     * @param page 页码，从 0 开始，默认 0
     * @param size 每页数量，默认 10
     */
    @GetMapping
    public Result<PageResult<MomentVO>> getMomentList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return momentService.getMomentList(page, size);
    }

    /**
     * 查看动态详情（同时增加浏览数）
     * GET /api/moments/{id}
     *
     * @param momentId     动态ID
     * @param authHeader   Authorization 请求头，可选，传了则判断是否已点赞
     */
    @GetMapping("/{id}")
    public Result<MomentVO> getMoment(
            @PathVariable("id") Long momentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long currentUserId = parseUserIdFromToken(authHeader);
        return momentService.getMoment(momentId, currentUserId);
    }

    /**
     * 获取当前登录用户自己的动态列表
     * GET /api/moments/my
     * 请求头需携带: Authorization: Bearer {token}
     */
    @GetMapping("/my")
    public Result<List<MomentVO>> getMyMoments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.getMyMoments(userId);
    }

    /**
     * 查看指定用户的动态列表（公开主页）
     * GET /api/moments/user/{userId}
     *
     * @param userId 目标用户ID
     */
    @GetMapping("/user/{userId}")
    public Result<List<MomentVO>> getUserMoments(@PathVariable("userId") Long userId) {
        return momentService.getUserMoments(userId);
    }

    /**
     * 编辑动态（只有发布者本人可以操作）
     * PUT /api/moments/{id}
     * 请求头需携带: Authorization: Bearer {token}
     * <p>
     * 所有字段均为可选，只传需要修改的字段即可。
     * 示例: { "visibility": 1 }  仅修改可见范围
     * 示例: { "content": "新内容", "visibility": 0 }
     */
    @PutMapping("/{id}")
    public Result<MomentVO> updateMoment(
            @PathVariable("id") Long momentId,
            @Valid @RequestBody UpdateMomentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.updateMoment(momentId, userId, request);
    }

    /**
     * 删除动态（软删除，只有发布者本人可以操作）
     * DELETE /api/moments/{id}
     * 请求头需携带: Authorization: Bearer {token}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMoment(
            @PathVariable("id") Long momentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.deleteMoment(momentId, userId);
    }

    // ==================== 点赞 ====================

    /**
     * 点赞动态
     * POST /api/moments/{id}/like
     * 请求头需携带: Authorization: Bearer {token}
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeMoment(
            @PathVariable("id") Long momentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.likeMoment(momentId, userId);
    }

    /**
     * 取消点赞
     * DELETE /api/moments/{id}/like
     * 请求头需携带: Authorization: Bearer {token}
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeMoment(
            @PathVariable("id") Long momentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录或 Token 无效");
        }
        return momentService.unlikeMoment(momentId, userId);
    }

    // ==================== 评论 ====================

    /**
     * 获取动态评论列表（两级结构）
     * GET /api/moments/{id}/comments
     *
     * @param momentId 动态ID
     */
    @GetMapping("/{id}/comments")
    public Result<List<MomentCommentVO>> getComments(@PathVariable("id") Long momentId) {
        return momentService.getComments(momentId);
    }

    /**
     * 发表评论或回复
     * POST /api/moments/{id}/comments
     * 请求头需携带: Authorization: Bearer {token}
     * <p>
     * 请求体示例 (一级评论):  {"content": "写得不错！"}
     * 请求体示例 (回复评论):  {"content": "同意！", "parentId": 1, "replyToId": 5}
     *
     * @param momentId  动态ID
     * @param body      请求体
     * @param authHeader Authorization 请求头
     */
    @PostMapping("/{id}/comments")
    public Result<Long> addComment(
            @PathVariable("id") Long momentId,
            @RequestBody CommentRequest body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = parseUserIdFromToken(authHeader);
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

    // ==================== 私有工具方法 ====================

    /**
     * 从 Authorization 请求头中解析用户ID，失败返回 null
     */
    private Long parseUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return JwtUtils.getUserIdFromToken(authHeader);
    }
}
