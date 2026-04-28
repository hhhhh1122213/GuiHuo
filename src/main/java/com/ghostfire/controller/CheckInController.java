package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public Result<?> checkIn() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(checkInService.checkIn(userId));
    }
}
