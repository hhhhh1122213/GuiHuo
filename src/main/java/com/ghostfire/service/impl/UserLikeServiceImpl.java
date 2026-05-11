package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Comment;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserLike;
import com.ghostfire.entity.UserStat;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.mapper.UserLikeMapper;
import com.ghostfire.service.CommentService;
import com.ghostfire.service.PostService;
import com.ghostfire.service.UserLikeService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.UserWalletLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl extends ServiceImpl<UserLikeMapper, UserLike> implements UserLikeService {

    private final PostService postService;
    private final CommentService commentService;
    private final UserStatService userStatService;
    private final UserWalletLogService userWalletLogService;

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
                // 原子更新作者金币和被赞数
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, post.getUserId())
                                .setSql("coin = coin + 2")
                                .setSql("like_count = like_count + 1"));
                // 读取更新后的余额写流水
                UserStat updatedStat = userStatService.getById(post.getUserId());
                UserWalletLog walletLog = new UserWalletLog();
                walletLog.setUserId(post.getUserId());
                walletLog.setAmount(2L);
                walletLog.setCurrentBalance(updatedStat != null ? updatedStat.getCoin() : 2L);
                walletLog.setType(Constant.WALLET_LIKE);
                userWalletLogService.save(walletLog);
            }
        } else if (targetType == Constant.LIKE_COMMENT) {
            commentService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getId, targetId)
                            .setSql("like_count = like_count + 1"));
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
                userStatService.getBaseMapper().update(null,
                        new LambdaUpdateWrapper<UserStat>()
                                .eq(UserStat::getUserId, post.getUserId())
                                .setSql("coin = GREATEST(coin - 2, 0)")
                                .setSql("like_count = GREATEST(like_count - 1, 0)"));
                UserStat updatedStat = userStatService.getById(post.getUserId());
                if (updatedStat != null) {
                    UserWalletLog walletLog = new UserWalletLog();
                    walletLog.setUserId(post.getUserId());
                    walletLog.setAmount(-2L);
                    walletLog.setCurrentBalance(updatedStat.getCoin());
                    walletLog.setType(Constant.WALLET_LIKE);
                    userWalletLogService.save(walletLog);
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
