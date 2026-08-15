-- V186: 撤销悬空权限码 tlm.tooling.export(台账导出)。
-- 该码在 V170 被登记并授权给 sysadmin/admin, 但 TLM 全模块(含其他业务模块)均未实现任何
-- 导出(Excel)功能, 既无前端按钮也无后端端点。为避免「登记了权限码却无对应 UI/接口」的悬空配置,
-- 删除该按钮码及其角色授权, 使 sys_button / sys_role_button 与实际功能保持一致。
-- 幂等: 按 btn_code 删除, 重复执行安全(无匹配行时 DELETE 0 行)。
DELETE FROM ops.sys_role_button
WHERE button_id IN (SELECT id FROM ops.sys_button WHERE btn_code = 'tlm.tooling.export');

DELETE FROM ops.sys_button WHERE btn_code = 'tlm.tooling.export';
