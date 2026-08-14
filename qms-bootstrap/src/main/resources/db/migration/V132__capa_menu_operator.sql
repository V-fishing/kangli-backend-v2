-- V132: 把 CAPA 页菜单(ncm.8d, 路径 /ncm/capas)授权给 operator + sysadmin
--
-- 背景：V131 误把不存在的 ncm.capas 菜单授权给 operator；实际 CAPA 列表页对应的菜单是 ncm.8d
-- (menu_name='CAPA', path='/ncm/capas', component='ncm/CapaList')。
-- 本迁移幂等补授权，确保 operator 可在"不良管理"下看到并进入 CAPA 页。

DO $$
BEGIN
  -- 菜单授权：ncm.8d(CAPA 页) 给 operator + sysadmin
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
  SELECT ops.gen_uuid_v7(), r.id, m.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_menu m
  WHERE r.role_code IN ('operator', 'sysadmin')
    AND m.menu_code = 'ncm.8d'
  ON CONFLICT (role_id, menu_id) DO NOTHING;
END $$;
