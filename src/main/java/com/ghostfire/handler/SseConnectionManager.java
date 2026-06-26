package com.ghostfire.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseConnectionManager {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> connections = new ConcurrentHashMap<>();

    public void register(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = connections.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        if (list.size() >= 5) {
            throw new RuntimeException("同一用户 SSE 连接数超限");
        }
        list.add(emitter);
        log.debug("SSE 连接注册: userId={}, 当前连接数={}", userId, list.size());
    }

    public void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = connections.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                connections.remove(userId);
            }
        }
        log.debug("SSE 连接移除: userId={}", userId);
    }

    public void sendToUser(Long userId, Object data) {
        CopyOnWriteArrayList<SseEmitter> list = connections.get(userId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                remove(userId, emitter);
            }
        }
    }
}
