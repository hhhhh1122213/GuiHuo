package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.handler.SseConnectionManager;
import com.ghostfire.service.MessageService;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseConnectionManager sseConnectionManager;
    private final MessageService messageService;

    @GetMapping("/subscribe")
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

        // 心跳保活
        ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor();
        heartbeat.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException e) {
                sseConnectionManager.remove(userId, emitter);
                heartbeat.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            sseConnectionManager.remove(userId, emitter);
            heartbeat.shutdown();
            log.debug("SSE 完成: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            sseConnectionManager.remove(userId, emitter);
            heartbeat.shutdown();
            log.debug("SSE 超时: userId={}", userId);
        });

        emitter.onError(e -> {
            sseConnectionManager.remove(userId, emitter);
            heartbeat.shutdown();
            log.debug("SSE 错误: userId={}", userId, e);
        });

        return emitter;
    }
}
