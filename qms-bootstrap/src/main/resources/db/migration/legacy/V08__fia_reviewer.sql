-- V08: fia_task 加复核人字段(双签名:检验人 + 复核人)
ALTER TABLE ops.fia_task ADD COLUMN reviewer_id UUID;
ALTER TABLE ops.fia_task ADD COLUMN reviewed_at TIMESTAMPTZ;
COMMENT ON COLUMN ops.fia_task.reviewer_id IS '复核人(双签名第二签)';
COMMENT ON COLUMN ops.fia_task.reviewed_at IS '复核签名时间';
