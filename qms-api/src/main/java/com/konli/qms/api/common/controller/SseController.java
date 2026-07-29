package com.konli.qms.api.common.controller;

import com.konli.qms.domain.notify.entity.SysNotification;
import com.konli.qms.service.notify.NotificationCreatedEvent;
import com.konli.qms.service.uop.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 实时推送端点:替代前端 30 秒轮询,在新通知入库时实时推送给已连接的客户端。
 * 支持页面切后台断开、切回前台重连,保持连接生命周期可控。
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final UserService userService;

    /** userId -> 该用户的所有活跃 SSE 连接 */
    private static final Map<String, List<SseEmitter>> CONNECTIONS = new ConcurrentHashMap<>();

    /**
     * 订阅 SSE 实时通知流。
     * 前端使用 EventSource 连接此端点,服务端在新通知到达时推送。
     */
    @GetMapping("/stream")
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        String userId = userService.getCurrent().userId();
        SseEmitter emitter = new SseEmitter(0L); // 无超时,长期连接

        CONNECTIONS.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.debug("[SSE] 用户 {} 建立连接,当前连接数: {}", userId, countFor(userId));

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        // 发送初始连接确认
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {
            remove(userId, emitter);
        }

        return emitter;
    }

    /** 监听通知创建事件,推送给目标用户的所有活跃连接。 */
    @EventListener
    public void onNotificationCreated(NotificationCreatedEvent event) {
        SysNotification n = event.getNotification();
        String userId = n.getUserId();
        if (userId == null) return;

        List<SseEmitter> emitters = CONNECTIONS.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("notification").data(n));
            } catch (IOException ex) {
                remove(userId, e);
            }
        }
    }

    private void remove(String userId, SseEmitter emitter) {
        List<SseEmitter> list = CONNECTIONS.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                CONNECTIONS.remove(userId);
            }
        }
    }

    private int countFor(String userId) {
        List<SseEmitter> list = CONNECTIONS.get(userId);
        return list == null ? 0 : list.size();
    }
}
