package com.limengyuan.partner.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.vo.ActivityVO;
import com.limengyuan.partner.common.dto.vo.ActivityWithApplicationsVO;
import com.limengyuan.partner.common.dto.request.JoinActivityRequest;
import com.limengyuan.partner.common.dto.vo.MyApplicationVO;
import com.limengyuan.partner.common.dto.vo.ParticipantPageVO;
import com.limengyuan.partner.common.dto.vo.ParticipantVO;
import com.limengyuan.partner.common.dto.request.ReviewRequest;
import com.limengyuan.partner.common.entity.Activity;
import com.limengyuan.partner.common.entity.Participant;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.mapper.ActivityMapper;
import com.limengyuan.partner.post.mapper.ParticipantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动参与服务层 - 处理报名、审核、退出等业务逻辑
 */
@Service
@Transactional
public class ParticipantService {

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * 申请加入活动
     */
    public Result<Participant> joinActivity(Long activityId, Long userId, JoinActivityRequest request) {
        // 1. 检查活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 2. 检查是否是活动发起人
        if (activity.getInitiatorId().equals(userId)) {
            return Result.error("您是活动发起人，无需申请");
        }

        // 3. 检查活动状态
        if (activity.getStatus() != 0) {
            return Result.error("活动已结束或已取消，无法报名");
        }

        // 4. 检查是否重复申请
        Participant existing = findByActivityIdAndUserId(activityId, userId);
        if (existing != null) {
            if (existing.getStatus() == Participant.STATUS_PENDING) {
                return Result.error("您已申请过，请等待审核");
            } else if (existing.getStatus() == Participant.STATUS_APPROVED) {
                return Result.error("您已是活动成员");
            } else if (existing.getStatus() == Participant.STATUS_REJECTED) {
                return Result.error("您的申请已被拒绝");
            }
            // 如果是主动退出(STATUS_LEFT)，允许重新申请 - 更新状态
            participantMapper.updateStatus(existing.getParticipantId(), Participant.STATUS_PENDING);
            return Result.success("重新申请成功，请等待审核", existing);
        }

        // 5. 检查是否已满员
        int currentCount = countApprovedByActivityId(activityId);
        if (currentCount >= activity.getMaxParticipants() - 1) {
            return Result.error("活动已满员");
        }

        // 6. 创建参与记录（MP 自动回填 participantId）
        Participant participant = Participant.builder()
                .activityId(activityId)
                .userId(userId)
                .status(Participant.STATUS_PENDING)
                .applyMsg(request != null ? request.getApplyMsg() : null)
                .build();

        int rows = participantMapper.insert(participant);
        if (rows == 0) {
            return Result.error("申请失败，请稍后重试");
        }

        return Result.success("申请成功，请等待发起人审核", participant);
    }

    /**
     * 获取活动的所有参与者列表
     */
    public Result<List<ParticipantVO>> getParticipants(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        List<ParticipantVO> participants = participantMapper.findByActivityIdWithUser(activityId);
        return Result.success(participants);
    }

    /**
     * 获取活动的已通过参与者列表
     */
    public Result<List<ParticipantVO>> getApprovedParticipants(Long activityId) {
        List<ParticipantVO> participants = participantMapper.findApprovedByActivityIdWithUser(activityId);
        return Result.success(participants);
    }

    /**
     * 审核通过
     */
    public Result<Void> approveParticipant(Long participantId, Long operatorId) {
        // 1. 查询参与记录
        Participant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            return Result.error("申请记录不存在");
        }

        // 2. 查询活动
        Activity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 3. 验证操作者是否是活动发起人
        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        // 4. 检查申请状态
        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        // 5. 检查是否已满员
        int currentCount = countApprovedByActivityId(participant.getActivityId());
        if (currentCount >= activity.getMaxParticipants() - 1) {
            return Result.error("活动已满员，无法通过更多申请");
        }

        // 6. 更新参与者状态
        boolean success = participantMapper.updateStatus(participantId, Participant.STATUS_APPROVED);
        if (!success) {
            return Result.error("操作失败");
        }

        // 7. 通过后检查是否已满员，满员则更新活动状态
        int newCount = currentCount + 1;
        if (newCount >= activity.getMaxParticipants() - 1) {
            activityMapper.updateStatus(activity.getActivityId(), 1); // 1 = 已满员
        }

        return Result.success("已通过", null);
    }

    /**
     * 审核拒绝
     */
    public Result<Void> rejectParticipant(Long participantId, Long operatorId) {
        Participant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            return Result.error("申请记录不存在");
        }

        Activity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }

        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        boolean success = participantMapper.updateStatus(participantId, Participant.STATUS_REJECTED);
        if (!success) {
            return Result.error("操作失败");
        }

        return Result.success("已拒绝", null);
    }

    /**
     * 退出活动
     */
    public Result<Void> leaveActivity(Long activityId, Long userId) {
        Participant participant = findByActivityIdAndUserId(activityId, userId);
        if (participant == null) {
            return Result.error("您未参与该活动");
        }

        if (participant.getStatus() == Participant.STATUS_LEFT) {
            return Result.error("您已退出该活动");
        }

        // 记录退出前的状态，用于判断是否需要恢复活动招募状态
        int previousStatus = participant.getStatus();

        boolean success = participantMapper.updateStatus(participant.getParticipantId(), Participant.STATUS_LEFT);
        if (!success) {
            return Result.error("操作失败");
        }

        // 如果退出的是已通过的成员，且活动当前是已满员状态，则恢复为招募中
        if (previousStatus == Participant.STATUS_APPROVED) {
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null && activity.getStatus() == 1) { // 1 = 已满员
                activityMapper.updateStatus(activityId, 0); // 0 = 招募中
            }
        }

        return Result.success("已退出活动", null);
    }

    /**
     * 统一审核接口
     */
    public Result<Void> reviewParticipant(Long participantId, Long operatorId, ReviewRequest request) {
        if (request == null || (!request.isApprove() && !request.isReject())) {
            return Result.error("请指定审核动作: approve 或 reject");
        }

        Participant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            return Result.error("申请记录不存在");
        }

        Activity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }

        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        if (request.isApprove()) {
            int currentCount = countApprovedByActivityId(participant.getActivityId());
            if (currentCount >= activity.getMaxParticipants() - 1) {
                return Result.error("活动已满员，无法通过更多申请");
            }
            participantMapper.updateStatus(participantId, Participant.STATUS_APPROVED);

            // 通过后检查是否已满员，满员则更新活动状态
            int newCount = currentCount + 1;
            if (newCount >= activity.getMaxParticipants() - 1) {
                activityMapper.updateStatus(activity.getActivityId(), 1); // 1 = 已满员
            }

            return Result.success("已通过", null);
        } else {
            participantMapper.updateStatus(participantId, Participant.STATUS_REJECTED);
            return Result.success("已拒绝", null);
        }
    }

    /**
     * 修改申请留言
     */
    public Result<Void> updateApplyMsg(Long participantId, Long userId, String applyMsg) {
        Participant participant = participantMapper.selectById(participantId);
        if (participant == null) {
            return Result.error("申请记录不存在");
        }

        if (!participant.getUserId().equals(userId)) {
            return Result.error("无权修改他人的申请留言");
        }

        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("申请已处理，无法修改留言");
        }

        boolean success = participantMapper.updateApplyMsg(participantId, applyMsg);
        if (!success) {
            return Result.error("修改失败，请稍后重试");
        }

        return Result.success("留言已更新", null);
    }

    /**
     * 获取用户的所有申请记录
     */
    public Result<List<MyApplicationVO>> getMyApplications(Long userId) {
        List<MyApplicationVO> applications = participantMapper.findByUserIdWithActivity(userId);
        return Result.success(applications);
    }

    /**
     * 获取用户发布的活动及其申请列表
     */
    public Result<List<ActivityWithApplicationsVO>> getMyActivitiesWithApplications(Long userId) {
        List<ActivityVO> activities = activityMapper.findByInitiatorIdWithUser(userId);

        List<ActivityWithApplicationsVO> result = new ArrayList<>();
        for (ActivityVO activity : activities) {
            List<ParticipantVO> applications = participantMapper.findByActivityIdWithUserPaged(
                    activity.getActivityId(), 0, 7);
            int totalApplications = countByActivityId(activity.getActivityId());

            ActivityWithApplicationsVO vo = ActivityWithApplicationsVO.builder()
                    .activityId(activity.getActivityId())
                    .initiatorId(activity.getInitiatorId())
                    .categoryIds(activity.getCategoryIds())
                    .title(activity.getTitle())
                    .description(activity.getDescription())
                    .images(activity.getImages())
                    .locationName(activity.getLocationName())
                    .locationAddress(activity.getLocationAddress())
                    .latitude(activity.getLatitude())
                    .longitude(activity.getLongitude())
                    .startTime(activity.getStartTime())
                    .maxParticipants(activity.getMaxParticipants())
                    .paymentType(activity.getPaymentType())
                    .status(activity.getStatus())
                    .createdAt(activity.getCreatedAt())
                    .updatedAt(activity.getUpdatedAt())
                    .initiatorNickname(activity.getInitiatorNickname())
                    .initiatorAvatar(activity.getInitiatorAvatar())
                    .initiatorCreditScore(activity.getInitiatorCreditScore())
                    .currentParticipants(activity.getCurrentParticipants())
                    .applications(applications)
                    .totalApplications(totalApplications)
                    .build();

            result.add(vo);
        }

        return Result.success(result);
    }

    /**
     * 分页获取活动的参与者列表
     */
    public Result<ParticipantPageVO> getParticipantsPaged(Long activityId, int page, int size) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        int offset = page * size;
        List<ParticipantVO> participants = participantMapper.findByActivityIdWithUserPaged(activityId, offset, size);
        int total = countByActivityId(activityId);

        ParticipantPageVO pageVO = ParticipantPageVO.builder()
                .list(participants)
                .total(total)
                .page(page)
                .size(size)
                .build();

        return Result.success(pageVO);
    }

    // ============================
    // 内部辅助方法（使用 QueryWrapper）
    // ============================

    /**
     * 根据活动ID和用户ID查询参与记录
     */
    private Participant findByActivityIdAndUserId(Long activityId, Long userId) {
        QueryWrapper<Participant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId);
        return participantMapper.selectOne(wrapper);
    }

    /**
     * 统计某活动已通过的参与人数
     */
    private int countApprovedByActivityId(Long activityId) {
        QueryWrapper<Participant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("status", 1);
        return Math.toIntExact(participantMapper.selectCount(wrapper));
    }

    /**
     * 统计某活动的所有参与记录数
     */
    private int countByActivityId(Long activityId) {
        QueryWrapper<Participant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        return Math.toIntExact(participantMapper.selectCount(wrapper));
    }
}
