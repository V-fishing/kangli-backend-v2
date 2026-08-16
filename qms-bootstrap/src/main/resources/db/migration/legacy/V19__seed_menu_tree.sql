-- ============================================================
-- V19: 种子完整菜单树(对齐前端mock/system.ts MENU_TREE)
-- 前端P0-1:动态路由从后端API获取菜单,替换mock菜单树。
-- 幂等:用INSERT ... ON CONFLICT DO NOTHING。
-- ============================================================

-- 1. 总览(根目录)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'overview', '总览看板', '菜单', '/overview', 'overview/Dash', '📊', 0, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='overview');

-- 2. FIA 首件检验(目录)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'fia', '首件检验', '目录', '/fia', NULL, '📋', 10, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='fia');

-- FIA 子菜单
DO $$
DECLARE fia_pid uuid;
BEGIN
  SELECT id INTO fia_pid FROM ops.sys_menu WHERE menu_code='fia';
  IF fia_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), fia_pid, 'fia.dash', '看板总览', '菜单', 'dash', 'fia/Dash', '📊', 1, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.tasks', '检验任务', '菜单', 'tasks', 'fia/Tasks', '📋', 2, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.entry', '检验录入', '菜单', 'entry', 'fia/Entry', '✏️', 3, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.approve', '审批中心', '菜单', 'approve', 'fia/Approve', '✅', 4, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.stdlib', '检验标准库', '菜单', 'stdlib', 'fia/Stdlib', '📚', 5, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.trigcfg', '触发与签名配置', '菜单', 'trigcfg', 'fia/TrigCfg', '⚙️', 6, true),
      (ops.gen_uuid_v7(), fia_pid, 'fia.trace', '追溯归档', '菜单', 'trace', 'fia/Trace', '🔗', 7, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 3. SPC 过程能力
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'spc', '过程能力', '目录', '/spc', NULL, '📈', 20, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='spc');

DO $$
DECLARE spc_pid uuid;
BEGIN
  SELECT id INTO spc_pid FROM ops.sys_menu WHERE menu_code='spc';
  IF spc_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), spc_pid, 'spc.dash', '看板总览', '菜单', 'dash', 'spc/Dash', '📊', 1, true),
      (ops.gen_uuid_v7(), spc_pid, 'spc.control', '控制图监控', '菜单', 'control', 'spc/Control', '📈', 2, true),
      (ops.gen_uuid_v7(), spc_pid, 'spc.collect', '数据采集', '菜单', 'collect', 'spc/Collect', '📥', 3, true),
      (ops.gen_uuid_v7(), spc_pid, 'spc.alarm', '告警处理', '菜单', 'alarm', 'spc/Alarm', '🚨', 4, true),
      (ops.gen_uuid_v7(), spc_pid, 'spc.capability', '能力分析', '菜单', 'capability', 'spc/Capability', '🎯', 5, true),
      (ops.gen_uuid_v7(), spc_pid, 'spc.paramcfg', '参数配置', '菜单', 'paramcfg', 'spc/ParamCfg', '⚙️', 6, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 4. NCM 不良管理
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'ncm', '不良管理', '目录', '/ncm', NULL, '⚠️', 30, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='ncm');

DO $$
DECLARE ncm_pid uuid;
BEGIN
  SELECT id INTO ncm_pid FROM ops.sys_menu WHERE menu_code='ncm';
  IF ncm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), ncm_pid, 'ncm.dash', '看板总览', '菜单', 'dash', 'ncm/Dash', '📊', 1, true),
      (ops.gen_uuid_v7(), ncm_pid, 'ncm.entry', '不良录入', '菜单', 'entry', 'ncm/Entry', '✏️', 2, true),
      (ops.gen_uuid_v7(), ncm_pid, 'ncm.analysis', '不良分析', '菜单', 'analysis', 'ncm/Analysis', '🔍', 3, true),
      (ops.gen_uuid_v7(), ncm_pid, 'ncm.trend', '趋势报表', '菜单', 'trend', 'ncm/Trend', '📈', 4, true),
      (ops.gen_uuid_v7(), ncm_pid, 'ncm.8d', '8D整改', '菜单', '8d', 'ncm/EightD', '🎯', 5, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 5. SQM 供应商质量
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'sqm', '供应商质量', '目录', '/sqm', NULL, '🏢', 40, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='sqm');

DO $$
DECLARE sqm_pid uuid;
BEGIN
  SELECT id INTO sqm_pid FROM ops.sys_menu WHERE menu_code='sqm';
  IF sqm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.dash', '总览看板', '菜单', 'dash', 'sqm/Dash', '📊', 1, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.lifecycle', '供应商全生命周期', '菜单', 'lifecycle', 'sqm/Lifecycle', '🗂️', 2, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.audit', '供应商审核', '菜单', 'audit', 'sqm/Audit', '🔍', 3, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.change', '物料变更', '菜单', 'change', 'sqm/Change', '🔄', 4, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.abnormal', '来料异常', '菜单', 'abnormal', 'sqm/Abnormal', '⚠️', 5, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.fmea', 'FMEA风险', '菜单', 'fmea', 'sqm/Fmea', '🎯', 6, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.trace', '来料追溯', '菜单', 'trace', 'sqm/Trace', '🔗', 7, true),
      (ops.gen_uuid_v7(), sqm_pid, 'sqm.capa', 'CAPA纠正预防', '菜单', 'capa', 'sqm/Capa', '🛡️', 8, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 6. 巡检(Patrol)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'patrol', '巡检管理', '目录', '/patrol', NULL, '🔦', 50, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='patrol');

-- 7. 归档(Archive)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'archive', '统一归档', '菜单', '/archive', 'archive/ArchiveList', '🗄️', 60, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='archive');

-- 8. 系统管理(System - 已有9条,补充一个目录父节点)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'system', '系统管理', '目录', '/system', NULL, '⚙️', 90, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='system');
