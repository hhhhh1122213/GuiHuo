package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Comment;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserLike;
import com.ghostfire.mapper.UserLikeMapper;
import com.ghostfire.service.CommentService;
import com.ghostfire.service.PostService;
import com.ghostfire.service.UserLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl extends ServiceImpl<UserLikeMapper, UserLike> implements UserLikeService {

    private final PostService postService;
    private final CommentService commentService;

    @Override
    @Transactional
    public void like(Long userId, Long targetId, Integer targetType) {
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType);
        try {
            save(like);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("已点赞");
        }
        if (targetType == Constant.LIKE_POST) {
            Post post = postService.getById(targetId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                postService.updateById(post);
            }
        } else if (targetType == Constant.LIKE_COMMENT) {
            Comment comment = commentService.getById(targetId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                commentService.updateById(comment);
            }
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long targetId, Integer targetType) {
        remove(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetId, targetId)
                .eq(UserLike::getTargetType, targetType));
        if (targetType == Constant.LIKE_POST) {
            Post post = postService.getById(targetId);
            if (post != null && post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
                postService.updateById(post);
            }
        } else if (targetType == Constant.LIKE_COMMENT) {
            Comment comment = commentService.getById(targetId);
            if (comment != null && comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                commentService.updateById(comment);
            }
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, Integer targetType) {
        return count(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetId, targetId)
                .eq(UserLike::getTargetType, targetType)) > 0;
    }
}
