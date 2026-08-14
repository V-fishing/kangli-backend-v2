-- ============================================================================
-- V97: 大批量数据(近30w)灌入前的性能优化
-- 目标: 给三张 MES 源表(qms.*)补齐索引(此前全表无索引, 任何查询/JOIN/去重均全表扫描)
--       + 给 ops.sqm_incoming_lot 补充分页/过滤覆盖索引
-- 全部使用 CREATE INDEX IF NOT EXISTS, 幂等可重复执行; 非事务性(CONCURRENTLY 不可用
-- 于事务内, 故本脚本以单条 CREATE INDEX 提交, Flyway 默认每条语句自动提交)。
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) material_inspection (来料检验) — getSourceDetail 按 record_no/barcode/batch_no
--    V90/V96 导入按 plant_code 过滤、按 record_no 去重/关联异常单、supplier_name 模糊
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_mi_record_no
    ON qms.material_inspection (record_no);
CREATE INDEX IF NOT EXISTS idx_mi_material_barcode
    ON qms.material_inspection (material_barcode);
CREATE INDEX IF NOT EXISTS idx_mi_material_batch_no
    ON qms.material_inspection (material_batch_no);
CREATE INDEX IF NOT EXISTS idx_mi_plant_arrival
    ON qms.material_inspection (plant_code, arrival_date);
CREATE INDEX IF NOT EXISTS idx_mi_supplier_name
    ON qms.material_inspection (supplier_name);
CREATE INDEX IF NOT EXISTS idx_mi_is_deleted
    ON qms.material_inspection (is_deleted);

-- ---------------------------------------------------------------------------
-- 2) finished_goods_inspection (成品/半成品检验)
--    getSourceDetail 按 (category, prod_batch_or_sn); report_no 也是常用定位键
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_fgi_cat_batch
    ON qms.finished_goods_inspection (category, prod_batch_or_sn);
CREATE INDEX IF NOT EXISTS idx_fgi_prod_batch
    ON qms.finished_goods_inspection (prod_batch_or_sn);
CREATE INDEX IF NOT EXISTS idx_fgi_report_no
    ON qms.finished_goods_inspection (report_no);
CREATE INDEX IF NOT EXISTS idx_fgi_plant_prod
    ON qms.finished_goods_inspection (plant_code, production_date);
CREATE INDEX IF NOT EXISTS idx_fgi_is_deleted
    ON qms.finished_goods_inspection (is_deleted);

-- ---------------------------------------------------------------------------
-- 3) critical_material_binding (关键件绑定)
--    getSourceDetail 按 material_barcode/product_barcode; 建树按 work_order_no 聚合
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_cmb_material_barcode
    ON qms.critical_material_binding (material_barcode);
CREATE INDEX IF NOT EXISTS idx_cmb_product_barcode
    ON qms.critical_material_binding (product_barcode);
CREATE INDEX IF NOT EXISTS idx_cmb_work_order
    ON qms.critical_material_binding (work_order_no);
CREATE INDEX IF NOT EXISTS idx_cmb_plant_scan
    ON qms.critical_material_binding (plant_code, scan_time);
CREATE INDEX IF NOT EXISTS idx_cmb_is_deleted
    ON qms.critical_material_binding (is_deleted);

-- ---------------------------------------------------------------------------
-- 4) ops.sqm_incoming_lot — 总表分页 listLotsPage 按 (org_id, created_at DESC) 排序
--    已有 idx_lot_supplier* 等, 补覆盖排序的索引, 深翻页不再回表大 offset 扫描
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_lot_org_created
    ON ops.sqm_incoming_lot (org_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_lot_org_lotno
    ON ops.sqm_incoming_lot (org_id, lot_no);
