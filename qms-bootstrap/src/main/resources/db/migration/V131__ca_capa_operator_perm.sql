-- V131: 纠正措施(CA)权限码补注册 + CA/CAPA 菜单授权给 operator
--
-- 背景（MZ 操作工给了 CA 权限却进不去"纠正措施"页的根因）：
--   1) 后端 NcmCorrectiveActionController 的 list/page/get 查询接口用 @PreAuthorize('ncm.record.create')，
--      但全库没有 ncm.ca.list 按钮码（孤儿码，角色界面勾不了），任何只拿 CA 菜单的角色必 403；
--   2) 菜单 ncm.corrective-actions(V68 已建)、ncm.capas(CAPA) 只授权给 sysadmin，operator 无菜单授权，
--      路由守卫按 /me 菜单树判定可见，未授权 → 重定向工作台；
--   3) V130 已把 ncm.capa.list 重归属到 ncm.corrective-actions 菜单，因此勾选该菜单会派生 ncm.capa.list
--      （CAPA 查询可用），但 ncm.ca.list 缺失导致 CA 查询仍 403。
--
-- 本迁移：
--   ① 注册 ncm.ca.list(查询)/ncm.ca.create(管理)/ncm.ca.close(关闭) 三个按钮码，挂 ncm.corrective-actions 菜单；
--   ② 把 ncm.corrective-actions 菜单授权给 operator（派生 ncm.ca.list + ncm.capa.list）与 sysadmin（全）；
--   ③ 把 ncm.capas(CAPA) 菜单授权给 operator + sysadmin，确保 CAPA 页可导航；
--   ④ 把 ncm.ca.list/create/close 写按钮授权 sysadmin（保持全量）。
-- 全部幂等，可重复执行。

DO $$
BEGIN
  -- ===== 1) 注册 CA 查询/写按钮码，挂 ncm.corrective-actions 菜单 =====
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, b.code, b.name
  FROM (VALUES
    ('ncm.ca.list',  '纠正措施查询'),
    ('ncm.ca.create', '纠正措施管理'),
    ('ncm.ca.close', '纠正措施关闭')
  ) AS b(code, name)
  JOIN ops.sys_menu m ON m.menu_code = 'ncm.corrective-actions'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;

  -- ===== 2) 菜单授权：ncm.corrective-actions 给 operator + sysadmin =====
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
  SELECT ops.gen_uuid_v7(), r.id, m.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_menu m
  WHERE r.role_code IN ('operator', 'sysadmin')
    AND m.menu_code = 'ncm.corrective-actions'
  ON CONFLICT (role_id, menu_id) DO NOTHING;

  -- ===== 3) 菜单授权：ncm.capas(CAPA) 给 operator + sysadmin =====
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
  SELECT ops.gen_uuid_v7(), r.id, m.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_menu m
  WHERE r.role_code IN ('operator', 'sysadmin')
    AND m.menu_code = 'ncm.capas'
  ON CONFLICT (role_id, menu_id) DO NOTHING;

  -- ===== 4) 按钮授权：ncm.ca.list/create/close 给 sysadmin（全量）=====
  INSERT INTO ops.sys_role_button (id, role_id, button_id)
  SELECT ops.gen_uuid_v7(), r.id, b.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_button b
  WHERE r.role_code = 'sysadmin'
    AND b.btn_code IN ('ncm.ca.list', 'ncm.ca.create', 'ncm.ca.close')
  ON CONFLICT (role_id, button_id) DO NOTHING;
END $$;
