package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.config.RateLimit;
import com.ghostfire.service.WxAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mini/auth")
@RequiredArgsConstructor
public class MiniAuthController {

    private final WxAuthService wxAuthService;

    @Data
    public static class WxLoginDto {
        private String code;
    }

    @RateLimit(key = "wx-login", window = 60, maxCount = 20000)
    @PostMapping("/wx-login")
    public Result<?> wxLogin(@RequestBody WxLoginDto dto) {
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            return Result.fail("code 不能为空");
        }
        return Result.ok(wxAuthService.wxLogin(dto.getCode()));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.ok();
    }
}
