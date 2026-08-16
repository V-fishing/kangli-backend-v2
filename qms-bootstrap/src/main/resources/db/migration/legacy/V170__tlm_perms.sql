-- V170: TLM 工装管理 菜单 + 按钮权限码种子(跨端协同强约束 C1)。
-- 菜单 5 个 + 按钮 16 个 = 21 个权限码(详见前端规范 §2.5.1)。
-- 全部幂等, 可重复执行。sysadmin 全量授权。

-- 1) TLM 根目录(在 patrol=9 之后, sort=10)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), NULL, 'tlm', '工装管理', '目录', '/tlm', NULL, '🔧', 10, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='tlm');

-- 2) TLM 子菜单(台账/维保/异常/详情)
DO $$
DECLARE tlm_pid uuid;
BEGIN
  SELECT id INTO tlm_pid FROM ops.sys_menu WHERE menu_code='tlm';
  IF tlm_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), tlm_pid, 'tlm.tooling.list', '工装台账', '菜单', 'tooling', 'tlm/Tooling', '🔧', 1, true),
      (ops.gen_uuid_v7(), tlm_pid, 'tlm.maint.list',   '工装维保', '菜单', 'maint',   'tlm/Maint',   '🛠', 2, true),
      (ops.gen_uuid_v7(), tlm_pid, 'tlm.abnormal.list','工装异常', '菜单', 'abnormals','tlm/Abnormal','⚠️', 3, true)
    ON CONFLICT DO NOTHING;
  END IF;
END $$;

-- 3) 按钮码注册(挂 tlm.tooling.list 菜单)
DO $$
BEGIN
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('tlm.tooling.create',   '工装新增'),
    ('tlm.tooling.edit',     '工装编辑'),
    ('tlm.tooling.delete',   '工装删除'),
    ('tlm.tooling.scrap',    '工装报废'),
    ('tlm.tooling.repair',   '工装送修'),
    ('tlm.tooling.lock',     '工装锁定'),
    ('tlm.tooling.bind',     '工装绑定'),
    ('tlm.tooling.export',   '台账导出')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'tlm.tooling.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;

  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('tlm.maint.plan.create',   '保养计划新建'),
    ('tlm.maint.plan.edit',     '保养计划编辑'),
    ('tlm.maint.plan.delete',   '保养计划删除'),
    ('tlm.maint.record.create', '保养记录登记'),
    ('tlm.maint.record.delete', '保养记录删除')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'tlm.maint.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;

  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('tlm.repair.create',   '维修工单新建'),
    ('tlm.repair.complete', '维修完成'),
    ('tlm.scrap.approve',   '报废审批')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'tlm.tooling.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;
END $$;

-- 4) 菜单授权给角色(sysadmin 全量 + sqe/operator 可见台账与维保)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code IN ('tlm','tlm.tooling.list','tlm.maint.list','tlm.abnormal.list')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 5) 按钮授权给 sysadmin(全量)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN (
    'tlm.tooling.create','tlm.tooling.edit','tlm.tooling.delete','tlm.tooling.scrap',
    'tlm.tooling.repair','tlm.tooling.lock','tlm.tooling.bind','tlm.tooling.export',
    'tlm.maint.plan.create','tlm.maint.plan.edit','tlm.maint.plan.delete',
    'tlm.maint.record.create','tlm.maint.record.delete',
    'tlm.repair.create','tlm.repair.complete','tlm.scrap.approve'
  )
ON CONFLICT (role_id, button_id) DO NOTHING;
