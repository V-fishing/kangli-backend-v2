-- ============================================================
-- V220 工装(TLM) 维修验证不通过锁定通知配置(跨端协同强约束 C2, 需求 2.5.3.3 深度闭环)。
-- 事件: tlm_repair_verify_fail(工装维修后验证不通过自动锁定)。
-- 接收角色: sqe; 渠道站内弹窗。全部幂等, 可重复执行。
-- ============================================================

INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('tlm','tlm_repair_verify_fail', '工装验证不通过已锁定', 'sqe', '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='tlm' AND event_code=v.event_code);
