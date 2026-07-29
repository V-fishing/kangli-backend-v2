package com.konli.qms.service.notify;

import com.konli.qms.domain.notify.entity.SysNotification;
import lombok.Getter;

/**
 * 通知创建事件，用于 SSE 实时推送。
 * 在 NotificationService.insert() 写库后发布，SSE 监听器推送给对应 userId 的已连接客户端。
 */
@Getter
public class NotificationCreatedEvent {

    private final SysNotification notification;

    public NotificationCreatedEvent(SysNotification notification) {
        this.notification = notification;
    }
}
