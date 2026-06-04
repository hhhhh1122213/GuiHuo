package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.entity.Post;
import com.ghostfire.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

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

    @GetMapping("/list")
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userFavoriteService.pageFavorites(userId, page, size));
    }
}
