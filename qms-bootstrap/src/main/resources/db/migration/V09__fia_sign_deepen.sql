-- V09: FIA 深化 - 三级签名(批准人)+ 签名配置唯一约束
ALTER TABLE ops.fia_task ADD COLUMN approver_id UUID;
ALTER TABLE ops.fia_task ADD COLUMN approved_at TIMESTAMPTZ;
COMMENT ON COLUMN ops.fia_task.approver_id IS '批准人(三级签名第三签)';
COMMENT ON COLUMN ops.fia_task.approved_at IS '批准签名时间';

-- fia_sign_config 每公司一行
ALTER TABLE ops.fia_sign_config ADD CONSTRAINT uk_fia_sign_config_org UNIQUE(org_id);
