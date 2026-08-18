-- ============================================================
-- V240 通知中心/通知配置 按钮权限码注册 + 角色授权(跨端协同强约束 C1 补齐)。
-- 背景: 后端 NotifyMessageController 4 个端点 + NotifyConfigController 6 个端点
--       全部 @PreAuthorize("hasAuthority('system.notify.center'/'system.notify.config')"),
--       但这两个按钮码此前从未 seed 进 ops.sys_button(按钮权限码注册表)。
--       后果: 角色权限配置页按 sys_button 分组展示可勾选码, 该码根本选不到 ->
--       角色永远拿不到这两个码 -> 进入通知中心/通知配置页时接口 403 ->
--       前端弹"没有权限执行该操作 (403)"。即"给了菜单权限仍报无权限"。
-- 修复: 注册两个按钮码(挂各自菜单下) + sysadmin/admin 全量授权, 使页面可选可配。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 按钮码注册(挂通知中心 / 通知配置 菜单)
DO $$
DECLARE center_mid uuid; cfg_mid uuid;
BEGIN
  SELECT id INTO center_mid FROM ops.sys_menu WHERE menu_code = 'system.notify.center';
  SELECT id INTO cfg_mid    FROM ops.sys_menu WHERE menu_code = 'system.notify.config';
  IF center_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), center_mid, 'system.notify.center', '通知中心')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
  IF cfg_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), cfg_mid, 'system.notify.config', '通知配置')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 角色授权(sysadmin/admin 全量;其余角色由用户在权限页自行勾选)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('system.notify.center','system.notify.config')
ON CONFLICT (role_id, button_id) DO NOTHING;
