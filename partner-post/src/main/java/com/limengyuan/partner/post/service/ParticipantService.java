package com.limengyuan.partner.post.service;

import com.limengyuan.partner.common.dto.ActivityVO;
import com.limengyuan.partner.common.dto.ActivityWithApplicationsVO;
import com.limengyuan.partner.common.dto.JoinActivityRequest;
import com.limengyuan.partner.common.dto.MyApplicationVO;
import com.limengyuan.partner.common.dto.ParticipantPageVO;
import com.limengyuan.partner.common.dto.ParticipantVO;
import com.limengyuan.partner.common.dto.ReviewRequest;
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
import java.util.Optional;

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
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param request    申请请求
     * @return 操作结果
     */
    public Result<Participant> joinActivity(Long activityId, Long userId, JoinActivityRequest request) {
        // 1. 检查活动是否存在
        Optional<Activity> activityOpt = activityMapper.findById(activityId);
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 2. 检查是否是活动发起人（发起人不能申请自己的活动）
        if (activity.getInitiatorId().equals(userId)) {
            return Result.error("您是活动发起人，无需申请");
        }

        // 3. 检查活动状态
        if (activity.getStatus() != 0) {
            return Result.error("活动已结束或已取消，无法报名");
        }

        // 4. 检查是否重复申请
        Optional<Participant> existingOpt = participantMapper.findByActivityIdAndUserId(activityId, userId);
        if (existingOpt.isPresent()) {
            Participant existing = existingOpt.get();
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
        int currentCount = participantMapper.countApprovedByActivityId(activityId);
        // 注意: maxParticipants 包含发起人，所以可加入的人数是 maxParticipants - 1
        if (currentCount >= activity.getMaxParticipants() - 1) {
            return Result.error("活动已满员");
        }

        // 6. 创建参与记录
        Participant participant = Participant.builder()
                .activityId(activityId)
                .userId(userId)
                .status(Participant.STATUS_PENDING)
                .applyMsg(request != null ? request.getApplyMsg() : null)
                .build();

        Long participantId = participantMapper.insert(participant);
        if (participantId == null) {
            return Result.error("申请失败，请稍后重试");
        }

        participant.setParticipantId(participantId);
        return Result.success("申请成功，请等待发起人审核", participant);
    }

    /**
     * 获取活动的所有参与者列表
     *
     * @param activityId 活动ID
     * @return 参与者列表
     */
    public Result<List<ParticipantVO>> getParticipants(Long activityId) {
        // 检查活动是否存在
        Optional<Activity> activityOpt = activityMapper.findById(activityId);
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        List<ParticipantVO> participants = participantMapper.findByActivityIdWithUser(activityId);
        return Result.success(participants);
    }

    /**
     * 获取活动的已通过参与者列表
     *
     * @param activityId 活动ID
     * @return 已通过的参与者列表
     */
    public Result<List<ParticipantVO>> getApprovedParticipants(Long activityId) {
        List<ParticipantVO> participants = participantMapper.findApprovedByActivityIdWithUser(activityId);
        return Result.success(participants);
    }

    /**
     * 审核通过
     *
     * @param participantId 参与记录ID
     * @param operatorId    操作者ID（需要是活动发起人）
     * @return 操作结果
     */
    public Result<Void> approveParticipant(Long participantId, Long operatorId) {
        // 1. 查询参与记录
        Optional<Participant> participantOpt = participantMapper.findById(participantId);
        if (participantOpt.isEmpty()) {
            return Result.error("申请记录不存在");
        }

        Participant participant = participantOpt.get();

        // 2. 查询活动
        Optional<Activity> activityOpt = activityMapper.findById(participant.getActivityId());
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 3. 验证操作者是否是活动发起人
        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        // 4. 检查申请状态
        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        // 5. 检查是否已满员
        int currentCount = participantMapper.countApprovedByActivityId(participant.getActivityId());
        if (currentCount >= activity.getMaxParticipants() - 1) {
            return Result.error("活动已满员，无法通过更多申请");
        }

        // 6. 更新状态
        boolean success = participantMapper.updateStatus(participantId, Participant.STATUS_APPROVED);
        if (!success) {
            return Result.error("操作失败");
        }

        return Result.success("已通过", null);
    }

    /**
     * 审核拒绝
     *
     * @param participantId 参与记录ID
     * @param operatorId    操作者ID（需要是活动发起人）
     * @return 操作结果
     */
    public Result<Void> rejectParticipant(Long participantId, Long operatorId) {
        // 1. 查询参与记录
        Optional<Participant> participantOpt = participantMapper.findById(participantId);
        if (participantOpt.isEmpty()) {
            return Result.error("申请记录不存在");
        }

        Participant participant = participantOpt.get();

        // 2. 查询活动
        Optional<Activity> activityOpt = activityMapper.findById(participant.getActivityId());
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 3. 验证操作者是否是活动发起人
        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        // 4. 检查申请状态
        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        // 5. 更新状态
        boolean success = participantMapper.updateStatus(participantId, Participant.STATUS_REJECTED);
        if (!success) {
            return Result.error("操作失败");
        }

        return Result.success("已拒绝", null);
    }

    /**
     * 退出活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 操作结果
     */
    public Result<Void> leaveActivity(Long activityId, Long userId) {
        // 1. 查询参与记录
        Optional<Participant> participantOpt = participantMapper.findByActivityIdAndUserId(activityId, userId);
        if (participantOpt.isEmpty()) {
            return Result.error("您未参与该活动");
        }

        Participant participant = participantOpt.get();

        // 2. 检查状态
        if (participant.getStatus() == Participant.STATUS_LEFT) {
            return Result.error("您已退出该活动");
        }

        // 3. 更新状态为主动退出
        boolean success = participantMapper.updateStatus(participant.getParticipantId(), Participant.STATUS_LEFT);
        if (!success) {
            return Result.error("操作失败");
        }

        return Result.success("已退出活动", null);
    }

    /**
     * 统一审核接口
     *
     * @param participantId 参与记录ID
     * @param operatorId    操作者ID（需要是活动发起人）
     * @param request       审核请求（包含 action: approve/reject）
     * @return 操作结果
     */
    public Result<Void> reviewParticipant(Long participantId, Long operatorId, ReviewRequest request) {
        if (request == null || (!request.isApprove() && !request.isReject())) {
            return Result.error("请指定审核动作: approve 或 reject");
        }

        // 1. 查询参与记录
        Optional<Participant> participantOpt = participantMapper.findById(participantId);
        if (participantOpt.isEmpty()) {
            return Result.error("申请记录不存在");
        }

        Participant participant = participantOpt.get();

        // 2. 查询活动
        Optional<Activity> activityOpt = activityMapper.findById(participant.getActivityId());
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 3. 验证操作者是否是活动发起人
        if (!activity.getInitiatorId().equals(operatorId)) {
            return Result.error("您没有审核权限");
        }

        // 4. 检查申请状态
        if (participant.getStatus() != Participant.STATUS_PENDING) {
            return Result.error("该申请已处理");
        }

        // 5. 根据 action 执行不同操作
        if (request.isApprove()) {
            // 检查是否已满员
            int currentCount = participantMapper.countApprovedByActivityId(participant.getActivityId());
            if (currentCount >= activity.getMaxParticipants() - 1) {
                return Result.error("活动已满员，无法通过更多申请");
            }
            participantMapper.updateStatus(participantId, Participant.STATUS_APPROVED);
            return Result.success("已通过", null);
        } else {
            participantMapper.updateStatus(participantId, Participant.STATUS_REJECTED);
            return Result.success("已拒绝", null);
        }
    }

    /**
     * 获取用户的所有申请记录
     *
     * @param userId 用户ID
     * @return 申请记录列表（包含活动详情）
     */
    public Result<List<MyApplicationVO>> getMyApplications(Long userId) {
        List<MyApplicationVO> applications = participantMapper.findByUserIdWithActivity(userId);
        return Result.success(applications);
    }

    /**
     * 获取用户发布的活动及其申请列表
     * 每个活动默认返回前7条申请记录
     *
     * @param userId 用户ID
     * @return 活动及申请列表
     */
    public Result<List<ActivityWithApplicationsVO>> getMyActivitiesWithApplications(Long userId) {
        // 1. 获取用户发布的所有活动
        List<ActivityVO> activities = activityMapper.findByInitiatorIdWithUser(userId);

        // 2. 为每个活动获取申请列表和申请总数
        List<ActivityWithApplicationsVO> result = new ArrayList<>();
        for (ActivityVO activity : activities) {
            // 获取前7条申请记录
            List<ParticipantVO> applications = participantMapper.findByActivityIdWithUserPaged(
                    activity.getActivityId(), 0, 7);
            // 获取申请总数
            int totalApplications = participantMapper.countByActivityId(activity.getActivityId());

            // 构建聚合对象
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
     *
     * @param activityId 活动ID
     * @param page       页码（从0开始）
     * @param size       每页数量
     * @return 分页结果
     */
    public Result<ParticipantPageVO> getParticipantsPaged(Long activityId, int page, int size) {
        // 检查活动是否存在
        Optional<Activity> activityOpt = activityMapper.findById(activityId);
        if (activityOpt.isEmpty()) {
            return Result.error("活动不存在");
        }

        int offset = page * size;
        List<ParticipantVO> participants = participantMapper.findByActivityIdWithUserPaged(activityId, offset, size);
        int total = participantMapper.countByActivityId(activityId);

        ParticipantPageVO pageVO = ParticipantPageVO.builder()
                .list(participants)
                .total(total)
                .page(page)
                .size(size)
                .build();

        return Result.success(pageVO);
    }
}
