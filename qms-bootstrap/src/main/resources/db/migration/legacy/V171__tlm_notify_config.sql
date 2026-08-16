-- V171: TLM 工装管理 通知配置种子(跨端协同强约束 C2)。
-- module='tlm' 的事件行, 供 NotificationService.notify(module, eventCode, ...) 按配置解析接收人/渠道。
-- 若缺这些行, notify() 静默不发(配置禁用或查不到)。全部幂等。
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('tlm','tlm_calib_expiry', '测量设备校准到期预警', 'sqe',            '站内弹窗', true),
  ('tlm','tlm_maint_due',    '工装保养到期预警',     'sqe',            '站内弹窗', true),
  ('tlm','tlm_life_over',    '工装寿命超限预警',     'sqe',            '站内弹窗', true),
  ('tlm','tlm_repair_created','工装送修待处理',      'sqe',            '站内弹窗', true),
  ('tlm','tlm_scrap_applied', '工装报废待审批',      'sqe',            '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='tlm' AND event_code=v.event_code);
