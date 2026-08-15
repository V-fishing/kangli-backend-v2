-- ============================================================
-- V201 补齐 sqe / operator 角色的计量管理(tlm.metro.*)操作权限(跨端协同强约束 C1)。
-- 背景: V187 仅将 tlm.metro.view/calib/repair/scrap 授权给 sysadmin/admin,
-- 导致 SQE 登录后看不到送修/报废/校准录入等操作按钮。按业务职责 SQE 负责计量管理,
-- 须拥有完整计量操作权限。tlm.metro.list 已在 V200 授权, 此处补 view/calib/repair/scrap/lock。
-- 全部幂等, 可重复执行。
-- ============================================================

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN ('tlm.metro.view','tlm.metro.calib','tlm.metro.repair','tlm.metro.scrap','tlm.metro.lock')
ON CONFLICT (role_id, button_id) DO NOTHING;
