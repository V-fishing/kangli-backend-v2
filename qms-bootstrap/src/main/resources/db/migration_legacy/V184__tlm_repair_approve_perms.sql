-- ============================================================
-- V184 维修审批权限码(跨端协同强约束 C1)。
-- 审批中心「工装维修审批」回调需 tlm.repair.approve 权限。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 按钮码(审批)
INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'tlm.repair.approve', '工装维修审批'
FROM ops.sys_menu m
WHERE m.menu_code = 'tlm.repair.list'
  AND NOT EXISTS (
    SELECT 1 FROM ops.sys_button b WHERE b.menu_id = m.id AND b.btn_code = 'tlm.repair.approve'
  );

-- 授权给 sysadmin/admin(审批中心聚合, 与报废审批一致)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'tlm.repair.approve'
ON CONFLICT (role_id, button_id) DO NOTHING;
