package com.konli.qms.api.common.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.notify.entity.SysNotification;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 个人站内信分页(时间倒序), 供「工作台 → 消息中心」列表页。
     * keyword 模糊匹配标题/内容/类型; unread=true 仅返回未读。原全量 list 保留供顶栏铃铛。
     */
    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public R<PageResult<SysNotification>> listPage(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Boolean unread,
                                                   @RequestParam(required = false) String bizType) {
        return R.ok(notificationService.listMinePage(page, size, keyword, unread, bizType));
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
