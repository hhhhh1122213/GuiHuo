package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserStat;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.service.PostService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.UserWalletLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserStatService userStatService;
    private final UserWalletLogService userWalletLogService;

    @Override
    public Page<Post> pageByCategory(Long categoryId, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getCategoryId, categoryId)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> pageLatest(int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> pageEssence(int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getIsEssence, true)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> search(String keyword, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .and(wr -> wr.like(Post::getTitle, keyword)
                        .or()
                        .like(Post::getContent, keyword))
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public void addViewCount(Long postId) {
        getBaseMapper().update(null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .setSql("view_count = view_count + 1"));
    }

    @Override
    @Transactional
    public Post createPost(Long userId, PostDto dto) {
        Post post = new Post();
        post.setCategoryId(dto.getCategoryId());
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setStatus(Constant.POST_STATUS_NORMAL);
        save(post);

        // 先确保 UserStat 存在
        UserStat userStat = userStatService.getById(userId);
        if (userStat == null) {
            userStat = new UserStat();
            userStat.setUserId(userId);
            userStat.setCoin(0L);
            userStat.setPostCount(0);
            userStat.setLikeCount(0);
            userStat.setSignCount(0);
            userStatService.save(userStat);
        }
        // 原子更新金币和发帖数
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("coin = coin + 10")
                        .setSql("post_count = post_count + 1"));

        UserStat updatedStat = userStatService.getById(userId);
        UserWalletLog walletLog = new UserWalletLog();
        walletLog.setUserId(userId);
        walletLog.setAmount(10L);
        walletLog.setCurrentBalance(updatedStat != null ? updatedStat.getCoin() : 10L);
        walletLog.setType(Constant.WALLET_POST);
        userWalletLogService.save(walletLog);

        return post;
    }

    @Override
    @Transactional
    public void deletePost(Post post, Long userId) {
        post.setStatus(Constant.POST_STATUS_DELETED);
        updateById(post);
        UserStat userStat = userStatService.getById(userId);
        if (userStat != null && userStat.getPostCount() > 0) {
            userStat.setPostCount(userStat.getPostCount() - 1);
            userStatService.updateById(userStat);
        }
    }
}
