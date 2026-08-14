-- V98 半成/成品 检索性能索引(为 30w 数据量准备)
-- 1) 半成品/成品 tab 检索(searchProductLikeNodes)默认按 production_date DESC 排序并过滤 category,
--    复合索引覆盖 过滤+排序,避免 30w 下全表扫描+排序。
CREATE INDEX IF NOT EXISTS idx_fgi_cat_prod_date
    ON qms.finished_goods_inspection (category, production_date DESC);

-- 2) keyword 模糊检索(ILIKE '%kw%') 的 4 个常用列,建各自单列索引。
--    PostgreSQL B-tree 对 '%kw%' 前缀无关 LIKE 不能直接用,但单列索引仍能在
--    无 keyword 时配合上面的复合索引;此处主要为后续接入 pg_trgm 预留。
CREATE INDEX IF NOT EXISTS idx_fgi_product_name   ON qms.finished_goods_inspection (product_name);
CREATE INDEX IF NOT EXISTS idx_fgi_prod_batch     ON qms.finished_goods_inspection (prod_batch_or_sn);
CREATE INDEX IF NOT EXISTS idx_fgi_material_code  ON qms.finished_goods_inspection (material_code);
CREATE INDEX IF NOT EXISTS idx_fgi_order_no       ON qms.finished_goods_inspection (production_order_no);

-- 3) 物料 tab 检索(searchNodes) keyword ILIKE 的常用列,缓解 30w 下模糊检索全表扫。
CREATE INDEX IF NOT EXISTS idx_node_name      ON ops.sqm_trace_node (node_name);
CREATE INDEX IF NOT EXISTS idx_node_batch_no  ON ops.sqm_trace_node (batch_no);
CREATE INDEX IF NOT EXISTS idx_node_mat_code  ON ops.sqm_trace_node (material_code);

-- 4) 总表 listLotsPage keyword 模糊检索常用列。
CREATE INDEX IF NOT EXISTS idx_lot_part_no       ON ops.sqm_incoming_lot (org_id, part_no);
CREATE INDEX IF NOT EXISTS idx_lot_supplier_name ON ops.sqm_incoming_lot (org_id, supplier_name);
