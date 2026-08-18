-- ============================================================
-- V237 审批中心 / 我的任务 权限码种子(跨端协同强约束 C1 补齐)。
-- 背景: ApprovalCenterController.pending 与 MyTaskController.list 此前无 @PreAuthorize,
--       依赖 JWT 过滤器兜底(铁律第 9 条 C1 破例)。现补齐显式权限码并授权,消除破例。
-- 按钮码(挂工作台 dashboard 菜单下,工作台对所有一线角色开放):
--   approval.center.pending  待我审批聚合(跨 FIA/NCM/SQM)
--   my.task.list             我的任务聚合(跨模块个人任务中心)
-- 授权: sysadmin/admin 全量; sqe/operator 仅查看(与工作台其他聚合口径一致)。
-- 全部幂等,可重复执行。
-- ============================================================

-- 1) 按钮码注册(挂工作台 dashboard 菜单;dashboard 父菜单由 V152 种子)
DO $$
DECLARE dash_mid uuid;
BEGIN
  SELECT id INTO dash_mid FROM ops.sys_menu WHERE menu_code = 'dashboard';
  IF dash_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), dash_mid, 'approval.center.pending', '待我审批'),
      (ops.gen_uuid_v7(), dash_mid, 'my.task.list',            '我的任务')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 按钮授权(sysadmin/admin 全量; sqe/operator 仅查看)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('approval.center.pending','my.task.list')
ON CONFLICT (role_id, button_id) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN ('approval.center.pending','my.task.list')
ON CONFLICT (role_id, button_id) DO NOTHING;
