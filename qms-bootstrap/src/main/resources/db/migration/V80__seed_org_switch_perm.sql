-- V80 种子:切换分公司权限码 system.org.switch(挂 system.org.list 菜单,授权 sysadmin)。
-- 与后端 OrgSwitchFilter / KPI 对比页权限 (@PreAuthorize) 对齐;可分配给其他角色以开放切换能力。
-- 幂等:重复执行不报错、不重复插入。

INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name)
SELECT ops.gen_uuid_v7(), m.id, 'system.org.switch', '切换分公司'
FROM ops.sys_menu m
WHERE m.menu_code = 'system.org.list'
ON CONFLICT (menu_id, btn_code) DO NOTHING;

INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r
CROSS JOIN ops.sys_button b
WHERE r.role_code = 'sysadmin' AND r.org_id IS NULL
  AND b.btn_code = 'system.org.switch'
ON CONFLICT (role_id, button_id) DO NOTHING;
