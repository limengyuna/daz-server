package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.dto.vo.ActivityWithApplicationsVO;
import com.limengyuan.partner.common.dto.request.JoinActivityRequest;
import com.limengyuan.partner.common.dto.vo.MyApplicationVO;
import com.limengyuan.partner.common.dto.vo.ParticipantPageVO;
import com.limengyuan.partner.common.dto.request.ReviewRequest;
import com.limengyuan.partner.common.entity.Participant;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.common.util.UserContextHolder;
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
            @RequestBody(required = false) JoinActivityRequest request) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return participantService.joinActivity(activityId, userId, request);
    }

    /**
     * 获取活动参与者列表（分页）
     * GET /api/activities/{id}/participants?page=0&size=7
     */
    @GetMapping("/activities/{id}/participants")
    public Result<ParticipantPageVO> getParticipants(
            @PathVariable("id") Long activityId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "7") int size) {

        Long userId = UserContextHolder.getPrincipalId();
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
            @RequestBody ReviewRequest request) {

        Long userId = UserContextHolder.getPrincipalId();
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
    public Result<Void> leaveActivity(@PathVariable("id") Long activityId) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return participantService.leaveActivity(activityId, userId);
    }

    /**
     * 修改申请留言
     * PUT /api/participants/{id}/apply-msg
     * Body: {"applyMsg": "新的留言内容"}
     */
    @PutMapping("/participants/{id}/apply-msg")
    public Result<Void> updateApplyMsg(
            @PathVariable("id") Long participantId,
            @RequestBody JoinActivityRequest request) {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return participantService.updateApplyMsg(participantId, userId, request.getApplyMsg());
    }

    /**
     * 获取我的所有申请记录
     * GET /api/my/applications
     */
    @GetMapping("/my/applications")
    public Result<List<MyApplicationVO>> getMyApplications() {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return participantService.getMyApplications(userId);
    }

    /**
     * 获取我发布的活动及其申请列表
     * GET /api/my/activities-with-applications
     */
    @GetMapping("/my/activities-with-applications")
    public Result<List<ActivityWithApplicationsVO>> getMyActivitiesWithApplications() {

        Long userId = UserContextHolder.getPrincipalId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return participantService.getMyActivitiesWithApplications(userId);
    }
}
