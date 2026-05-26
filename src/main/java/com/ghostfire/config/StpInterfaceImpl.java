package com.ghostfire.config;

import cn.dev33.satoken.stp.StpInterface;
import com.ghostfire.entity.User;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限/角色认证接口实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        User user = userService.getById(Long.parseLong(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        List<String> permissions = new ArrayList<>();
        if ("ADMIN".equals(user.getRole())) {
            permissions.add("ADMIN");
            permissions.add("USER");
        } else {
            permissions.add("USER");
        }
        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.parseLong(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole());
        return roles;
    }
}
