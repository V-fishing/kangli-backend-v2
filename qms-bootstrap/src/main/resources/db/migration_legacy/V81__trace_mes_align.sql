-- V81: 追溯模块对齐 MES（生产工单/检验阶段/组件批号/产品条码/供应商 VEN 编号）
-- 与既有 Flyway 迁移体系一致,全部幂等(ADD COLUMN IF NOT EXISTS)。

ALTER TABLE ops.sqm_trace_node       ADD COLUMN IF NOT EXISTS production_order_no VARCHAR(32);
ALTER TABLE ops.sqm_trace_node       ADD COLUMN IF NOT EXISTS stage                VARCHAR(16);

ALTER TABLE ops.sqm_trace_raw_detail ADD COLUMN IF NOT EXISTS batch_no            VARCHAR(64);

ALTER TABLE ops.sqm_trace_product_detail ADD COLUMN IF NOT EXISTS product_barcode VARCHAR(64);

ALTER TABLE ops.sqm_supplier         ADD COLUMN IF NOT EXISTS ven_code            VARCHAR(32);
ALTER TABLE ops.sqm_incoming_lot     ADD COLUMN IF NOT EXISTS ven_code            VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_supplier_ven            ON ops.sqm_supplier(ven_code);
CREATE INDEX IF NOT EXISTS idx_lot_supplier_part_iqc   ON ops.sqm_incoming_lot(org_id, supplier_id, part_no, iqc_pass);

COMMENT ON COLUMN ops.sqm_trace_node.production_order_no IS '生产工单(MES 工单枢纽 TASK_NO/WORK_ORDER)';
COMMENT ON COLUMN ops.sqm_trace_node.stage              IS 'MES 检验阶段 IQC/IPQC/SQC/FQC/OQC/RQC/PKG';
COMMENT ON COLUMN ops.sqm_trace_raw_detail.batch_no     IS '组件批号(SON_LOT_NO)';
COMMENT ON COLUMN ops.sqm_trace_product_detail.product_barcode IS '产品条码=产品批号';
COMMENT ON COLUMN ops.sqm_supplier.ven_code             IS 'MES 供应商编号(如 VEN00417)';
COMMENT ON COLUMN ops.sqm_incoming_lot.ven_code        IS 'MES 供应商编号(来料检验记录上的 VEN 编号)';
