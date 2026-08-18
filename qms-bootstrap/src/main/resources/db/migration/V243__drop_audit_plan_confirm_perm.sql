-- 清理已废弃的审核计划确认权限码(前端已移除 confirmPlan 调用,后端端点与 Service 已删除)
-- 幂等:仅删存在的数据,可重复执行
DELETE FROM ops.sys_role_button WHERE button_code = 'sqm.audit.plan.confirm';
DELETE FROM ops.sys_button WHERE code = 'sqm.audit.plan.confirm';
