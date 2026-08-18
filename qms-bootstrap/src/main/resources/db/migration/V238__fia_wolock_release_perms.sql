-- ============================================================
-- V238 工单锁定放行权限码种子(跨端协同强约束 C1 补齐)。
-- 背景: 前端 fia/woLock.ts 调用 POST /v1/fia/wo-lock/release 与
--        /v1/fia/wo-lock/emergency-release, 但后端 FiaWoLockController 此前
--        无对应端点(前端会 404)。已补齐 Controller 端点并加 @PreAuthorize,
--        此处补齐按钮码注册 + 角色授权, 消除契约断裂。
-- 按钮码(挂工单锁定 fia.wolock 菜单下):
--   fia.wolock.release      审批放行
--   fia.wolock.emergency    紧急放行
-- 授权: sysadmin/admin 全量; sqe/operator 同 FIA 其他操作口径授权。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 按钮码注册(挂工单锁定 fia.wolock 菜单)
DO $$
DECLARE wolock_mid uuid;
BEGIN
  SELECT id INTO wolock_mid FROM ops.sys_menu WHERE menu_code = 'fia.wolock';
  IF wolock_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), wolock_mid, 'fia.wolock.release',  '审批放行'),
      (ops.gen_uuid_v7(), wolock_mid, 'fia.wolock.emergency', '紧急放行')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 按钮授权(sysadmin/admin 全量; sqe/operator 授权放行操作)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('fia.wolock.release','fia.wolock.emergency')
ON CONFLICT (role_id, button_id) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN ('fia.wolock.release','fia.wolock.emergency')
ON CONFLICT (role_id, button_id) DO NOTHING;
