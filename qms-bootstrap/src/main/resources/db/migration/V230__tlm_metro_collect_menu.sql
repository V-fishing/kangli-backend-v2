-- ============================================================
-- V230 新增「计量数据采集」子菜单 + 权限码(跨端协同强约束 C1)。
-- 工装管理目录(tlm)下新增 tlm.metro.collect 子菜单(排序 6), 路径 /tlm/metro/collect。
-- 权限码: 菜单 tlm.metro.collect + 按钮 tlm.metro.collect(采集录入)。
-- 后端 GET  /v1/tlm/metro-record/page  以 hasAuthority('tlm.metro.list') 校验(可见即可查);
--       POST /v1/tlm/metro-record       以 hasAnyAuthority('tlm.metro.calib','tlm.metro.list') 校验(可录入)。
-- 角色授权沿用计量管理可见范围(sysadmin/admin/sqe/operator)。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 计量数据采集子菜单(挂 tlm 目录, 与计量管理并列)
DO $$
DECLARE tlm_pid uuid;
BEGIN
  SELECT id INTO tlm_pid FROM ops.sys_menu WHERE menu_code='tlm';
  IF tlm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT ops.gen_uuid_v7(), tlm_pid, 'tlm.metro.collect', '计量数据采集', '菜单', '/tlm/metro/collect', 'tlm/MetroCollect', '📏', 6, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='tlm.metro.collect');
  END IF;
END $$;

-- 2) 按钮码(采集录入)
INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'tlm.metro.collect', '计量数据采集录入'
FROM ops.sys_menu m
WHERE m.menu_code = 'tlm.metro.collect'
  AND NOT EXISTS (
    SELECT 1 FROM ops.sys_button b WHERE b.menu_id = m.id AND b.btn_code = 'tlm.metro.collect'
  );

-- 3) 菜单授权给角色(sysadmin/admin/sqe/operator 可见)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code = 'tlm.metro.collect'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 4) 按钮授权给 sysadmin/admin/sqe(计量管理员可录入)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin','sqe')
  AND b.btn_code = 'tlm.metro.collect'
ON CONFLICT (role_id, button_id) DO NOTHING;
