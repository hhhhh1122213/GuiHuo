package com.ghostfire.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ghostfire.entity.Post;
import com.ghostfire.service.PostService;
import com.ghostfire.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlushTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostService postService;
    private final RankingService rankingService;

    private static final String VIEW_COUNT_PREFIX = "post:views:";

    @Scheduled(fixedDelayString = "${app.view-flush-interval:30000}")
    public void flush() {
        ScanOptions options = ScanOptions.scanOptions()
                .match(VIEW_COUNT_PREFIX + "*")
                .count(100)
                .build();

        Map<Long, Integer> viewCounts = new HashMap<>();
        try (var cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                Long postId = Long.parseLong(key.substring(VIEW_COUNT_PREFIX.length()));
                Object val = redisTemplate.opsForValue().get(key);
                if (val != null) {
                    viewCounts.put(postId, Integer.parseInt(val.toString()));
                }
            }
        }

        if (viewCounts.isEmpty()) {
            return;
        }

        // 批量更新 DB
        for (Map.Entry<Long, Integer> entry : viewCounts.entrySet()) {
            postService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getId, entry.getKey())
                            .setSql("view_count = view_count + {0}", entry.getValue()));
            // 删掉已同步的 key
            redisTemplate.delete(VIEW_COUNT_PREFIX + entry.getKey());
        }

        log.debug("浏览量刷库完成，同步 {} 篇帖子", viewCounts.size());

        // 更新热榜分数
        for (Long postId : viewCounts.keySet()) {
            Post post = postService.getById(postId);
            if (post != null) {
                rankingService.updateScore(RankingService.RANK_HOT_POSTS, postId,
                        RankingService.calcHotScore(post));
            }
        }
    }
}
