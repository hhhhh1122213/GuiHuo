package com.ghostfire.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserStat;
import com.ghostfire.service.PostService;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingInitTask implements ApplicationRunner {

    private final UserStatService userStatService;
    private final RankingService rankingService;
    private final PostService postService;

    @Override
    public void run(ApplicationArguments args) {
        List<UserStat> stats = userStatService.list(
                new LambdaQueryWrapper<UserStat>().isNotNull(UserStat::getUserId));
        if (stats.isEmpty()) {
            return;
        }
        for (UserStat stat : stats) {
            Long uid = stat.getUserId();
            rankingService.updateScore(RankingService.RANK_COIN, uid,
                    stat.getCoin() != null ? stat.getCoin() : 0);
            rankingService.updateScore(RankingService.RANK_LIKE, uid,
                    stat.getLikeCount() != null ? stat.getLikeCount() : 0);
            rankingService.updateScore(RankingService.RANK_POST, uid,
                    stat.getPostCount() != null ? stat.getPostCount() : 0);
        }
        log.info("排行榜初始化完成，加载 {} 个用户", stats.size());

        // 预热热榜
        List<Post> recentPosts = postService.list(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .ge(Post::getCreateTime, LocalDateTime.now().minusDays(30)));
        for (Post post : recentPosts) {
            double score = RankingService.calcHotScore(post);
            rankingService.updateScore(RankingService.RANK_HOT_POSTS, post.getId(), score);
        }
        log.info("热榜预热完成，加载 {} 篇帖子", recentPosts.size());
    }
}
