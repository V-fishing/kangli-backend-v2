-- ============================================================
-- V182 新增「维修工单」子菜单 + 权限码(跨端协同强约束 C1)。
-- 工装管理目录(tlm)下新增 tlm.repair.list 子菜单(排序 4)。
-- 权限码: 菜单 tlm.repair.list + 按钮 tlm.repair.list(查看)。
-- 全部幂等, 可重复执行。sysadmin/admin 全量授权; sqe/operator 可见。
-- ============================================================

-- 1) 维修工单子菜单
DO $$
DECLARE tlm_pid uuid;
BEGIN
  SELECT id INTO tlm_pid FROM ops.sys_menu WHERE menu_code='tlm';
  IF tlm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT ops.gen_uuid_v7(), tlm_pid, 'tlm.repair.list', '维修工单', '菜单', 'repairs', 'tlm/Repair', '🔧', 4, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='tlm.repair.list');
  END IF;
END $$;

-- 2) 按钮码(查看)
INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'tlm.repair.list', '维修工单查看'
FROM ops.sys_menu m
WHERE m.menu_code = 'tlm.repair.list'
  AND NOT EXISTS (
    SELECT 1 FROM ops.sys_button b WHERE b.menu_id = m.id AND b.btn_code = 'tlm.repair.list'
  );

-- 3) 菜单授权给角色(sysadmin/admin/sqe/operator 可见)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code = 'tlm.repair.list'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 4) 按钮授权给 sysadmin/admin
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'tlm.repair.list'
ON CONFLICT (role_id, button_id) DO NOTHING;
