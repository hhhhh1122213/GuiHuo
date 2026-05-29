package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mini/favorite")
@RequiredArgsConstructor
public class MiniFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping
    public Result<?> favorite(@RequestParam Long postId) {
        long userId = StpUtil.getLoginIdAsLong();
        userFavoriteService.favorite(userId, postId);
        return Result.ok();
    }

    @DeleteMapping
    public Result<?> unfavorite(@RequestParam Long postId) {
        long userId = StpUtil.getLoginIdAsLong();
        userFavoriteService.unfavorite(userId, postId);
        return Result.ok();
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long postId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userFavoriteService.isFavorited(userId, postId));
    }
}
