-- ============================================================
-- V242 补齐后端 @PreAuthorize 用到、但未在 ops.sys_button 注册的按钮权限码。
-- 背景: 经脚本比对, 后端 qms-api/src/main 共引用 151 个 hasAuthority 码, 其中 8 个
--       未在 sys_button 注册(权限页选不到 -> 角色拿不到该 authority -> 进对应页面后
--       接口 403 "无权限", 即使菜单已授权可见)。8 个缺失码:
--         system.audit.list    -> 挂 sqm.audit(供应商审核) 菜单
--         system.menu.list     -> 自身即菜单(菜单管理)
--         system.org.list      -> 自身即菜单(组织管理)
--         system.role.list     -> 自身即菜单(角色管理)
--         system.user.list     -> 自身即菜单(用户管理)
--         sqm.capa             -> 自身即菜单(供应商绩效)
--         tlm.abnormal.list    -> 自身即菜单(工装异常)
--         tlm.maint.list       -> 自身即菜单(工装维保)
--   注: system.*.list 这类码"既是菜单 menu_code、又是接口 authority", 此前未注册进
--       sys_button 导致权限页无法勾选。注册后页面可选, 角色勾选即可消除 403。
--       全部幂等, 可重复执行。sysadmin/admin 全量授权, 其余角色由权限页自行勾选。
-- ============================================================

-- 1) 按钮码注册(挂各自菜单; 菜单不存在则跳过, 不报错)
INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'system.audit.list', '供应商审核列表'
FROM ops.sys_menu m WHERE m.menu_code = 'sqm.audit'
ON CONFLICT (menu_id, btn_code) DO NOTHING;

INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, m.menu_code, m.menu_name
FROM ops.sys_menu m
WHERE m.menu_code IN ('system.menu.list','system.org.list','system.role.list','system.user.list','sqm.capa','tlm.abnormal.list','tlm.maint.list')
ON CONFLICT (menu_id, btn_code) DO NOTHING;

-- 2) 角色授权(sysadmin/admin 全量; 其余角色由用户在权限页自行勾选)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('system.audit.list','system.menu.list','system.org.list','system.role.list','system.user.list','sqm.capa','tlm.abnormal.list','tlm.maint.list')
ON CONFLICT (role_id, button_id) DO NOTHING;
