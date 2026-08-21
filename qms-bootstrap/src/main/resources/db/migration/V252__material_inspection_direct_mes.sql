-- ============================================================
-- V252 物料检验(来料检验)独立模块权限码(直读直写 MES qms.material_inspection)
-- 1) 注册 fia.material.list / create / edit / delete 按钮(挂 fia 菜单)
-- 2) 授权 sysadmin / admin
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 注册按钮权限码(挂 fia 首件检验菜单)
DO $$
DECLARE fia_mid uuid;
BEGIN
  SELECT id INTO fia_mid FROM ops.sys_menu WHERE menu_code = 'fia';
  IF fia_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), fia_mid, 'fia.material.list',   '物料检验查询'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.material.create', '物料检验建单'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.material.edit',   '物料检验录入/签核'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.material.delete', '物料检验删除')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 角色授权(sysadmin / admin 全量)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('fia.material.list','fia.material.create','fia.material.edit','fia.material.delete')
ON CONFLICT (role_id, button_id) DO NOTHING;
