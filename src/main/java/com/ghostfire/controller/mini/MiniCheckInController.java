package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.config.RateLimit;
import com.ghostfire.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mini/checkin")
@RequiredArgsConstructor
public class MiniCheckInController {

    private final CheckInService checkInService;

    @RateLimit(key = "mini-checkin", window = 86400, maxCount = 100)
    @PostMapping
    public Result<?> checkIn() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(checkInService.checkIn(userId));
    }
}
