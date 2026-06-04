package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserFollow;

import java.util.List;

public interface UserFollowService extends IService<UserFollow> {

    void follow(Long followerId, Long followeeId);

    void unfollow(Long followerId, Long followeeId);

    boolean isFollowing(Long followerId, Long followeeId);

    int followerCount(Long userId);

    int followingCount(Long userId);

    List<Long> listFollowerIds(Long userId);

    List<Long> listFolloweeIds(Long userId);
}
