-- 供应商审核计划新增"审核组长用户 ID"字段,支撑按当前登录用户聚合"我的任务"(与 V150 8D 对齐)。
-- audit_lead(姓名文本)已落地,此处补 ID 作为一等公民,后续改派/聚合复用。
ALTER TABLE ops.sqm_audit_plan ADD COLUMN IF NOT EXISTS audit_lead_user_id VARCHAR(36);

-- 历史数据兜底:按 audit_lead 姓名反查 sys_user.real_name 回填。
UPDATE ops.sqm_audit_plan p
SET audit_lead_user_id = u.id::text
FROM ops.sys_user u
WHERE p.audit_lead_user_id IS NULL
  AND p.audit_lead IS NOT NULL
  AND TRIM(p.audit_lead) <> ''
  AND u.real_name = TRIM(p.audit_lead);

COMMENT ON COLUMN ops.sqm_audit_plan.audit_lead_user_id IS '审核组长用户 ID(对应 ops.sys_user.id)';
