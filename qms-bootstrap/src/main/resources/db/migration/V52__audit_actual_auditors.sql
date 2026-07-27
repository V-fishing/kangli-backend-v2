-- V52: 审核计划新增 actual_auditors(实际参与审核人),签字后由后端同步,
-- 使「审核组栏」始终反映真实参与审核的人,而非仅计划时配置的审核组。
ALTER TABLE ops.sqm_audit_plan ADD COLUMN actual_auditors TEXT;
