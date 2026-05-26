package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.config.RateLimit;
import com.ghostfire.dto.LoginDto;
import com.ghostfire.dto.PasswordDto;
import com.ghostfire.dto.RegisterDto;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import com.ghostfire.service.UserService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.vo.AuthInfoMapper;
import com.ghostfire.vo.AuthInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserStatService userStatService;
    private final AuthInfoMapper authInfoMapper;

    @RateLimit(key = "login", window = 300, maxCount = 5)
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDto dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        StpUtil.login(user.getId());
        return Result.ok(StpUtil.getTokenInfo());
    }

    @RateLimit(key = "register", window = 3600, maxCount = 3)
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
    public Result<AuthInfoVO> info() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        UserStat stat = userStatService.getById(userId);

        AuthInfoVO vo = authInfoMapper.toVO(user);
        authInfoMapper.enrich(vo, stat);

        return Result.ok(vo);
    }
    @PostMapping("/password")
    public Result<?> changePassword(@Valid @RequestBody PasswordDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.ok();
    }

}
