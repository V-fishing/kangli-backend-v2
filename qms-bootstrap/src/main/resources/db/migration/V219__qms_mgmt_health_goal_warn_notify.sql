-- ============================================================
-- V219 体系管理(QMS-MGMT) 健康度预警 + 质量目标达成率预警 通知配置(跨端协同强约束 C2)。
-- 事件: qms_health_warn(体系健康度低于阈值) / qms_goal_warn(质量目标未达标预警)。
-- 接收角色: sqe(体系/SQE 角色); 渠道默认站内弹窗。
-- 全部幂等, 可重复执行。
-- ============================================================

INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('qms-mgmt','qms_health_warn', '体系健康度预警', 'sqe', '站内弹窗', true),
  ('qms-mgmt','qms_goal_warn',  '质量目标未达标预警', 'sqe', '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='qms-mgmt' AND event_code=v.event_code);
