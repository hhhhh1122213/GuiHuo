package com.ghostfire.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.entity.Medal;
import com.ghostfire.service.MedalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medal")
@RequiredArgsConstructor
public class MedalController {

    private final MedalService medalService;

    /** 全部勋章定义 */
    @GetMapping("/list")
    public Result<List<Medal>> list() {
        return Result.ok(medalService.listAll());
    }

    /** 我的已获得勋章 */
    @GetMapping("/my")
    public Result<List<Medal>> my() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(medalService.listUserMedals(userId));
    }

    /** 管理员手动颁发勋章 */
    @SaCheckRole("ADMIN")
    @PostMapping("/award")
    public Result<?> award(@RequestParam Long userId, @RequestParam Long medalId) {
        medalService.awardManual(userId, medalId);
        return Result.ok();
    }
}
