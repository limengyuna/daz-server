package com.limengyuan.partner.user.service;

import com.limengyuan.partner.common.dto.PageResult;
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
     * 获取关注列表（我关注的人）- 分页
     */
    public Result<PageResult<User>> getFollowingList(Long userId, int page, int size) {
        // 计算偏移量
        int offset = page * size;
        
        // 获取分页数据
        List<User> followingList = userFollowMapper.getFollowingList(userId, offset, size);
        // 不返回密码
        followingList.forEach(user -> user.setPasswordHash(null));
        
        // 获取总数
        int total = userFollowMapper.getFollowingCount(userId);
        
        // 构建分页结果
        PageResult<User> pageResult = PageResult.of(followingList, total, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取粉丝列表（关注我的人）- 分页
     */
    public Result<PageResult<User>> getFollowersList(Long userId, int page, int size) {
        // 计算偏移量
        int offset = page * size;
        
        // 获取分页数据
        List<User> followersList = userFollowMapper.getFollowersList(userId, offset, size);
        // 不返回密码
        followersList.forEach(user -> user.setPasswordHash(null));
        
        // 获取总数
        int total = userFollowMapper.getFollowersCount(userId);
        
        // 构建分页结果
        PageResult<User> pageResult = PageResult.of(followersList, total, page, size);
        return Result.success(pageResult);
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
