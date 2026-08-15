-- ============================================================
-- V210 体系管理(QMS-MGMT) 通知配置种子(跨端协同强约束 C2)。
-- 事件: qms_audit_nc_created(内审不符合项新增) / qms_adverse_created(不良事件登记)。
-- 接收角色: sqe(体系/SQE 角色); 渠道默认站内弹窗(webhook/direct 由系统管理>通知配置补充)。
-- 全部幂等, 可重复执行。
-- ============================================================

INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('qms-mgmt','qms_audit_nc_created', '内审不符合项新增', 'sqe', '站内弹窗', true),
  ('qms-mgmt','qms_adverse_created',  '不良事件登记',     'sqe', '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='qms-mgmt' AND event_code=v.event_code);
