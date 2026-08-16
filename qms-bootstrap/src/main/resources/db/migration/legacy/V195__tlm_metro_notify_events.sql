-- V195 计量模块补充通知事件配置(强约束 C2): 维修完成待校准提醒 + 校准临期预警。
-- 接收角色复用 sqe(计量管理员); 渠道站内弹窗; 全部幂等。
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT 'tlm', 'tlm_repair_done', '计量器具维修完成待校准', 'sqe', '站内弹窗', true
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='tlm' AND event_code='tlm_repair_done');

INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT 'tlm', 'tlm_calib_due', '计量器具校准临期预警', 'sqe', '站内弹窗', true
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_config WHERE module='tlm' AND event_code='tlm_calib_due');

-- 补充计量锁定按钮码(计量页「更多 ▾」含 锁定/解锁 操作, 与 tlm.tooling.lock 并行授权)
DO $$
BEGIN
  INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
  SELECT ops.gen_uuid_v7(), m.id, 'tlm.metro.lock', '计量锁定'
  FROM ops.sys_menu m WHERE m.menu_code = 'tlm.metro.list'
  ON CONFLICT (menu_id, btn_code) DO NOTHING;
END $$;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code = 'tlm.metro.lock'
ON CONFLICT (role_id, button_id) DO NOTHING;
