-- V206: 售后管理(CS) 通知配置种子(跨端协同强约束 C2)。
-- module='cs' 的事件行, 供 NotificationService.notify(module, eventCode, ...) 按配置解析接收人/渠道。
-- 若缺这些行, notify() 静默不发(配置禁用或查不到)。全部幂等。
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('cs','cs_wo_created',   '售后新工单待处理', 'sqe',     '站内弹窗', true),
  ('cs','cs_wo_assigned',  '售后工单已指派',   'sqe',     '站内弹窗', true),
  ('cs','cs_fb_created',   '客户反馈登记',     'sqe',     '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='cs' AND event_code=v.event_code);
