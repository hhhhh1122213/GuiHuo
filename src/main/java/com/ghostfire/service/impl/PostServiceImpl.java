package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.config.RedisStreamConfig;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.entity.PostTag;
import com.ghostfire.handler.SensitiveWordFilter;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.PostTagService;
import com.ghostfire.service.PostService;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserStatService userStatService;
    private final MedalService medalService;
    private final RankingService rankingService;
    private final PostTagService postTagService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WalletService walletService;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final MessageService messageService;

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
        String normalized = sort == null ? "recommend" : sort.trim().toLowerCase();

        // 热门排序第 1 页且无分类筛选时，优先读 ZSet 缓存
        if ("hot".equals(normalized) && page == 1 && categoryId == null) {
            Set<Object> topIds = redisTemplate.opsForZSet()
                    .reverseRange(RankingService.RANK_HOT_POSTS, 0, size - 1);
            if (topIds != null && !topIds.isEmpty()) {
                List<Long> ids = topIds.stream().map(o -> Long.parseLong(o.toString())).toList();
                List<Post> posts = baseMapper.selectBatchIds(ids);
                Map<Long, Post> map = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
                List<Post> ordered = ids.stream().map(map::get).filter(Objects::nonNull).toList();
                return new Page<Post>(page, size, ordered.size()).setRecords(ordered);
            }
        }

        // 兜底：ZSet 未命中（冷启动/Redis 故障）走原 DB 排序
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
        // 敏感词过滤
        SensitiveWordFilter.FilterResult filterResult = sensitiveWordFilter.filter(
                dto.getTitle() + " " + dto.getContent());
        if (!filterResult.pass()) {
            throw new RuntimeException("内容包含违规信息，发布失败");
        }

        Post post = new Post();
        post.setCategoryId(dto.getCategoryId());
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(filterResult.filteredContent());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setStatus(filterResult.needReview()
                ? Constant.POST_STATUS_PENDING
                : Constant.POST_STATUS_NORMAL);
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

        // 只有审核通过的帖子才发金币 + 更新统计
        if (post.getStatus() == Constant.POST_STATUS_NORMAL) {
            walletService.changeCoin(userId, Constant.COIN_POST_REWARD, Constant.WALLET_POST, post.getId(),
                    "POST:" + post.getId(), "发帖奖励");
            userStatService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<UserStat>()
                            .eq(UserStat::getUserId, userId)
                            .setSql("post_count = post_count + 1"));
            UserStat updated = userStatService.getById(userId);
            if (updated != null) {
                rankingService.updateScore(RankingService.RANK_POST, userId,
                        updated.getPostCount() != null ? updated.getPostCount() : 0);
            }
            medalService.checkAutoAward(userId);
        }

        return post;
    }

    @Transactional
    public void approvePost(Post post) {
        post.setStatus(Constant.POST_STATUS_NORMAL);
        updateById(post);

        // 补发金币 + 统计
        walletService.changeCoin(post.getUserId(), Constant.COIN_POST_REWARD,
                Constant.WALLET_POST, post.getId(),
                "POST:" + post.getId(), "发帖奖励");
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, post.getUserId())
                        .setSql("post_count = post_count + 1"));

        medalService.checkAutoAward(post.getUserId());

        // 更新热榜
        rankingService.updateScore(RankingService.RANK_HOT_POSTS,
                post.getId(), RankingService.calcHotScore(post));

        // 通知作者
        messageService.send(0L, post.getUserId(),
                "帖子《" + post.getTitle() + "》已审核通过",
                Constant.MSG_TYPE_SYSTEM, post.getId());
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
