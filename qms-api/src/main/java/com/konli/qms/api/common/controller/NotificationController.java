package com.konli.qms.api.common.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.notify.entity.SysNotification;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 全平台站内消息中心:收件箱/未读计数/标记已读。 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public R<List<SysNotification>> list() {
        return R.ok(notificationService.listMine());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public R<Long> unreadCount() {
        return R.ok(notificationService.unreadCount());
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public R<Void> read(@PathVariable String id) {
        notificationService.markRead(id);
        return R.ok();
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public R<Void> readAll() {
        notificationService.markAllRead();
        return R.ok();
    }
}
