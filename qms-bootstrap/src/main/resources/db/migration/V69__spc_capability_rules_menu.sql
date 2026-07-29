-- V69: SPC 模块补齐「能力分析」「判异规则」二级菜单 (幂等)
DO $$
DECLARE
  spc_id uuid;
BEGIN
  SELECT id INTO spc_id FROM ops.sys_menu WHERE menu_code = 'spc';

  -- 激活 spc.capability 并指向新页面
  UPDATE ops.sys_menu
     SET parent_id  = spc_id,
         menu_name  = '能力分析',
         menu_type  = '菜单',
         path       = '/spc/capability',
         component  = 'spc/Capability',
         icon       = '',
         sort_order = 4,
         visible    = true
   WHERE menu_code  = 'spc.capability';

  -- 新增 spc.rules 二级菜单
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), spc_id, 'spc.rules', '判异规则', '菜单', '/spc/rules', 'spc/RuleDetection', '', 5, true
  WHERE spc_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'spc.rules');
END $$;
