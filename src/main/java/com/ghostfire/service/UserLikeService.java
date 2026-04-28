package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserLike;

public interface UserLikeService extends IService<UserLike> {

    void like(Long userId, Long targetId, Integer targetType);

    void unlike(Long userId, Long targetId, Integer targetType);

    boolean isLiked(Long userId, Long targetId, Integer targetType);
}
