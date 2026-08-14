-- V174: 工装主表增加「采购日期」字段(purchase_date)。
-- 语义独立于入库日期(inbound_date), 记录资产采购入账时间, 用于全生命周期台账透明度。
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS purchase_date DATE;
COMMENT ON COLUMN ops.tlm_tooling.purchase_date IS '采购日期(资产入账时间)';
