-- V105: 系统管理 - 通知配置 菜单 + 权限
-- 首行必须注释,避免 UTF-8 BOM 破坏后续语句(同项目其他种子文件约定)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), (SELECT id FROM ops.sys_menu WHERE menu_code='system'),
       'system.notify.config', '通知配置', '菜单', '/system/notify-config', 'views/system/NotifyConfig.vue', '🔔', 95, true
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='system.notify.config');

INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), rr.id, m.id
FROM ops.sys_role rr, ops.sys_menu m
WHERE m.menu_code='system.notify.config'
  AND rr.role_code IN ('sysadmin', 'admin', 'rd', 'qmanager')
  AND NOT EXISTS (SELECT 1 FROM ops.sys_role_menu x WHERE x.role_id=rr.id AND x.menu_id=m.id);
