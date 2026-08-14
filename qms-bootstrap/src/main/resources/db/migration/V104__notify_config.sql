-- V104: 统一通知配置
-- 1) ops.notify_channel : 全局外部渠道(钉钉/企微/自定义webhook/站内弹窗)
-- 2) ops.notify_config  : 模块×事件 的通知接收角色与渠道配置(替代各模块硬编码 roleCode)
-- 说明: 现有 spc_notify_channel(SPC判异报警专用)保留不动; 本表覆盖其余模块及 SPC 采集任务通知。

-- ============ 1. 外部渠道表 ============
CREATE TABLE IF NOT EXISTS ops.notify_channel (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  channel     VARCHAR(32) NOT NULL UNIQUE,
  webhook_url TEXT,
  is_enabled  BOOLEAN NOT NULL DEFAULT false,
  level       VARCHAR(16) DEFAULT '普通',
  remark      VARCHAR(128)
);
COMMENT ON TABLE ops.notify_channel IS '全局通知外部渠道(钉钉/企微/自定义webhook)';

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark)
SELECT '站内弹窗', NULL, true, '普通', '系统站内信(默认开启)'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='站内弹窗');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark)
SELECT '企业微信', '', false, '普通', '企业微信机器人 webhook'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='企业微信');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark)
SELECT '钉钉', '', false, '普通', '钉钉机器人 webhook'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='钉钉');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark)
SELECT '自定义Webhook', '', false, '普通', '自定义 webhook 地址'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='自定义Webhook');

-- ============ 2. 通知配置表 ============
CREATE TABLE IF NOT EXISTS ops.notify_config (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  module      VARCHAR(32) NOT NULL,
  event_code  VARCHAR(64) NOT NULL,
  event_name  VARCHAR(128) NOT NULL,
  role_codes  VARCHAR(256),                       -- 逗号分隔角色码; 空=不发站内信
  channels    VARCHAR(256) DEFAULT '站内弹窗',     -- 逗号分隔渠道名; 默认仅站内弹窗
  enabled     BOOLEAN NOT NULL DEFAULT true,
  org_id      UUID,                                -- 空=全局
  UNIQUE (module, event_code)
);
COMMENT ON TABLE ops.notify_config IS '统一通知配置: 模块×事件 的接收角色与外部渠道';

-- 注: 历史代码大量写死 "supervisor", 但 sys_role 实际班组长 role_code 为 "shiftleader",
--     本种子按正确角色码写入, 同步修正原通知静默丢失问题。
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  -- ===== NCM =====
  ('ncm','ncm_8d_status',       '8D 报告状态变更',     'qmanager,sqe',            '站内弹窗', true),
  ('ncm','ncm_capa_created',    '纠正措施已创建',       'qmanager,sqe',            '站内弹窗', true),
  ('ncm','ncm_capa_completed',  '纠正措施已完成',       'qmanager,sqe',            '站内弹窗', true),
  ('ncm','ncm_capa_closed',     '纠正措施已关闭',       'qmanager,sqe',            '站内弹窗', true),
  -- ===== SQM =====
  ('sqm','sqm_audit_plan_created','审核计划已创建',     'qmanager,sqe',            '站内弹窗', true),
  ('sqm','sqm_audit_plan_started','审核计划已开始',     'qmanager,sqe',            '站内弹窗', true),
  ('sqm','sqm_audit_nc_major',  '审核严重不符合项',     'qmanager,sqe',            '站内弹窗', true),
  ('sqm','sqm_audit_nc_closed', '审核不符合项已闭环',   'qmanager,sqe',            '站内弹窗', true),
  ('sqm','sqm_audit_nc_overdue','审核NC整改超期',       'qmanager,purchaser',      '站内弹窗', true),
  ('sqm','sqm_abnormal_overdue','来料异常超期提醒',     'sqe',                     '站内弹窗', true),
  ('sqm','sqm_abnormal_overdue_escalate','来料异常超期升级(≥14天)','qmanager,purchaser','站内弹窗', true),
  ('sqm','sqm_fmea_overdue',    'FMEA措施超期提醒',     'sqe',                     '站内弹窗', true),
  ('sqm','sqm_fmea_overdue_escalate','FMEA措施超期升级(≥14天)','qmanager',         '站内弹窗', true),
  ('sqm','sqm_change_submitted', '物料变更待审批',       'purchaser,rd,sqe',        '站内弹窗', true),
  ('sqm','sqm_change_result',    '物料变更审批结果',     'purchaser,rd,sqe',        '站内弹窗', true),
  -- ===== FIA =====
  ('fia','fia_task_created',     '首件任务创建待检',     'inspector,shiftleader',   '站内弹窗', true),
  ('fia','fia_task_released',   '首件判定合格放行',     'inspector,shiftleader,qmanager','站内弹窗', true),
  ('fia','fia_task_rejected',   '首件判定不合格拦截',   'qmanager,shiftleader',     '站内弹窗', true),
  -- ===== Patrol 巡检 =====
  ('patrol','patrol_task_created','巡检任务已创建',     'inspector,shiftleader',   '站内弹窗', true),
  ('patrol','patrol_task_abnormal','巡检点检查异常',     'qmanager,sqe',            '站内弹窗', true),
  ('patrol','patrol_task_completed','巡检任务全部完成', 'inspector,shiftleader,qmanager','站内弹窗', true),
  ('patrol','patrol_task_closed','巡检任务手动关闭',     'inspector,shiftleader,qmanager','站内弹窗', true),
  -- ===== Schedule 定时超期扫描 =====
  ('schedule','patrol_overdue', '巡检任务超期',         'inspector,shiftleader,qmanager','站内弹窗', true),
  ('schedule','fia_overdue',    '首件检验超期',         'inspector,shiftleader,qmanager','站内弹窗', true),
  ('schedule','audit_nc_overdue','审核NC整改超期',      'qmanager,purchaser',      '站内弹窗', true),
  ('schedule','capa_overdue',   '纠正措施超期',         'qmanager,sqe',            '站内弹窗', true),
  -- ===== SPC 采集任务(原写 notification_log, 改为统一配置) =====
  ('spc','spc_collect_assigned','SPC采集任务下发',      'shiftleader',              '站内弹窗', true),
  ('spc','spc_collect_missing', 'SPC采集缺失告警',      'shiftleader',              '站内弹窗', true),
  ('spc','spc_collect_due_soon','SPC采集临期提醒',      'shiftleader',              '站内弹窗', true),
  ('spc','spc_collect_done',    'SPC采集完成回执',      'shiftleader',              '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (
  SELECT 1 FROM ops.notify_config c WHERE c.module=v.module AND c.event_code=v.event_code
);
