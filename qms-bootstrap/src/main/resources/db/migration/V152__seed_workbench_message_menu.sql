-- 消息中心菜单:挂在工作台(dashboard)下,位于「个人任务中心」之后
-- 幂等:先确保菜单行存在,再授权全部角色(菜单可见由后端 /me 驱动,无需额外按钮权限)

DO $$
DECLARE
    v_parent_id uuid;
    v_menu_id   uuid;
BEGIN
    -- 取工作台父菜单 id
    SELECT id INTO v_parent_id FROM ops.sys_menu WHERE menu_code = 'dashboard';

    -- 注册菜单(幂等)
    INSERT INTO ops.sys_menu (id, menu_code, menu_name, menu_type, path, component, parent_id, sort_order, icon, visible, created_at, updated_at)
    SELECT ops.gen_uuid_v7(), 'workbench.messages', '消息中心', '菜单', '/workbench/messages', 'workbench/MessageCenter',
           v_parent_id, 2, '📮', true, now(), now()
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'workbench.messages');

    SELECT id INTO v_menu_id FROM ops.sys_menu WHERE menu_code = 'workbench.messages';

    -- 授权给全部角色(所有分公司 + sysadmin 等),对已存在的授权幂等跳过
    INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
    SELECT ops.gen_uuid_v7(), r.id, v_menu_id
    FROM ops.sys_role r
    WHERE NOT EXISTS (
        SELECT 1 FROM ops.sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = v_menu_id
    );
END $$;

-- 通知分页查询索引:避免 1.7 万行按用户全表扫描(user_id + 时间倒序)
CREATE INDEX IF NOT EXISTS idx_sys_notification_user_created
    ON ops.sys_notification (user_id, created_at DESC);
