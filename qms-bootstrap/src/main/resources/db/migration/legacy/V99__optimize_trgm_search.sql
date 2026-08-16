-- V99 模糊检索加速: pg_trgm + GIN 索引
-- 让 'ILIKE %kw%' 走 GIN 三元组索引,避免 30w 数据下全表扫描。
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 半成/成品 tab 检索 keyword 四列
CREATE INDEX IF NOT EXISTS idx_fgi_trgm_name   ON qms.finished_goods_inspection USING gin (product_name   gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_fgi_trgm_batch  ON qms.finished_goods_inspection USING gin (prod_batch_or_sn gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_fgi_trgm_mat    ON qms.finished_goods_inspection USING gin (material_code  gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_fgi_trgm_order  ON qms.finished_goods_inspection USING gin (production_order_no gin_trgm_ops);

-- 物料 tab 检索 keyword 三列
CREATE INDEX IF NOT EXISTS idx_node_trgm_name   ON ops.sqm_trace_node USING gin (node_name    gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_node_trgm_batch  ON ops.sqm_trace_node USING gin (batch_no     gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_node_trgm_mat    ON ops.sqm_trace_node USING gin (material_code gin_trgm_ops);

-- 总表 listLotsPage keyword 两列
CREATE INDEX IF NOT EXISTS idx_lot_trgm_part    ON ops.sqm_incoming_lot USING gin (part_no       gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_lot_trgm_sup     ON ops.sqm_incoming_lot USING gin (supplier_name gin_trgm_ops);
