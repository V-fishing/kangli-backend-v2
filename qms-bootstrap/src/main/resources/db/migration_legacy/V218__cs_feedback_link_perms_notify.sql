-- ============================================================
-- V218 售后管理 · 客户反馈联动质量改进(2.4.2.5) 权限码 + 通知配置(跨端协同强约束 C1/C2)。
-- 1) 按钮码 cs.feedback.link(反馈发起 8D/CAPA 联动) 挂 cs.feedback.list 菜单。
-- 2) 通知配置: cs_fb_lowscore(低分自动流转) / cs_fb_ncm(反馈联动质量改进)。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 按钮码: 反馈联动发起
DO $$
DECLARE fb_mid uuid;
BEGIN
  SELECT id INTO fb_mid FROM ops.sys_menu WHERE menu_code='cs.feedback.list';
  IF fb_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
    VALUES (ops.gen_uuid_v7(), fb_mid, 'cs.feedback.link', '反馈联动')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 2) 按钮授权(sysadmin/admin 可联动)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'cs.feedback.link'
ON CONFLICT (role_id, button_id) DO NOTHING;

-- 3) 通知配置种子(接收角色 sqe; 渠道站内弹窗)
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT v.module, v.event_code, v.event_name, v.role_codes, v.channels, v.enabled
FROM (VALUES
  ('cs','cs_fb_lowscore', '低分反馈自动流转', 'sqe', '站内弹窗', true),
  ('cs','cs_fb_ncm',     '反馈联动质量改进', 'sqe', '站内弹窗', true)
) AS v(module, event_code, event_name, role_codes, channels, enabled)
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='cs' AND event_code=v.event_code);
