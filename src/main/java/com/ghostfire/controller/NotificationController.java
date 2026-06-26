package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.config.RateLimit;
import com.ghostfire.handler.SseConnectionManager;
import com.ghostfire.service.MessageService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseConnectionManager sseConnectionManager;
    private final MessageService messageService;

    /** 全局共享心跳线程池（4 线程，避免每连接一个线程的无界增长） */
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(4, r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    public void shutdown() {
        heartbeatExecutor.shutdownNow();
    }

    @GetMapping("/subscribe")
    @RateLimit(key = "sse:subscribe", window = 60, maxCount = 2000)
    @SuppressWarnings("AutoCloseableResource")
    public SseEmitter subscribe(@RequestParam String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            throw new RuntimeException("未登录或 token 已过期");
        }
        long userId = Long.parseLong(loginId.toString());

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        sseConnectionManager.register(userId, emitter);

        // 立即推送当前未读统计
        try {
            emitter.send(SseEmitter.event().data(messageService.getUnreadStats(userId)));
        } catch (IOException e) {
            sseConnectionManager.remove(userId, emitter);
            return emitter;
        }

        // 心跳保活（提交到共享线程池，每 30s 一次）
        ScheduledFuture<?> heartbeatFuture = heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException ignored) {
                // 连接已关闭，onCompletion/onError 会清理 heartbeatFuture
            }
        }, 30, 30, TimeUnit.SECONDS);

        Runnable cleanup = () -> {
            heartbeatFuture.cancel(false);
            sseConnectionManager.remove(userId, emitter);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }
}
