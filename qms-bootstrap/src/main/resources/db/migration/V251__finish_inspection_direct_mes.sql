-- ============================================================
-- V251 完工检验独立模块权限码(直读直写 MES qms.finished_goods_inspection)
-- 1) 注册 fia.finish.list / create / edit / delete 按钮(挂 fia 菜单)
-- 2) 废弃 fia.finish.release(旧完工检验放行逻辑已删除)
-- 3) 授权 sysadmin / admin
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 注册按钮权限码(挂 fia 首件检验菜单)
DO $$
DECLARE fia_mid uuid;
BEGIN
  SELECT id INTO fia_mid FROM ops.sys_menu WHERE menu_code = 'fia';
  IF fia_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.list',   '完工检验查询'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.create', '完工检验建单'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.edit',   '完工检验录入/签核'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.delete', '完工检验删除')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 废弃旧放行码(旧 fia_task trigger_type='完工检验' 逻辑已删除)
DELETE FROM ops.sys_role_button
WHERE button_id IN (SELECT id FROM ops.sys_button WHERE btn_code = 'fia.finish.release');
DELETE FROM ops.sys_button WHERE btn_code = 'fia.finish.release';

-- 3) 角色授权(sysadmin / admin 全量)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('fia.finish.list','fia.finish.create','fia.finish.edit','fia.finish.delete')
ON CONFLICT (role_id, button_id) DO NOTHING;
