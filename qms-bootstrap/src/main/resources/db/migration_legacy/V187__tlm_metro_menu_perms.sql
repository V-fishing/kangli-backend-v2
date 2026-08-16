-- ============================================================
-- V187 TLM 计量管理子模块：清理废弃 msm 字典 + 菜单/权限码(C1) + 通知事件码(C2)。
-- 1) 清理 V07 预留但从未被任何代码使用的 msm_gauge_status / msm_calib_plan_status 孤儿字典。
-- 2) tlm 目录下新增 tlm.metro.list 计量管理子菜单(排序 5) + 按钮权限码。
-- 3) ops.notify_config 新增 tlm_calib_plan_created 事件码(校准计划生成推送计量管理员)。
-- 全部幂等, 可重复执行。sysadmin/admin 全量授权; sqe/operator 可见。
-- ============================================================

-- 1) 清理废弃 msm 孤儿字典(无 Java 实体/Service/Controller 引用, 计量状态改用 calib_due_date 派生)
DELETE FROM ops.sys_dict WHERE dict_type IN ('msm_gauge_status', 'msm_calib_plan_status');

-- 2) 计量管理子菜单
DO $$
DECLARE tlm_pid uuid;
BEGIN
  SELECT id INTO tlm_pid FROM ops.sys_menu WHERE menu_code='tlm';
  IF tlm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT ops.gen_uuid_v7(), tlm_pid, 'tlm.metro.list', '计量管理', '菜单', 'metro', 'tlm/Metro', '📐', 5, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='tlm.metro.list');
  END IF;
END $$;

-- 3) 按钮码(挂 tlm.metro.list 菜单)
DO $$
BEGIN
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('tlm.metro.view',   '计量查看'),
    ('tlm.metro.calib',  '校准录入'),
    ('tlm.metro.repair', '计量送修'),
    ('tlm.metro.scrap',  '计量报废')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'tlm.metro.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;
END $$;

-- 4) 菜单授权给角色(sysadmin/admin/sqe/operator 可见)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code = 'tlm.metro.list'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 5) 按钮授权给 sysadmin/admin
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('tlm.metro.view','tlm.metro.calib','tlm.metro.repair','tlm.metro.scrap')
ON CONFLICT (role_id, button_id) DO NOTHING;

-- 6) 通知配置: 校准计划生成推送计量管理员(强约束 C2)
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT 'tlm', 'tlm_calib_plan_created', '计量器具校准计划生成', 'sqe', '站内弹窗', true
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='tlm' AND event_code='tlm_calib_plan_created');
