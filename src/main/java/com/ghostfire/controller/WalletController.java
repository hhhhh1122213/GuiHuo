package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.UserWalletLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final UserWalletLogService userWalletLogService;
    private final UserStatService userStatService;

    @GetMapping("/logs")
    public Result<Page<UserWalletLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userWalletLogService.pageByUserId(userId, page, size));
    }

    @GetMapping("/balance")
    public Result<Long> balance() {
        long userId = StpUtil.getLoginIdAsLong();
        var userStat = userStatService.getById(userId);
        Long coin = userStat != null ? userStat.getCoin() : 0L;
        return Result.ok(coin);
    }
}
