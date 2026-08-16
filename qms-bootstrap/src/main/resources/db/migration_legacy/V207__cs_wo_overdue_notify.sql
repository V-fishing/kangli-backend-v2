-- V207: 售后工单超时预警通知配置种子(跨端协同强约束 C2)。
-- module='cs' 事件 cs_wo_overdue: 工单超过期望时间(expect_time)仍未闭环(PENDING/ASSIGNED)时预警。
-- 接收人: sqe 角色(由 notify 按配置解析); 同时服务在 ASSIGNED 时额外点对点推送给负责人。
-- 全部幂等。
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('cs','cs_wo_overdue', '售后工单超时预警', 'sqe', '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='cs' AND event_code=v.event_code);
