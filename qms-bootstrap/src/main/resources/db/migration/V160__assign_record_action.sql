-- qms_assign_record 增加 action 列,区分首次指派(assign)与改派(reassign),便于追溯。
ALTER TABLE ops.qms_assign_record ADD COLUMN IF NOT EXISTS action VARCHAR(20);

COMMENT ON COLUMN ops.qms_assign_record.action IS '指派动作: assign=首次指派, reassign=改派';
