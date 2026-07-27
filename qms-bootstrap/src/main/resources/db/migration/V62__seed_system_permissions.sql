-- V62 系统管理：菜单 / 按钮 / 角色授权修复种子
--
-- 背景（导致"系统/模块/组织"功能此前不生效的真正原因）：
--   1) 库里已存在一套 system.org / system.menu / system.role / system.user 菜单（无 .list 后缀），
--      其 menu_code 与后端 OrgController/MenuController/RoleController/UserController 要求的
--      system.org.list / system.menu.list / system.role.list / system.user.list 等 authority 不匹配，
--      调用即被 @PreAuthorize 拦成 403；
--   2) 这些旧菜单的 path（如 /system/user 单数）与 component（system/user/index）与前端路由
--      /system/users -> UserList.vue 等对不上，点了也进不去；
--   3) PermissionLoader 从「角色关联的菜单 menu_code + 按钮 btn_code」生成 authority，旧种子虽把
--      这些菜单赋给了 sysadmin/operator，但码不对，依然 403。
--
-- 本迁移：先清理失效的旧菜单（及其按钮/授权），再建正确的 system.*.list 菜单 + 写操作按钮，
-- 并把菜单与按钮授权给 admin 类角色（sysadmin/admin/rd/qmanager 全量，operator 仅用户管理），
-- 使「组织/菜单/角色/用户」管理真正可用。全部幂等，可重复执行。

DO $$
BEGIN
  -- ===== 1) 清理失效的旧系统菜单（避免重复入口 + 路由/权限错乱）=====
  DELETE FROM ops.sys_role_button rb
   WHERE rb.button_id IN (
     SELECT b.id FROM ops.sys_button b JOIN ops.sys_menu m ON m.id = b.menu_id
     WHERE m.menu_code IN ('system.org','system.menu','system.role','system.user'));
  DELETE FROM ops.sys_role_menu rm
   WHERE rm.menu_id IN (SELECT id FROM ops.sys_menu WHERE menu_code IN ('system.org','system.menu','system.role','system.user'));
  DELETE FROM ops.sys_button b
   WHERE b.menu_id IN (SELECT id FROM ops.sys_menu WHERE menu_code IN ('system.org','system.menu','system.role','system.user'));
  DELETE FROM ops.sys_menu WHERE menu_code IN ('system.org','system.menu','system.role','system.user');

  -- ===== 2) 正确的系统子菜单（menu_code = 后端 list 级 authority，path/component 对齐前端路由）=====
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), p.id, m.code, m.name, '菜单', m.path, m.comp, '⚙️', m.sort, true
  FROM (VALUES
    ('system.org.list',  '组织管理', '/system/orgs',  'system/OrgView',  1),
    ('system.menu.list', '菜单管理', '/system/menus', 'system/MenuList', 2),
    ('system.role.list', '角色管理', '/system/roles', 'system/RoleList', 3),
    ('system.user.list', '用户管理', '/system/users', 'system/UserList', 4)
  ) AS m(code, name, path, comp, sort)
  CROSS JOIN (SELECT id FROM ops.sys_menu WHERE menu_code = 'system') AS p
  ON CONFLICT (menu_code) DO NOTHING;

  -- ===== 3) 写操作按钮（btn_code = 后端各写操作 authority）=====
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), mm.id, b.code, b.name
  FROM (VALUES
    ('system.org.list',  'system.org.create',  '新增组织'),
    ('system.org.list',  'system.org.delete',  '删除组织'),
    ('system.menu.list', 'system.menu.create', '新增菜单'),
    ('system.menu.list', 'system.menu.delete', '删除菜单'),
    ('system.role.list', 'system.role.create', '新增角色'),
    ('system.role.list', 'system.role.delete', '删除角色'),
    ('system.role.list', 'system.role.assign', '分配权限'),
    ('system.user.list', 'system.user.create', '新增用户'),
    ('system.user.list', 'system.user.delete', '删除用户'),
    ('system.user.list', 'system.role.assign', '分配角色')
  ) AS b(menu_code, code, name)
  JOIN ops.sys_menu mm ON mm.menu_code = b.menu_code
  ON CONFLICT (menu_id, btn_code) DO NOTHING;

  -- ===== 4) 菜单授权 =====
  -- 全量（org/menu/role/user）给 admin 类角色
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
  SELECT ops.gen_uuid_v7(), r.id, m.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_menu m
  WHERE r.role_code IN ('sysadmin','admin','rd','qmanager')
    AND m.menu_code IN ('system.org.list','system.menu.list','system.role.list','system.user.list')
  ON CONFLICT (role_id, menu_id) DO NOTHING;
  -- operator 仅用户管理
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
  SELECT ops.gen_uuid_v7(), r.id, m.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_menu m
  WHERE r.role_code = 'operator' AND m.menu_code = 'system.user.list'
  ON CONFLICT (role_id, menu_id) DO NOTHING;

  -- ===== 5) 按钮授权 =====
  INSERT INTO ops.sys_role_button (id, role_id, button_id)
  SELECT ops.gen_uuid_v7(), r.id, b.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_button b
  WHERE b.btn_code IN (
      'system.org.create', 'system.org.delete',
      'system.menu.create', 'system.menu.delete',
      'system.role.create', 'system.role.delete', 'system.role.assign',
      'system.user.create', 'system.user.delete')
    AND (
      r.role_code IN ('sysadmin','admin','rd','qmanager')
      OR (r.role_code = 'operator' AND b.btn_code IN ('system.user.list','system.user.create','system.user.delete'))
    )
  ON CONFLICT (role_id, button_id) DO NOTHING;
END $$;
