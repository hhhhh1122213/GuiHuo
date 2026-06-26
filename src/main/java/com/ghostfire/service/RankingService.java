package com.ghostfire.service;

import com.ghostfire.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String RANK_COIN = "rank:coin";
    public static final String RANK_LIKE = "rank:like";
    public static final String RANK_POST = "rank:post";
    public static final String RANK_HOT_POSTS = "rank:hot:posts";

    public static double calcHotScore(Post post) {
        long ageHours = Duration.between(post.getCreateTime(), LocalDateTime.now()).toHours();
        if (ageHours < 0) ageHours = 0;
        return (post.getLikeCount() != null ? post.getLikeCount() : 0) * 5
             + (post.getCommentCount() != null ? post.getCommentCount() : 0) * 8
             + (post.getViewCount() != null ? post.getViewCount() : 0) * 1
             + (Boolean.TRUE.equals(post.getIsEssence()) ? 200 : 0)
             + (Boolean.TRUE.equals(post.getIsTop()) ? 500 : 0)
             - ageHours * 2;
    }

    /** 更新用户在排行榜中的分数 */
    public void updateScore(String key, Long userId, double score) {
        redisTemplate.opsForZSet().add(key, userId, score);
    }

    /** 查询 Top N */
    public Set<ZSetOperations.TypedTuple<Object>> topN(String key, int n) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);
    }

    /** 查询用户排名（从 0 开始，返回 -1 表示未上榜） */
    public Long getRank(String key, Long userId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId);
        return rank != null ? rank + 1 : -1L; // 转为从 1 开始
    }

    /** 查询用户分数 */
    public Double getScore(String key, Long userId) {
        return redisTemplate.opsForZSet().score(key, userId);
    }

    /** 用户在三个榜的排名 */
    public void initScores(String key, List<Long> userIds, List<Double> scores) {
        for (int i = 0; i < userIds.size(); i++) {
            redisTemplate.opsForZSet().add(key, userIds.get(i), scores.get(i));
        }
    }
}
