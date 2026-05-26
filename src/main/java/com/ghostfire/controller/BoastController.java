package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.BoastDto;
import com.ghostfire.entity.Boast;
import com.ghostfire.entity.BoastBet;
import com.ghostfire.service.BoastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boast")
@RequiredArgsConstructor
public class BoastController {
    private final BoastService boastService;

    /** 发起挑战 */
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody BoastDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Boast boast = boastService.create(dto, userId);
        return Result.ok(boast);
    }

    /** 挑战列表（status: 0=进行中, 1=已结束, 不传=全部） */
    @GetMapping("/list")
    public Result<Page<Boast>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(boastService.list(page, size, status));
    }

    /** 挑战详情 */
    @GetMapping("/{id}")
    public Result<Boast> detail(@PathVariable Long id) {
        return Result.ok(boastService.detail(id));
    }

    /** 参与挑战（选选项，投赌注） */
    @PostMapping("/bet")
    public Result<?> bet(@RequestParam Long boastId, @RequestParam Integer optionType) {
        long userId = StpUtil.getLoginIdAsLong();
        try {
            BoastBet bet = boastService.bet(boastId, optionType, userId);
            return Result.ok(bet);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 结算挑战（发布者或管理员操作，1=发布者赢, 2=挑战者赢） */
    @PostMapping("/settle/{id}")
    public Result<?> settle(@PathVariable Long id, @RequestParam Integer result) {
        long userId = StpUtil.getLoginIdAsLong();
        try {
            Boast boast = boastService.settle(id, result, userId);
            return Result.ok(boast);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
