-- ============================================================
-- V253 完工检验菜单登记(挂 FIA 首件检验目录二级菜单)+ 物料检验并入
-- 1) 登记 fia.finish 菜单(挂 fia 目录下, path=/fia/finish-inspection, component=fia/FinishInspectionList)
-- 2) 授权 sysadmin / admin(菜单可见性)
-- 3) 物料检验(fia.material.*)并入完工检验列表页"物料"分段, 不单独登记菜单
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 登记完工检验菜单(挂 fia 目录)
DO $$
DECLARE fia_mid uuid;
DECLARE finish_mid uuid;
BEGIN
  SELECT id INTO fia_mid FROM ops.sys_menu WHERE menu_code = 'fia';
  IF fia_mid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
    SELECT fia_mid, 'fia.finish', '完工检验', '菜单', '/fia/finish-inspection', 'fia/FinishInspectionList', '', 8, true
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'fia.finish');

    -- 2) 角色授权(sysadmin / admin 全量)
    SELECT id INTO finish_mid FROM ops.sys_menu WHERE menu_code = 'fia.finish';
    IF finish_mid IS NOT NULL THEN
      INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
      SELECT ops.gen_uuid_v7(), r.id, finish_mid
      FROM ops.sys_role r
      WHERE r.role_code IN ('sysadmin','admin')
      ON CONFLICT (role_id, menu_id) DO NOTHING;
    END IF;
  END IF;
END $$;
