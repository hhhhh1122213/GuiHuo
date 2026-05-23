package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.config.BloomFilterHelper;
import com.ghostfire.entity.Comment;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserLike;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserLikeMapper;
import com.ghostfire.service.CommentService;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.PostService;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserLikeService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl extends ServiceImpl<UserLikeMapper, UserLike> implements UserLikeService {

    private final PostService postService;
    private final CommentService commentService;
    private final UserStatService userStatService;
    private final MedalService medalService;
    private final RankingService rankingService;
    private final BloomFilterHelper bloomFilter;
    private final MessageService messageService;

    @Override
    @Transactional
    public void like(Long userId, Long targetId, Integer targetType) {
        // 布隆过滤器快速排除已点赞（false = 肯定没赞过）
        String bloomKey = "bloom:like:" + userId + ":" + targetType;
        String bloomVal = String.valueOf(targetId);
        if (bloomFilter.mightContain(bloomKey, bloomVal)) {
            throw new RuntimeException("已点赞");
        }

        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType);
        try {
            save(like);
            bloomFilter.add(bloomKey, bloomVal);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("已点赞");
        }
        if (targetType == Constant.LIKE_POST) {
            postService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getId, targetId)
                            .setSql("like_count = like_count + 1"));
            Post post = postService.getById(targetId);
            if (post != null) {
                // 确保作者 UserStat 存在
                UserStat authorStat = userStatService.getById(post.getUserId());
                if (authorStat == null) {
                    authorStat = new UserStat();
                    authorStat.setUserId(post.getUserId());
                    authorStat.setCoin(0L);
                    authorStat.setPostCount(0);
                    authorStat.setLikeCount(0);
                    authorStat.setSignCount(0);
                    userStatService.save(authorStat);
                }
                // 加金币 + 写流水 + 更新被赞数
                userStatService.addCoin(post.getUserId(), Constant.COIN_LIKE_REWARD, Constant.WALLET_LIKE, targetId);
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, post.getUserId())
                                .setSql("like_count = like_count + 1"));
                // 更新获赞排行榜
                UserStat updatedStat = userStatService.getById(post.getUserId());
                if (updatedStat != null) {
                    rankingService.updateScore(RankingService.RANK_LIKE, post.getUserId(),
                            updatedStat.getLikeCount() != null ? updatedStat.getLikeCount() : 0);
                }
                medalService.checkAutoAward(post.getUserId());
                // 通知帖子作者
                if (!userId.equals(post.getUserId())) {
                    try {
                        messageService.send(userId, post.getUserId(),
                                "赞了你的帖子", Constant.MSG_TYPE_LIKE, targetId);
                    } catch (Exception e) {
                        log.warn("点赞通知发送失败: userId={}, authorId={}", userId, post.getUserId(), e);
                    }
                }
            }
        } else if (targetType == Constant.LIKE_COMMENT) {
            commentService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getId, targetId)
                            .setSql("like_count = like_count + 1"));
            Comment comment = commentService.getById(targetId);
            if (comment != null && !userId.equals(comment.getUserId())) {
                try {
                    messageService.send(userId, comment.getUserId(),
                            "赞了你的评论", Constant.MSG_TYPE_LIKE, comment.getPostId());
                } catch (Exception e) {
                    log.warn("评论点赞通知发送失败 userId={}, authorId={}", userId, comment.getUserId(), e);
                }
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
            postService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getId, targetId)
                            .setSql("like_count = GREATEST(like_count - 1, 0)"));
            Post post = postService.getById(targetId);
            if (post != null) {
                userStatService.addCoin(post.getUserId(), -Constant.COIN_LIKE_REWARD, Constant.WALLET_LIKE, targetId);
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, post.getUserId())
                                .setSql("like_count = GREATEST(like_count - 1, 0)"));
                // 更新获赞排行榜
                UserStat updatedStat = userStatService.getById(post.getUserId());
                if (updatedStat != null) {
                    rankingService.updateScore(RankingService.RANK_LIKE, post.getUserId(),
                            updatedStat.getLikeCount() != null ? updatedStat.getLikeCount() : 0);
                }
            }
        } else if (targetType == Constant.LIKE_COMMENT) {
            commentService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getId, targetId)
                            .setSql("like_count = GREATEST(like_count - 1, 0)"));
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
