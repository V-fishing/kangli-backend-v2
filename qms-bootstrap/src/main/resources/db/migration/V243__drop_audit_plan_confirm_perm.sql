-- 清理已废弃的审核计划确认权限码(前端已移除 confirmPlan 调用,后端端点与 Service 已删除)
-- 幂等:仅删存在的数据,可重复执行。
-- 修正:sys_button 主键列名为 id、权限码列名为 btn_code;sys_role_button 通过 button_id 关联。
DELETE FROM ops.sys_role_button
WHERE button_id = (SELECT id FROM ops.sys_button WHERE btn_code = 'sqm.audit.plan.confirm');
DELETE FROM ops.sys_button WHERE btn_code = 'sqm.audit.plan.confirm';
