package com.konli.qms.service.notify;

import com.konli.qms.domain.notify.entity.NotifyMessage;

import java.util.List;

/**
 * 点对点通知发送核心。
 * 仅处理 channel_type=direct 的渠道(钉钉应用消息/企业微信应用消息/邮件/短信),
 * 异步外发并把发送记录写入 ops.notify_message, 失败不抛异常(记录 fail_reason)。
 */
public interface DirectNotifyService {

    /** 接收人信息(用于邮件/短信/钉钉·企微桥接)。 */
    record DirectReceiver(String userId, String realName, String username, String email, String phone) {}

    /**
     * 向单个用户按渠道列表点对点发送通知(异步)。
     *
     * @param channels 渠道名列表(仅 direct 且启用者生效)
     * @return 本次创建的发送记录(可能为空: 无可用渠道/接收人为空时)
     */
    List<NotifyMessage> sendToUser(String orgId, String senderId, String senderName,
                                   DirectReceiver receiver, List<String> channels,
                                   String title, String content,
                                   String bizType, String bizId, String bizNo);
}
