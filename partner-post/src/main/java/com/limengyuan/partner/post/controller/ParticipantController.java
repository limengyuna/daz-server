package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.ActivityWithApplicationsVO;
import com.limengyuan.partner.common.dto.JoinActivityRequest;
import com.limengyuan.partner.common.dto.MyApplicationVO;
import com.limengyuan.partner.common.dto.ParticipantPageVO;
import com.limengyuan.partner.common.dto.ReviewRequest;
import com.limengyuan.partner.common.entity.Participant;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.JwtUtils;
import com.limengyuan.partner.post.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动参与控制器 - 报名、审核、退出等接口
 */
@RestController
@RequestMapping("/api")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    /**
     * 申请加入活动
     * POST /api/activities/{id}/join
     */
    @PostMapping("/activities/{id}/join")
    public Result<Participant> joinActivity(
            @PathVariable("id") Long activityId,
            @RequestBody(required = false) JoinActivityRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.joinActivity(activityId, userId, request);
    }

    /**
     * 获取活动参与者列表（分页）
     * GET /api/activities/{id}/participants?page=0&size=7
     *
     * @param activityId 活动ID
     * @param page       页码，从0开始，默认0
     * @param size       每页数量，默认7
     */
    @GetMapping("/activities/{id}/participants")
    public Result<ParticipantPageVO> getParticipants(
            @PathVariable("id") Long activityId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "7") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.getParticipantsPaged(activityId, page, size);
    }

    /**
     * 统一审核接口
     * PUT /api/participants/{id}/review
     * Body: {"action": "approve"} 或 {"action": "reject"}
     */
    @PutMapping("/participants/{id}/review")
    public Result<Void> reviewParticipant(
            @PathVariable("id") Long participantId,
            @RequestBody ReviewRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.reviewParticipant(participantId, userId, request);
    }

    /**
     * 退出活动
     * DELETE /api/activities/{id}/leave
     */
    @DeleteMapping("/activities/{id}/leave")
    public Result<Void> leaveActivity(
            @PathVariable("id") Long activityId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.leaveActivity(activityId, userId);
    }

    /**
     * 获取我的所有申请记录
     * GET /api/my/applications
     */
    @GetMapping("/my/applications")
    public Result<List<MyApplicationVO>> getMyApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.getMyApplications(userId);
    }

    /**
     * 获取我发布的活动及其申请列表
     * GET /api/my/activities-with-applications
     * 
     * 每个活动默认返回前7条申请记录，附带申请总数。
     * 前端可通过 GET /api/activities/{id}/participants?page=1&size=7 加载更多。
     */
    @GetMapping("/my/activities-with-applications")
    public Result<List<ActivityWithApplicationsVO>> getMyActivitiesWithApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = getUserIdFromAuth(authHeader);
        if (userId == null) {
            return Result.error("请先登录");
        }

        return participantService.getMyActivitiesWithApplications(userId);
    }

    /**
     * 从 Authorization Header 解析用户ID
     */
    private Long getUserIdFromAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return JwtUtils.getUserIdFromToken(authHeader);
    }
}
