package com.limengyuan.partner.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.dto.PageResult;
import com.limengyuan.partner.common.dto.vo.UserProfileVO;
import com.limengyuan.partner.common.entity.User;
import com.limengyuan.partner.common.entity.UserFollow;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.user.mapper.UserFollowMapper;
import com.limengyuan.partner.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户关注服务层
 */
@Service
@Transactional
public class UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    public UserFollowService(UserFollowMapper userFollowMapper, UserMapper userMapper, UserService userService) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * 关注用户
     */
    public Result<Void> followUser(Long followerId, Long followeeId) {
        // 不能关注自己
        if (followerId.equals(followeeId)) {
            return Result.error("不能关注自己");
        }

        // 检查被关注用户是否存在
        if (userMapper.selectById(followeeId) == null) {
            return Result.error("用户不存在");
        }

        // 检查是否已关注
        if (isFollowing(followerId, followeeId)) {
            return Result.error("已经关注过该用户");
        }

        // 执行关注
        UserFollow follow = UserFollow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .build();
        try {
            int rows = userFollowMapper.insert(follow);
            if (rows > 0) {
                return Result.success("关注成功", null);
            } else {
                return Result.error("关注失败");
            }
        } catch (Exception e) {
            // 可能是重复关注，唯一索引冲突
            return Result.error("已经关注过该用户");
        }
    }

    /**
     * 取消关注
     */
    public Result<Void> unfollowUser(Long followerId, Long followeeId) {
        // 检查是否已关注
        if (!isFollowing(followerId, followeeId)) {
            return Result.error("未关注该用户");
        }

        // 执行取消关注
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("followee_id", followeeId);
        int rows = userFollowMapper.delete(wrapper);
        if (rows > 0) {
            return Result.success("取消关注成功", null);
        } else {
            return Result.error("取消关注失败");
        }
    }

    /**
     * 检查是否关注
     */
    public Result<Boolean> checkFollowing(Long followerId, Long followeeId) {
        boolean following = isFollowing(followerId, followeeId);
        return Result.success(following);
    }

    /**
     * 获取关注列表（我关注的人）- 分页
     */
    public Result<PageResult<UserProfileVO>> getFollowingList(Long userId, int page, int size) {
        // 计算偏移量
        int offset = page * size;
        
        // 获取分页数据
        List<User> followingList = userFollowMapper.getFollowingList(userId, offset, size);
        // 转换为 VO，脱敏
        List<UserProfileVO> voList = followingList.stream()
                .map(userService::toProfileVO)
                .collect(java.util.stream.Collectors.toList());
        
        // 获取总数
        int total = getFollowingCount(userId);
        
        // 构建分页结果
        PageResult<UserProfileVO> pageResult = PageResult.of(voList, total, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取粉丝列表（关注我的人）- 分页
     */
    public Result<PageResult<UserProfileVO>> getFollowersList(Long userId, int page, int size) {
        // 计算偏移量
        int offset = page * size;
        
        // 获取分页数据
        List<User> followersList = userFollowMapper.getFollowersList(userId, offset, size);
        // 转换为 VO，脱敏
        List<UserProfileVO> voList = followersList.stream()
                .map(userService::toProfileVO)
                .collect(java.util.stream.Collectors.toList());
        
        // 获取总数
        int total = getFollowersCount(userId);
        
        // 构建分页结果
        PageResult<UserProfileVO> pageResult = PageResult.of(voList, total, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取关注统计信息
     */
    public Result<Map<String, Integer>> getFollowStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("followingCount", getFollowingCount(userId));
        stats.put("followersCount", getFollowersCount(userId));
        return Result.success(stats);
    }

    // ============================
    // 内部辅助方法（使用 QueryWrapper）
    // ============================

    /**
     * 检查是否已关注
     */
    private boolean isFollowing(Long followerId, Long followeeId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId).eq("followee_id", followeeId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取关注数量
     */
    private int getFollowingCount(Long userId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        return Math.toIntExact(userFollowMapper.selectCount(wrapper));
    }

    /**
     * 获取粉丝数量
     */
    private int getFollowersCount(Long userId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("followee_id", userId);
        return Math.toIntExact(userFollowMapper.selectCount(wrapper));
    }
}
