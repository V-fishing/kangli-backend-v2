-- V47: 清空旧版写死的会签链(质量/采购/研发),改为由计划的审核组(auditor_team)动态生成。
-- 会签记录在 listApprovals / startPlan 时按 auditor_team 惰性重建,清空后下次查看即生效。
DELETE FROM ops.sqm_audit_approval;
