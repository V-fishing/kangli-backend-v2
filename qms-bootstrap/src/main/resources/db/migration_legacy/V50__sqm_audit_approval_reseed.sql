-- V50:清空旧版会签数据(原先按 auditorTeam 写死/角色码生成),
-- 改为由 V49 配置按审核类型生成。打开计划详情时会惰性重建(按配置)。
DELETE FROM ops.sqm_audit_approval;
