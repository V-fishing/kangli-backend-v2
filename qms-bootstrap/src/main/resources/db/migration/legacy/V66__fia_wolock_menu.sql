-- ============================================================
-- V66: FIA 模块新增「工单锁定」二级菜单(幂等,可重复执行)
-- ------------------------------------------------------------
-- 在 首件检验(fia) 模块下挂一个「工单锁定」子页签,对应真实路由
-- /fia/wo-lock(WoLockList)。前端 navigation/subnav 按菜单树渲染,
-- 角色授权沿用根模块码 fia,无需额外改 sys_role_menu。
--
-- 列顺序: id, parent_id, menu_code, menu_name, menu_type,
--          path, component, icon, sort_order, visible
-- ============================================================

DO $$
DECLARE
  fia_id uuid;
BEGIN
  SELECT id INTO fia_id FROM ops.sys_menu WHERE menu_code = 'fia';

  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), fia_id, 'fia.wolock', '工单锁定', '菜单', '/fia/wo-lock', 'fia/WoLockList', '', 6, true
  WHERE fia_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'fia.wolock');
END $$;
