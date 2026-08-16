-- 审核计划增加 change_id,用于「物料变更审核」与变更单双向追溯
ALTER TABLE ops.sqm_audit_plan ADD COLUMN IF NOT EXISTS change_id VARCHAR(64);
COMMENT ON COLUMN ops.sqm_audit_plan.change_id IS '来源变更单 id(物料变更审核由变更单提交联动生成,用于双向追溯)';
