-- NCM 纠正措施子页签(insert between CAPA and 趋势报表)
INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
SELECT ops.gen_uuid_v7(), id, 'ncm.corrective-actions', '纠正措施', '菜单', '/ncm/corrective-actions', 'ncm/CorrectiveActionList', '', 5, true
FROM ops.sys_menu WHERE menu_code='ncm'
AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code='ncm.corrective-actions');

-- shift existing entries to keep ordering
UPDATE ops.sys_menu SET sort_order=6 WHERE menu_code='ncm.trend';
UPDATE ops.sys_menu SET sort_order=7 WHERE menu_code='ncm.8d-approval-config';
