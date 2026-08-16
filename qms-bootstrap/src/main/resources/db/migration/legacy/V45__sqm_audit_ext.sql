-- 审核类型特有字段(JSONB)
-- 主表保留共性字段,各审核类型(年度复审/过程审核/专项审核/飞行检查/物料变更审核...)的差异字段
-- 统一存放在 ext_json 中,按 audit_type 区分,新增审核类型无需改表结构。
ALTER TABLE ops.sqm_audit_plan ADD COLUMN ext_json JSONB;
ALTER TABLE ops.sqm_audit_record ADD COLUMN ext_json JSONB;

COMMENT ON COLUMN ops.sqm_audit_plan.ext_json IS '审核类型特有字段(JSON 字符串),按 audit_type 差异化';
COMMENT ON COLUMN ops.sqm_audit_record.ext_json IS '审核记录类型特有字段(JSON 字符串)';
