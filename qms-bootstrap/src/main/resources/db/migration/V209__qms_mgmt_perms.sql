-- ============================================================
-- V209 体系管理(QMS-MGMT) 菜单 + 按钮权限码种子(跨端协同强约束 C1)。
-- 菜单: qms-mgmt(体系管理 目录, sort=12) 下 5 个子模块:
--   qms-mgmt.goal     质量目标管理
--   qms-mgmt.audit    内审数据管理(计划 + 不符合项)
--   qms-mgmt.adverse  不良事件管理
--   qms-mgmt.feedback 顾客反馈分析(复用 CS 反馈数据, 仅查看)
--   qms-mgmt.dashboard体系合规监控看板(聚合分析, 仅查看)
-- 按钮码格式 {module}.{resource}.{action}, 挂各子菜单下。
-- 全部幂等, 可重复执行。sysadmin/admin 全量操作; sqe/operator 仅查看。
-- 注意: 按钮码 menu_id 直接指定(DECLARE 取值), 避免与菜单码同名 JOIN 笛卡尔冲突。
-- ============================================================

-- 1) 体系管理根目录(sort=12, 在 cs=11 之后)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'qms-mgmt', '体系管理', '目录', '/qms-mgmt', NULL, '🗂', 12, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='qms-mgmt');

-- 2) 体系管理子菜单(5 个)
DO $$
DECLARE mgmt_pid uuid;
BEGIN
  SELECT id INTO mgmt_pid FROM ops.sys_menu WHERE menu_code='qms-mgmt';
  IF mgmt_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), mgmt_pid, 'qms-mgmt.goal',      '质量目标管理',   '菜单', '/qms-mgmt/goal',      'qms-mgmt/QualityGoal',      '🎯', 1, true),
      (ops.gen_uuid_v7(), mgmt_pid, 'qms-mgmt.audit',     '内审数据管理',   '菜单', '/qms-mgmt/audit',     'qms-mgmt/InternalAudit',    '🔍', 2, true),
      (ops.gen_uuid_v7(), mgmt_pid, 'qms-mgmt.adverse',   '不良事件管理',   '菜单', '/qms-mgmt/adverse',   'qms-mgmt/AdverseEvent',     '⚠',  3, true),
      (ops.gen_uuid_v7(), mgmt_pid, 'qms-mgmt.feedback',  '顾客反馈分析',   '菜单', '/qms-mgmt/feedback',  'qms-mgmt/FeedbackAnalysis', '💬', 4, true),
      (ops.gen_uuid_v7(), mgmt_pid, 'qms-mgmt.dashboard', '体系合规监控',   '菜单', '/qms-mgmt/dashboard', 'qms-mgmt/ComplianceBoard',  '📈', 5, true)
    ON CONFLICT (menu_code) DO NOTHING;
  END IF;
END $$;

-- 3) 按钮码注册(每个按钮码明确归属对应菜单, 直接指定 menu_id 避免笛卡尔冲突)
DO $$
DECLARE
  goal_mid uuid; audit_mid uuid; adverse_mid uuid; fb_mid uuid; dash_mid uuid;
BEGIN
  SELECT id INTO goal_mid   FROM ops.sys_menu WHERE menu_code='qms-mgmt.goal';
  SELECT id INTO audit_mid  FROM ops.sys_menu WHERE menu_code='qms-mgmt.audit';
  SELECT id INTO adverse_mid FROM ops.sys_menu WHERE menu_code='qms-mgmt.adverse';
  SELECT id INTO fb_mid     FROM ops.sys_menu WHERE menu_code='qms-mgmt.feedback';
  SELECT id INTO dash_mid   FROM ops.sys_menu WHERE menu_code='qms-mgmt.dashboard';

  IF goal_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), goal_mid, 'qms-mgmt.goal.list',   '目标查看'),
      (ops.gen_uuid_v7(), goal_mid, 'qms-mgmt.goal.create', '目标新建'),
      (ops.gen_uuid_v7(), goal_mid, 'qms-mgmt.goal.edit',   '目标编辑'),
      (ops.gen_uuid_v7(), goal_mid, 'qms-mgmt.goal.delete', '目标删除')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;

  IF audit_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), audit_mid, 'qms-mgmt.audit.list',   '内审查看'),
      (ops.gen_uuid_v7(), audit_mid, 'qms-mgmt.audit.create', '内审新建'),
      (ops.gen_uuid_v7(), audit_mid, 'qms-mgmt.audit.edit',   '内审编辑'),
      (ops.gen_uuid_v7(), audit_mid, 'qms-mgmt.audit.delete', '内审删除'),
      (ops.gen_uuid_v7(), audit_mid, 'qms-mgmt.audit.nc',     '不符合项维护')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;

  IF adverse_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), adverse_mid, 'qms-mgmt.adverse.list',   '事件查看'),
      (ops.gen_uuid_v7(), adverse_mid, 'qms-mgmt.adverse.create', '事件登记'),
      (ops.gen_uuid_v7(), adverse_mid, 'qms-mgmt.adverse.edit',   '事件编辑'),
      (ops.gen_uuid_v7(), adverse_mid, 'qms-mgmt.adverse.delete', '事件删除')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;

  IF fb_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), fb_mid, 'qms-mgmt.feedback.list', '反馈分析查看')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;

  IF dash_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), dash_mid, 'qms-mgmt.dashboard.list', '看板查看')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 4) 菜单授权(sysadmin/admin/sqe/operator 可见全部 5 个子菜单)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code IN (
    'qms-mgmt','qms-mgmt.goal','qms-mgmt.audit','qms-mgmt.adverse',
    'qms-mgmt.feedback','qms-mgmt.dashboard'
  )
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 5) 按钮授权(sysadmin/admin 全量操作; sqe/operator 仅查看类按钮)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN (
    'qms-mgmt.goal.list','qms-mgmt.goal.create','qms-mgmt.goal.edit','qms-mgmt.goal.delete',
    'qms-mgmt.audit.list','qms-mgmt.audit.create','qms-mgmt.audit.edit','qms-mgmt.audit.delete','qms-mgmt.audit.nc',
    'qms-mgmt.adverse.list','qms-mgmt.adverse.create','qms-mgmt.adverse.edit','qms-mgmt.adverse.delete',
    'qms-mgmt.feedback.list','qms-mgmt.dashboard.list'
  )
ON CONFLICT (role_id, button_id) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN (
    'qms-mgmt.goal.list','qms-mgmt.audit.list','qms-mgmt.adverse.list',
    'qms-mgmt.feedback.list','qms-mgmt.dashboard.list'
  )
ON CONFLICT (role_id, button_id) DO NOTHING;
