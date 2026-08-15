-- ============================================================
-- V203 售后管理 · 工单流程控制(cs.workorder.*) 菜单 + 按钮权限码种子(跨端协同强约束 C1)。
-- 菜单: cs(售后管理 目录) -> cs.workorder.list(工单管理 菜单)。
-- 按钮码: list/create/edit/delete/assign/close 挂 cs.workorder.list 下。
-- 全部幂等, 可重复执行。sysadmin/admin 全量授权; sqe/operator 仅可见(cs 售后由客服/售后角色负责,
-- 此处先授予 sysadmin/admin 完整操作, sqe/operator 仅查看, 后续可在后台配置扩展)。
-- ============================================================

-- 1) CS 根目录(在 tlm=10 之后, sort=11)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'cs', '售后管理', '目录', '/cs', NULL, '🎧', 11, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='cs');

-- 2) CS 子菜单(工单管理)
DO $$
DECLARE cs_pid uuid;
BEGIN
  SELECT id INTO cs_pid FROM ops.sys_menu WHERE menu_code='cs';
  IF cs_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), cs_pid, 'cs.workorder.list', '工单管理', '菜单', '/cs/work-orders', 'cs/WorkOrder', '🛠', 1, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 3) 按钮码注册(挂 cs.workorder.list 菜单)
DO $$
BEGIN
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('cs.workorder.list',   '工单查看'),
    ('cs.workorder.create', '工单新建'),
    ('cs.workorder.edit',   '工单编辑'),
    ('cs.workorder.delete', '工单删除'),
    ('cs.workorder.assign', '工单派单'),
    ('cs.workorder.close',  '工单评价闭环')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'cs.workorder.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;
END $$;

-- 4) 菜单授权给角色(sysadmin/admin 全量可见; sqe/operator 可见工单)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code IN ('cs','cs.workorder.list')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 5) 按钮授权(sysadmin/admin 完整操作; sqe/operator 仅查看)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN (
    'cs.workorder.list','cs.workorder.create','cs.workorder.edit','cs.workorder.delete',
    'cs.workorder.assign','cs.workorder.close'
  )
ON CONFLICT (role_id, button_id) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN ('cs.workorder.list')
ON CONFLICT (role_id, button_id) DO NOTHING;
