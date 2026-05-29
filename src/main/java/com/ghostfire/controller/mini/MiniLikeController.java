package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.service.UserLikeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mini/like")
@RequiredArgsConstructor
public class MiniLikeController {

    private final UserLikeService userLikeService;

    @PostMapping
    public Result<?> like(@RequestParam Long targetId,
                          @RequestParam @Min(1) @Max(2) Integer targetType) {
        long userId = StpUtil.getLoginIdAsLong();
        userLikeService.like(userId, targetId, targetType);
        return Result.ok();
    }

    @DeleteMapping
    public Result<?> unlike(@RequestParam Long targetId,
                            @RequestParam @Min(1) @Max(2) Integer targetType) {
        long userId = StpUtil.getLoginIdAsLong();
        userLikeService.unlike(userId, targetId, targetType);
        return Result.ok();
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long targetId,
                                 @RequestParam @Min(1) @Max(2) Integer targetType) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userLikeService.isLiked(userId, targetId, targetType));
    }
}
