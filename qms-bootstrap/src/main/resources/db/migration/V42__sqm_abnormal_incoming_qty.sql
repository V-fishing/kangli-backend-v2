-- 修正:qty 实际为不良数,新增来料数字段 incoming_qty(原 defect_qty 废弃)
ALTER TABLE ops.sqm_incoming_abnormal DROP COLUMN IF EXISTS defect_qty;
ALTER TABLE ops.sqm_incoming_abnormal ADD COLUMN IF NOT EXISTS incoming_qty INTEGER;
