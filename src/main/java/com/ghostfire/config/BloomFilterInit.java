package com.ghostfire.config;

import com.ghostfire.entity.User;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时加载已有用户名到布隆过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInit implements ApplicationRunner {

    private static final String BLOOM_USERNAMES = "bloom:usernames";

    private final BloomFilterHelper bloomFilter;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        var users = userService.list();
        for (User user : users) {
            bloomFilter.add(BLOOM_USERNAMES, user.getUsername());
        }
        log.info("布隆过滤器初始化完成，加载 {} 个用户名", users.size());
    }
}
