-- 方案1:来料异常表新增可读批次号 batch_no,解决 lot_id 混存 32 位 UUID(去横杠)的问题。
-- lot_id 由 IQC/FIA 任务创建时被写成来料批次记录主键 UUID(去横杠 32 位 hex),
-- 手工创建则写可读字符串。新增 batch_no 存放可读批次号,前端优先展示 batch_no。
ALTER TABLE ops.sqm_incoming_abnormal ADD COLUMN IF NOT EXISTS batch_no VARCHAR(64);

-- 旧数据一次性回写:
-- 1) lot_id 为去横杠 UUID 且能反查到来料批次 → 填对应 lot_no
UPDATE ops.sqm_incoming_abnormal a
SET batch_no = l.lot_no
FROM ops.sqm_incoming_lot l
WHERE a.batch_no IS NULL
  AND REPLACE(l.id::text, '-', '') = a.lot_id;

-- 2) 剩余(lot_id 本身已是可读字符串)→ 直接沿用 lot_id
UPDATE ops.sqm_incoming_abnormal
SET batch_no = lot_id
WHERE batch_no IS NULL;
