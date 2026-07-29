-- V74: FIA 新增"标准检验项明细维护"菜单(独立维护页 /fia/std-items)
-- 挂在 fia 根目录(code=fia)下,组件 fia/StdItemMaintain。幂等可重复执行。

DO $$
DECLARE
  fia_id uuid;
BEGIN
  SELECT id INTO fia_id FROM ops.sys_menu WHERE menu_code = 'fia';
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), fia_id, 'fia.std-items', '检验项明细', '菜单', '/fia/std-items', 'fia/StdItemMaintain', '', 6, true
  WHERE fia_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'fia.std-items');
END $$;
