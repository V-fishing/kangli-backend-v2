-- 来料异常表新增「不良数」字段(defect_qty)
ALTER TABLE ops.sqm_incoming_abnormal ADD COLUMN IF NOT EXISTS defect_qty INTEGER;
