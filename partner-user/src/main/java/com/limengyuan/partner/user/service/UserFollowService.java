package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.entity.User;
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

    public UserFollowService(UserFollowMapper userFollowMapper, UserMapper userMapper) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
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
        if (userMapper.findById(followeeId).isEmpty()) {
            return Result.error("用户不存在");
        }

        // 检查是否已关注
        if (userFollowMapper.isFollowing(followerId, followeeId)) {
            return Result.error("已经关注过该用户");
        }

        // 执行关注
        int rows = userFollowMapper.follow(followerId, followeeId);
        if (rows > 0) {
            return Result.success("关注成功", null);
        } else {
            return Result.error("关注失败");
        }
    }

    /**
     * 取消关注
     */
    public Result<Void> unfollowUser(Long followerId, Long followeeId) {
        // 检查是否已关注
        if (!userFollowMapper.isFollowing(followerId, followeeId)) {
            return Result.error("未关注该用户");
        }

        // 执行取消关注
        int rows = userFollowMapper.unfollow(followerId, followeeId);
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
        boolean isFollowing = userFollowMapper.isFollowing(followerId, followeeId);
        return Result.success(isFollowing);
    }

    /**
     * 获取关注列表（我关注的人）
     */
    public Result<List<User>> getFollowingList(Long userId) {
        List<User> followingList = userFollowMapper.getFollowingList(userId);
        // 不返回密码
        followingList.forEach(user -> user.setPasswordHash(null));
        return Result.success(followingList);
    }

    /**
     * 获取粉丝列表（关注我的人）
     */
    public Result<List<User>> getFollowersList(Long userId) {
        List<User> followersList = userFollowMapper.getFollowersList(userId);
        // 不返回密码
        followersList.forEach(user -> user.setPasswordHash(null));
        return Result.success(followersList);
    }

    /**
     * 获取关注统计信息
     */
    public Result<Map<String, Integer>> getFollowStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("followingCount", userFollowMapper.getFollowingCount(userId));
        stats.put("followersCount", userFollowMapper.getFollowersCount(userId));
        return Result.success(stats);
    }
}
