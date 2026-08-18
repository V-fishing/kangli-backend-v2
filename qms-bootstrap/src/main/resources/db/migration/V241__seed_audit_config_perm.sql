-- ============================================================
-- V241 审核配置页(system.audit-config)独立按钮权限码注册 + 角色授权。
-- 背景: 审核配置页(前端 AuditApprovalConfig.vue)后端两块接口此前直接复用业务码:
--       供应商会签 GET/PUT /audit-approval-cfg  -> sqm.audit.list / sqm.audit.approve
--       8D 阶段签批 GET/POST  /approval-config   -> ncm.8d.list    / ncm.8d.create
--   这 4 个码分散挂在「供应商审核/供应商/不良管理/CAPA」菜单下,
--   导致: ① 角色权限页"系统管理"分组下没有"审核配置"按钮可选(用户反馈);
--        ② 仅勾了 system.audit-config 菜单、未勾这些业务码的角色进入该页即 403 报无权限。
-- 修复: 注册独立按钮码 system.audit-config(挂 system.audit-config 菜单),
--       前端页面保存按钮用 perm.has('system.audit-config') 控制,
--       后端两 Controller 的 4 个端点 @PreAuthorize 改为该码(向后兼容:
--       旧业务码仍保留在 sys_button, 不影响其他页面)。sysadmin/admin 全量授权。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 按钮码注册(挂 system.audit-config 菜单)
DO $$
DECLARE cfg_mid uuid;
BEGIN
  SELECT id INTO cfg_mid FROM ops.sys_menu WHERE menu_code = 'system.audit-config';
  IF cfg_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), cfg_mid, 'system.audit-config', '审核配置')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 角色授权(sysadmin/admin 全量;其余角色由用户在权限页自行勾选)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'system.audit-config'
ON CONFLICT (role_id, button_id) DO NOTHING;
