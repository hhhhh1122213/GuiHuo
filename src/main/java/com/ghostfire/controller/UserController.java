package com.ghostfire.controller;

import com.ghostfire.common.Result;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import com.ghostfire.service.UserService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatService userStatService;

    @GetMapping("/{id}")
    public Result<?> profile(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        UserStat stat = userStatService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("stat", stat);
        return Result.ok(result);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User form) {
        User user = userService.getById(form.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (form.getNickname() != null) user.setNickname(form.getNickname());
        if (form.getAvatar() != null) user.setAvatar(form.getAvatar());
        userService.updateById(user);
        return Result.ok();
    }
}
