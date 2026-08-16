-- V84: SPC 模块新增「标准线管理」二级菜单 (幂等)
DO $$
DECLARE
  spc_id uuid;
BEGIN
  SELECT id INTO spc_id FROM ops.sys_menu WHERE menu_code = 'spc';

  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), spc_id, 'spc.specstandard', '标准线管理', '菜单', '/spc/spec-standards', 'spc/SpecStandard', '', 6, true
  WHERE spc_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'spc.specstandard');
END $$;
