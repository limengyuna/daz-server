package com.limengyuan.partner.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limengyuan.partner.common.dto.CreateMomentRequest;
import com.limengyuan.partner.common.dto.MomentCommentVO;
import com.limengyuan.partner.common.dto.MomentVO;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.UpdateMomentRequest;
import com.limengyuan.partner.common.entity.Moment;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.MomentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 动态服务层
 */
@Service
@Transactional
public class MomentService {

    private final MomentMapper momentMapper;
    private final ObjectMapper objectMapper;

    public MomentService(MomentMapper momentMapper, ObjectMapper objectMapper) {
        this.momentMapper = momentMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 动态 CRUD ====================

    /**
     * 发布动态
     *
     * @param request 发布请求
     * @return 发布结果
     */
    public Result<MomentVO> createMoment(CreateMomentRequest request) {
        // 处理图片列表为 JSON 字符串
        String imagesJson = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(request.getImages());
            } catch (JsonProcessingException e) {
                return Result.error("图片格式错误");
            }
        }

        Moment moment = Moment.builder()
                .userId(request.getUserId())
                .content(request.getContent().trim())
                .images(imagesJson)
                .locationName(request.getLocationName() != null ? request.getLocationName().trim() : null)
                .locationAddress(request.getLocationAddress() != null ? request.getLocationAddress().trim() : null)
                .visibility(request.getVisibility() != null ? request.getVisibility() : 0)
                .build();

        Long momentId = momentMapper.insert(moment);
        if (momentId == null) {
            return Result.error("发布失败");
        }

        return momentMapper.findByIdWithUser(momentId)
                .map(vo -> Result.success("发布成功", vo))
                .orElse(Result.error("发布成功但查询失败"));
    }

    /**
     * 查看动态详情 (同时增加浏览数)
     *
     * @param momentId  动态ID
     * @param currentUserId 当前登录用户ID，用于判断是否已点赞，未登录传 null
     * @return 动态详情
     */
    @Transactional
    public Result<MomentVO> getMoment(Long momentId, Long currentUserId) {
        Optional<MomentVO> optional = momentMapper.findByIdWithUser(momentId);
        if (optional.isEmpty()) {
            return Result.error("动态不存在或已被删除");
        }
        MomentVO vo = optional.get();

        // 增加浏览数
        momentMapper.incrementViewCount(momentId);
        vo.setViewCount(vo.getViewCount() != null ? vo.getViewCount() + 1 : 1);

        // 判断当前用户是否已点赞
        if (currentUserId != null) {
            vo.setLiked(momentMapper.existsLike(momentId, currentUserId));
        } else {
            vo.setLiked(false);
        }

        return Result.success(vo);
    }

    /**
     * 获取动态广场列表（分页，只显示公开动态）
     *
     * @param page 页码，从 0 开始
     * @param size 每页数量
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public Result<PageResult<MomentVO>> getMomentList(int page, int size) {
        List<MomentVO> list = momentMapper.findAllPublicWithUser(page, size);
        long total = momentMapper.countAllPublic();
        return Result.success(PageResult.of(list, total, page, size));
    }

    /**
     * 查看当前登录用户自己的动态列表
     *
     * @param userId 用户ID
     * @return 动态列表
     */
    @Transactional(readOnly = true)
    public Result<List<MomentVO>> getMyMoments(Long userId) {
        List<MomentVO> list = momentMapper.findByUserIdWithUser(userId);
        return Result.success(list);
    }

    /**
     * 查看指定用户的动态列表（公开主页）
     *
     * @param userId 目标用户ID
     * @return 动态列表
     */
    @Transactional(readOnly = true)
    public Result<List<MomentVO>> getUserMoments(Long userId) {
        List<MomentVO> list = momentMapper.findByUserIdWithUser(userId);
        return Result.success(list);
    }

    /**
     * 编辑动态（只有发布者本人可以操作，只更新传入的非 null 字段）
     *
     * @param momentId 动态ID
     * @param userId   当前登录用户ID
     * @param request  编辑请求
     * @return 更新后的动态详情
     */
    @Transactional
    public Result<MomentVO> updateMoment(Long momentId, Long userId, UpdateMomentRequest request) {
        // 处理图片列表
        String imagesJson = null;
        if (request.getImages() != null) {
            try {
                imagesJson = objectMapper.writeValueAsString(request.getImages());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return Result.error("图片格式错误");
            }
        }

        String content = request.getContent() != null ? request.getContent().trim() : null;
        String locationName = request.getLocationName() != null ? request.getLocationName().trim() : null;
        String locationAddress = request.getLocationAddress() != null ? request.getLocationAddress().trim() : null;

        // 至少要有一个字段被修改
        if (content == null && imagesJson == null && locationName == null
                && locationAddress == null && request.getVisibility() == null) {
            return Result.error(400, "请至少修改一个字段");
        }

        int rows = momentMapper.update(momentId, userId, content, imagesJson,
                locationName, locationAddress, request.getVisibility());
        if (rows == 0) {
            return Result.error("更新失败，动态不存在或无权限编辑");
        }

        return momentMapper.findByIdWithUser(momentId)
                .map(Result::success)
                .orElse(Result.error("更新成功但查询失败"));
    }

    /**
     * 删除动态（软删除，只有发布者本人可以删除）
     *
     * @param momentId 动态ID
     * @param userId   当前登录用户ID
     * @return 操作结果
     */
    public Result<Void> deleteMoment(Long momentId, Long userId) {
        int rows = momentMapper.deleteSoft(momentId, userId);
        if (rows == 0) {
            return Result.error("删除失败，动态不存在或无权限删除");
        }
        return Result.success("删除成功", null);
    }

    // ==================== 点赞 ====================

    /**
     * 点赞动态
     *
     * @param momentId 动态ID
     * @param userId   当前登录用户ID
     * @return 操作结果
     */
    @Transactional
    public Result<Void> likeMoment(Long momentId, Long userId) {
        if (momentMapper.existsLike(momentId, userId)) {
            return Result.error(400, "已经点赞过了");
        }
        momentMapper.insertLike(momentId, userId);
        momentMapper.incrementLikeCount(momentId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞
     *
     * @param momentId 动态ID
     * @param userId   当前登录用户ID
     * @return 操作结果
     */
    @Transactional
    public Result<Void> unlikeMoment(Long momentId, Long userId) {
        int rows = momentMapper.deleteLike(momentId, userId);
        if (rows == 0) {
            return Result.error(400, "尚未点赞");
        }
        momentMapper.decrementLikeCount(momentId);
        return Result.success("取消点赞成功", null);
    }

    // ==================== 评论 ====================

    /**
     * 发表评论或回复
     *
     * @param momentId  动态ID
     * @param userId    评论用户ID
     * @param parentId  父评论ID，发一级评论时传 null
     * @param replyToId 被回复人用户ID，发一级评论时传 null
     * @param content   评论内容
     * @return 操作结果
     */
    @Transactional
    public Result<Long> addComment(Long momentId, Long userId,
                                   Long parentId, Long replyToId, String content) {
        if (content == null || content.isBlank()) {
            return Result.error("评论内容不能为空");
        }
        if (content.length() > 500) {
            return Result.error("评论内容不能超过500字");
        }

        Long commentId = momentMapper.insertComment(momentId, userId, parentId, replyToId, content.trim());
        if (commentId == null) {
            return Result.error("评论失败");
        }
        // 同步更新动态评论数
        momentMapper.incrementCommentCount(momentId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 获取动态的评论列表（两级结构）
     *
     * @param momentId 动态ID
     * @return 一级评论列表，每条一级评论内嵌 replies 回复列表
     */
    @Transactional(readOnly = true)
    public Result<List<MomentCommentVO>> getComments(Long momentId) {
        // 1. 查询所有一级评论
        List<MomentCommentVO> topComments = momentMapper.findTopCommentsByMomentId(momentId);

        // 2. 为每条一级评论填充回复列表
        for (MomentCommentVO comment : topComments) {
            List<MomentCommentVO> replies = momentMapper.findRepliesByParentId(comment.getCommentId());
            comment.setReplies(replies);
        }

        return Result.success(topComments);
    }
}
