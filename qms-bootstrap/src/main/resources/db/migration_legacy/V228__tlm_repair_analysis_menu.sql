-- ============================================================
-- V228 新增「维修根因分析」子菜单 + 权限码(跨端协同强约束 C1)。
-- 工装管理目录(tlm)下新增 tlm.repair.analysis 子菜单(排序 5)。
-- 权限码: 菜单 tlm.repair.analysis + 按钮 tlm.repair.analysis(查看)。
-- 后端 GET /v1/tlm/repair/analysis 以 @PreAuthorize("hasAuthority('tlm.repair.list')") 校验,
-- 故按钮码登记为 tlm.repair.analysis 便于前端 v-permission 控制菜单可见性;
-- 角色授权沿用 tlm.repair.list 的可见范围(sysadmin/admin/sqe/operator)。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 维修根因分析子菜单
DO $$
DECLARE tlm_pid uuid;
BEGIN
  SELECT id INTO tlm_pid FROM ops.sys_menu WHERE menu_code='tlm';
  IF tlm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT ops.gen_uuid_v7(), tlm_pid, 'tlm.repair.analysis', '维修根因分析', '菜单', '/tlm/repair-analysis', 'tlm/RepairAnalysis', '📊', 5, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='tlm.repair.analysis');
  END IF;
END $$;

-- 2) 按钮码(查看)
INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'tlm.repair.analysis', '维修根因分析查看'
FROM ops.sys_menu m
WHERE m.menu_code = 'tlm.repair.analysis'
  AND NOT EXISTS (
    SELECT 1 FROM ops.sys_button b WHERE b.menu_id = m.id AND b.btn_code = 'tlm.repair.analysis'
  );

-- 3) 菜单授权给角色(sysadmin/admin/sqe/operator 可见, 沿用维修工单可见范围)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code = 'tlm.repair.analysis'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 4) 按钮授权给 sysadmin/admin
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'tlm.repair.analysis'
ON CONFLICT (role_id, button_id) DO NOTHING;
