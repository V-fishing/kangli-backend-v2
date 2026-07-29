-- V67: 首件检验模块下新增「来料检验」二级菜单 (幂等)
DO $$
DECLARE
  fia_id uuid;
BEGIN
  SELECT id INTO fia_id FROM ops.sys_menu WHERE menu_code = 'fia';
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), fia_id, 'fia.incoming', '来料检验', '菜单', '/fia/incoming', 'fia/IncomingList', '', 7, true
  WHERE fia_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'fia.incoming');
END $$;
