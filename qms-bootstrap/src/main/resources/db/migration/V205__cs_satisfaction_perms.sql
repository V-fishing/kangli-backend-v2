-- ============================================================
-- V205 售后管理 · 客户满意度(B 模块) 菜单 + 按钮权限码种子(跨端协同强约束 C1)。
-- 菜单: cs(已存在) 下新增 cs.satisfaction.list(满意度看板) + cs.feedback.list(客户反馈)。
-- 按钮码: satisfaction 仅查看(挂 cs.satisfaction.list 菜单);
--        feedback.list/create/edit/delete/handle 挂 cs.feedback.list 菜单。
-- 全部幂等, 可重复执行。sysadmin/admin 全量操作; sqe/operator 仅查看。
-- 注意: 按钮码 menu_id 必须在 INSERT 时直接指定, 避免与菜单码同名导致的 JOIN 笛卡尔冲突。
-- ============================================================

-- 1) CS 子菜单(满意度看板 + 客户反馈)
DO $$
DECLARE cs_pid uuid;
BEGIN
  SELECT id INTO cs_pid FROM ops.sys_menu WHERE menu_code='cs';
  IF cs_pid IS NOT NULL THEN
    INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible) VALUES
      (ops.gen_uuid_v7(), cs_pid, 'cs.satisfaction.list', '满意度看板', '菜单', '/cs/satisfaction', 'cs/Satisfaction', '📊', 2, true),
      (ops.gen_uuid_v7(), cs_pid, 'cs.feedback.list',    '客户反馈',   '菜单', '/cs/feedback',    'cs/Feedback',    '💬', 3, true)
    ON CONFLICT (menu_code) DO NOTHING;
  END IF;
END $$;

-- 2) 按钮码注册(每个按钮码明确归属对应菜单, 直接指定 menu_id, 避免同名 JOIN 笛卡尔冲突)
DO $$
DECLARE sat_mid uuid; fb_mid uuid;
BEGIN
  SELECT id INTO sat_mid FROM ops.sys_menu WHERE menu_code='cs.satisfaction.list';
  SELECT id INTO fb_mid  FROM ops.sys_menu WHERE menu_code='cs.feedback.list';

  IF sat_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
    VALUES (ops.gen_uuid_v7(), sat_mid, 'cs.satisfaction.list', '满意度查看')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;

  IF fb_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.list',   '反馈查看'),
      (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.create', '反馈登记'),
      (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.edit',   '反馈编辑'),
      (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.delete', '反馈删除'),
      (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.handle', '反馈处理')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 3) 菜单授权(sysadmin/admin/sqe/operator 可见)
INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
SELECT ops.gen_uuid_v7(), r.id, m.id
FROM ops.sys_role r CROSS JOIN ops.sys_menu m
WHERE r.role_code IN ('sysadmin','admin','sqe','operator')
  AND m.menu_code IN ('cs.satisfaction.list','cs.feedback.list')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 4) 按钮授权(sysadmin/admin 全量; sqe/operator 仅查看)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN (
    'cs.satisfaction.list','cs.feedback.list','cs.feedback.create','cs.feedback.edit',
    'cs.feedback.delete','cs.feedback.handle'
  )
ON CONFLICT (role_id, button_id) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sqe','operator')
  AND b.btn_code IN ('cs.satisfaction.list','cs.feedback.list')
ON CONFLICT (role_id, button_id) DO NOTHING;
