-- FIA 首件检验含两种来源: SUPPLIER(供应商来料) 与 FACTORY(产线首件)。
-- FACTORY 来源的 FIA 任务无供应商,但 completeAndArchive 时会经 syncToTrace
-- 把合格首件作为来料批次录入 sqm_incoming_lot,导致 supplier_id 为 NULL
-- 触发 NOT NULL 约束冲突,整笔复核签名事务回滚、接口返回 500。
-- 放宽该约束:来料批次对 SUPPLIER 来源必填,FACTORY 来源允许为空。
ALTER TABLE ops.sqm_incoming_lot ALTER COLUMN supplier_id DROP NOT NULL;
COMMENT ON COLUMN ops.sqm_incoming_lot.supplier_id
  IS '供应商(来料批次;FIA 产线首件 FACTORY 来源无供应商时为 NULL)';
