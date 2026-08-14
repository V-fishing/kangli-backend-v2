-- 个人任务中心菜单:挂在工作台(dashboard)下,授权 operator + sysadmin
-- 幂等:先确保菜单行存在,再授权角色(菜单可见由后端 /me 驱动,无需额外按钮权限)

DO $$
DECLARE
    v_parent_id uuid;
    v_menu_id   uuid;
BEGIN
    -- 取工作台父菜单 id
    SELECT id INTO v_parent_id FROM ops.sys_menu WHERE menu_code = 'dashboard';

    -- 注册菜单(幂等)
    INSERT INTO ops.sys_menu (id, menu_code, menu_name, menu_type, path, component, parent_id, sort_order, icon, visible, created_at, updated_at)
    SELECT ops.gen_uuid_v7(), 'workbench.tasks', '个人任务中心', '菜单', '/workbench/tasks', 'workbench/TaskCenter',
           v_parent_id, 1, '🗂️', true, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'workbench.tasks');

    SELECT id INTO v_menu_id FROM ops.sys_menu WHERE menu_code = 'workbench.tasks';

    -- 授权给 operator(所有分公司)与 sysadmin
    INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
    SELECT ops.gen_uuid_v7(), r.id, v_menu_id
    FROM ops.sys_role r
    WHERE r.role_code IN ('operator', 'sysadmin')
      AND NOT EXISTS (
        SELECT 1 FROM ops.sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = v_menu_id
      );
END $$;
