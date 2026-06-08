package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.PostTagService;
import com.ghostfire.service.PostService;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserStatService userStatService;
    private final MedalService medalService;
    private final RankingService rankingService;
    private final PostTagService postTagService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VIEW_COUNT_PREFIX = "post:views:";

    @Override

    public Page<Post> pageByCategory(Long categoryId, int page, int size) {
        return pageFeed(categoryId, page, size, "recommend");
    }

    @Override
    public Page<Post> pageByTag(Long tagId, int page, int size) {
        return baseMapper.selectByTag(tagId, new Page<>(page, size));
    }

    @Override
    public Page<Post> pageLatest(int page, int size) {
        return pageFeed(null, page, size, "latest");
    }

    @Override
    public Page<Post> pageFeed(Long categoryId, int page, int size, String sort) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(categoryId != null, Post::getCategoryId, categoryId)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL);
        applyFeedSort(w, sort);
        return page(p, w);
    }

    @Override
    public Page<Post> pageEssence(int page, int size) {
        return pageEssence(null, page, size);
    }

    @Override
    public Page<Post> pageEssence(Long categoryId, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getIsEssence, true)
                .eq(categoryId != null, Post::getCategoryId, categoryId)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    private void applyFeedSort(LambdaQueryWrapper<Post> w, String sort) {
        String normalized = sort == null ? "recommend" : sort.trim().toLowerCase();
        switch (normalized) {
            case "latest" -> w
                    .orderByDesc(Post::getCreateTime)
                    .orderByDesc(Post::getId);
            case "hot" -> w
                    .orderByDesc(Post::getIsTop)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getViewCount)
                    .orderByDesc(Post::getCreateTime);
            default -> w
                    .orderByDesc(Post::getIsTop)
                    .orderByDesc(Post::getIsEssence)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getViewCount)
                    .orderByDesc(Post::getCreateTime);
        }
    }

    @Override
    public Page<Post> search(String keyword, int page, int size) {
        Page<Post> p = new Page<>(page, size, false); // 跳过自动 count
        p = baseMapper.searchFullText(keyword, p);
        p.setTotal(baseMapper.searchFullTextCount(keyword)); // 手动查总数
        return p;
    }

    @Override
    public void addViewCount(Long postId) {
        redisTemplate.opsForValue().increment(VIEW_COUNT_PREFIX + postId);
    }

    @Override
    public long getViewCountDelta(Long postId) {
        Object val = redisTemplate.opsForValue().get(VIEW_COUNT_PREFIX + postId);
        return val != null ? Long.parseLong(val.toString()) : 0;
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
        postTagService.replacePostTags(post.getId(), dto.getTags());

        // 确保 UserStat 存在
        UserStat userStat = userStatService.getById(userId);
        if (userStat == null) {
            userStat = new UserStat();
            userStat.setUserId(userId);
            userStat.setCoin(0L);
            userStat.setPostCount(0);
            userStat.setLikeCount(0);
            userStat.setSignCount(0);
            userStat.setStreakCount(0);
            try {
                userStatService.save(userStat);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发创建时忽略，后续原子 SQL 更新不受影响
            }
        }
        // 加金币 + 写流水 + 更新发帖数
        userStatService.addCoin(userId, Constant.COIN_POST_REWARD, Constant.WALLET_POST, post.getId());
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("post_count = post_count + 1"));
        // 更新发帖排行榜
        UserStat updated = userStatService.getById(userId);
        if (updated != null) {
            rankingService.updateScore(RankingService.RANK_POST, userId,
                    updated.getPostCount() != null ? updated.getPostCount() : 0);
        }
        medalService.checkAutoAward(userId);

        return post;
    }

    @Override
    @Transactional
    public void deletePost(Post post, Long userId) {
        post.setStatus(Constant.POST_STATUS_DELETED);
        updateById(post);
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("post_count = GREATEST(post_count - 1, 0)"));
        UserStat userStat = userStatService.getById(userId);
        if (userStat != null) {
            rankingService.updateScore(RankingService.RANK_POST, userId,
                    userStat.getPostCount() != null ? userStat.getPostCount() : 0);
        }
    }

    @Override
    @Transactional
    public void editPost(long id, PostDto dto, Post post) {
        if (dto.getTitle() != null)
            post.setTitle(dto.getTitle());
        if (dto.getContent() != null)
            post.setContent(dto.getContent());
        if (dto.getCategoryId() != null)
            post.setCategoryId(dto.getCategoryId());
        updateById(post);
        postTagService.replacePostTags(id, dto.getTags());
    }
}
