package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.entity.User;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final UserService userService;

    @GetMapping("/coin")
    public Result<List<Map<String, Object>>> coin(
            @RequestParam(defaultValue = "10") int top) {
        return Result.ok(buildRanking(RankingService.RANK_COIN, top));
    }

    @GetMapping("/like")
    public Result<List<Map<String, Object>>> like(
            @RequestParam(defaultValue = "10") int top) {
        return Result.ok(buildRanking(RankingService.RANK_LIKE, top));
    }

    @GetMapping("/post")
    public Result<List<Map<String, Object>>> post(
            @RequestParam(defaultValue = "10") int top) {
        return Result.ok(buildRanking(RankingService.RANK_POST, top));
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> my() {
        long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> data = new HashMap<>();
        data.put("coinRank", rankingService.getRank(RankingService.RANK_COIN, userId));
        data.put("coinScore", rankingService.getScore(RankingService.RANK_COIN, userId));
        data.put("likeRank", rankingService.getRank(RankingService.RANK_LIKE, userId));
        data.put("likeScore", rankingService.getScore(RankingService.RANK_LIKE, userId));
        data.put("postRank", rankingService.getRank(RankingService.RANK_POST, userId));
        data.put("postScore", rankingService.getScore(RankingService.RANK_POST, userId));
        return Result.ok(data);
    }

    private List<Map<String, Object>> buildRanking(String key, int top) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = rankingService.topN(key, top);
        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            Object value = tuple.getValue();
            if (!(value instanceof Number number)) {
                continue;
            }
            Long uid = number.longValue();
            User user = userService.getById(uid);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("userId", uid);
            item.put("nickname", user != null ? user.getNickname() : "未知用户");
            item.put("avatar", user != null ? user.getAvatar() : null);
            item.put("score", tuple.getScore());
            result.add(item);
        }
        return result;
    }
}
