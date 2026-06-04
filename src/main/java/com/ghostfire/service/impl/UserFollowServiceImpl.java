package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserFollow;
import com.ghostfire.mapper.UserFollowMapper;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.UserFollowService;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private final UserService userService;
    private final MessageService messageService;

    @Override
    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        User followee = userService.getById(followeeId);
        if (followee == null) {
            throw new RuntimeException("用户不存在");
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        try {
            save(follow);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("已关注");
        }

        messageService.send(followerId, followeeId, "关注了你", Constant.MSG_TYPE_FOLLOW, followerId);
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        remove(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId));
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId)) > 0;
    }

    @Override
    public int followerCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFolloweeId, userId));
    }

    @Override
    public int followingCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowerId, userId));
    }

    @Override
    public List<Long> listFollowerIds(Long userId) {
        return list(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFolloweeId, userId))
                .stream()
                .map(UserFollow::getFollowerId)
                .toList();
    }

    @Override
    public List<Long> listFolloweeIds(Long userId) {
        return list(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowerId, userId))
                .stream()
                .map(UserFollow::getFolloweeId)
                .toList();
    }
}
