-- V151: 修复站内弹窗渠道类型误标为 direct 导致的通知失败脏数据
-- 问题: ops.notify_channel 中 '站内弹窗' 的 channel_type 被错误标为 'direct',
--       而 DirectNotifyServiceImpl 仅处理 channel_type=direct 渠道(钉钉/企微/邮件/短信),
--       站内弹窗进入其 switch 的 default 分支 -> '未知渠道类型: inapp' -> 写入 6975 条失败记录。
--       站内弹窗本属 NotificationService 的站内信收件箱(SSE 推送)体系, 不应走点对点外发。
-- 修复: 1) 站内弹窗 channel_type 改为 inapp(非 direct, 被 DirectNotifyServiceImpl 自然跳过)
--       2) 清理 notify_message 中 站内弹窗 渠道的历史失败/发送中脏数据(用户站内信已正常到达)

-- 1) 修正渠道类型(幂等)
UPDATE ops.notify_channel
   SET channel_type = 'inapp'
 WHERE channel = '站内弹窗'
   AND channel_type = 'direct';

-- 2) 清理站内弹窗渠道的脏发送记录(这些记录对用户无实际意义, 站内信走 sys_notification)
DELETE FROM ops.notify_message
 WHERE channel = '站内弹窗';
