package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.dto.LoginDto;
import com.ghostfire.dto.RegisterDto;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import com.ghostfire.service.UserService;
import com.ghostfire.service.UserStatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserStatService userStatService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDto dto) {
        System.out.println("我登陆了");
        User user = userService.login(dto.getUsername(), dto.getPassword());
        StpUtil.login(user.getId());
        return Result.ok(StpUtil.getTokenInfo());
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDto dto) {
        userService.register(dto);
        return Result.ok();
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/info")
    public Result<?> info() {
        System.out.println("请求进来了");
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        user.setPassword(null);
        UserStat stat = userStatService.getById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("stat", stat);
        return Result.ok(result);
    }
}
