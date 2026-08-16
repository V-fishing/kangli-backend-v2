-- ============================================================
-- V200 补齐 TLM 列表类权限码(跨端协同强约束 C1 收尾)。
-- 问题: V170 未将 tlm.tooling.list、V187 未将 tlm.metro.list
-- 注册为 sys_button 按钮码, 而对应 Controller 端点
--   /v1/tlm/tooling/page        -> hasAuthority('tlm.tooling.list')
--   /v1/tlm/tooling/metro/dashboard -> hasAuthority('tlm.metro.list')
--   /v1/tlm/tooling/{id}/binds   -> hasAuthority('tlm.metro.list')
--   /v1/tlm/calib-plans/page     -> hasAuthority('tlm.metro.list')
-- 以按钮码形式参与 Spring Security 鉴权, 缺失会导致列表/看板/校准计划被 401 拦截。
-- 全部幂等, 可重复执行; sysadmin/admin/sqe/operator 授权(与 V187 可见范围一致)。
-- ============================================================

-- 1) 按钮码补登记(挂各自列表菜单下)
DO $$
BEGIN
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, 'tlm.tooling.list', '工装台账查看'
  FROM ops.sys_menu m
  WHERE m.menu_code = 'tlm.tooling.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;

  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, 'tlm.metro.list', '计量管理查看'
  FROM ops.sys_menu m
  WHERE m.menu_code = 'tlm.metro.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;
END $$;

-- 2) 按钮授权给 sysadmin/admin/sqe/operator
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND b.btn_code IN ('tlm.tooling.list','tlm.metro.list')
ON CONFLICT (role_id, button_id) DO NOTHING;
