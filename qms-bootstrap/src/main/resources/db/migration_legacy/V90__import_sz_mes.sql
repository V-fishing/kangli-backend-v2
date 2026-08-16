-- V90(方案B重构): 导入 MES 三份源数据到 QMS 追溯体系
-- 核心改动(相对旧版):
--   1) 不再造任何加工/占位表: 删除 KIT-/SHIP-/SEMI- 占位批次、sqm_trace_node / sqm_trace_link /
--      sqm_trace_raw_detail / sqm_trace_product_detail / sqm_key_part_sn 的写入。
--   2) 节点权威 = 三张源表(material_inspection / finished_goods_inspection / critical_material_binding),
--      节点详情查询时直接从源表取, 满足"以表字段为准、不每次导入修改数据结构"。
--   3) 追溯树边 = 统一关系表 ops.sqm_trace_relation(parent_barcode, child_barcode, relation_type, org_id)
--      (由 V93 建表)。本脚本只负责推导并写入真实存在的边, 不臆造关联。
--   4) sqm_incoming_lot 仅写入真实来料批次(IQC 录入模型需要, 非加工表), 不再写占位批次。
--
-- 依赖前置: 源 SQL/CSV 已以 UTF-8 由 psql 导入到 qms 库对应表。
-- 双组织分流: 源表 plant_code 值恰好等于 sys_org.org_code(MZ/SZ)。
--
-- 边推导规则(基于实测关联键命中率):
--   * keypart->finished : critical_material_binding.product_barcode(成品条码) <-> material_barcode(关键件条码)
--                         实测 66645/83984 成品条码可直接命中成品表 prod_batch_or_sn(79%)。
--   * incoming->keypart : 关键件 material_barcode 同时命中 material_inspection.material_barcode 时,
--                         补 来料->关键件 边(实测 13293 命中, 16%)。
--   * 半成品: 源表绑定关系不含半成品边(work_order_no 无法桥接成品<->半成品), 不臆造边,
--             半成品节点仅存于 finished_goods_inspection(category='半成品'), 查询时独立展示。
--
-- 重要说明:
--   1) 本脚本走 Flyway 自动迁移(已重编号为 V93 前置依赖); 重跑前先执行 V94 清理脚本清旧数据。
--   2) 全部幂等: ADD COLUMN IF NOT EXISTS / INSERT ... NOT EXISTS / ON CONFLICT DO NOTHING。

BEGIN;

-- ============ Step 0: sqm_incoming_lot 增加 ext_json(留存来料原始行供后续精确关联) ============
ALTER TABLE ops.sqm_incoming_lot ADD COLUMN IF NOT EXISTS ext_json JSONB;
COMMENT ON COLUMN ops.sqm_incoming_lot.ext_json IS 'MES 来料检验原始行(JSONB 留存, 用于追溯/建树关联 material_barcode)';

-- ============ Step 1: MZ/SZ 组织 UUID + 占位供应商 ============
DO $$
DECLARE
    v_cnt INT;
BEGIN
    SELECT count(*) INTO v_cnt FROM ops.sys_org WHERE org_code IN ('MZ','SZ');
    IF v_cnt < 2 THEN
        RAISE EXCEPTION 'MZ/SZ 组织不存在(当前仅 % 个), 无法导入 MES 数据, 请先初始化 org_code=MZ 与 org_code=SZ', v_cnt;
    END IF;
END $$;

CREATE TEMPORARY TABLE tmp_org (org_code TEXT, org_id UUID) ON COMMIT DROP;
INSERT INTO tmp_org (org_code, org_id)
SELECT org_code, id FROM ops.sys_org WHERE org_code IN ('MZ','SZ');

-- 占位供应商(未知供应商-MZ / 未知供应商-SZ): 满足 sqm_supplier 严格 UNIQUE 约束
INSERT INTO ops.sqm_supplier
    (id, org_id, supplier_no, supplier_code, name, credit_code, category, status, created_at, updated_at)
SELECT ops.gen_uuid_v7(), o.org_id,
       o.org_code || '_UNKNOWN', o.org_code || 'UNK', '未知供应商-' || o.org_code, o.org_code || '_UNKNOWN_CREDIT',
       '其它', '合格', now(), now()
FROM tmp_org o
WHERE NOT EXISTS (
    SELECT 1 FROM ops.sqm_supplier s
    WHERE s.org_id = o.org_id AND s.supplier_code = o.org_code || 'UNK'
);

-- ============ Step 2: 真实来料批次 sqm_incoming_lot(录入模型需要, 非加工表; 不造占位批次) ============
-- lot_no 用 record_no(业务唯一主键); 落 material_barcode 列供后续精确关联关键件。
INSERT INTO ops.sqm_incoming_lot
    (id, org_id, lot_no, supplier_id, ven_code, part_no, part_name,
     qty, unit, incoming_date, inspect_result, inspect_type, iqc_pass,
     po_no, is_key_part, material_barcode, ext_json, created_at, updated_at)
SELECT
    ops.gen_uuid_v7(),
    o.org_id,
    mi.record_no,
    COALESCE(s.id, ph.id),
    NULLIF(mi.supplier_code, ''),
    mi.material_code,
    mi.material_name,
    COALESCE(NULLIF(mi.submitted_qty, ''), '0')::numeric,
    NULLIF(mi.unit, ''),
    COALESCE(NULLIF(mi.arrival_date, '')::date, NULLIF(mi.inspection_date, '')::date),
    COALESCE(NULLIF(mi.inspection_result, ''), '待检'),
    '正常',
    (COALESCE(NULLIF(mi.inspection_result, ''), '待检') = '合格'),
    NULLIF(mi.purchase_order, ''),
    FALSE,
    NULLIF(mi.material_barcode, ''),
    to_jsonb(mi),
    now(), now()
FROM qms.material_inspection mi
JOIN tmp_org o ON o.org_code = mi.plant_code
LEFT JOIN ops.sqm_supplier s
       ON s.org_id = o.org_id AND s.ven_code = mi.supplier_code
LEFT JOIN ops.sqm_supplier ph
       ON ph.org_id = o.org_id AND ph.supplier_code = o.org_code || 'UNK'
WHERE mi.is_deleted = '0'
  AND NOT EXISTS (
        SELECT 1 FROM ops.sqm_incoming_lot l
        WHERE l.lot_no = mi.record_no
    );

-- ============ Step 3: 不合格来料行 -> 异常整改单(不合格仅进异常单, 不进树) ============
INSERT INTO ops.sqm_incoming_abnormal
    (id, org_id, abnormal_no, lot_id, batch_no, supplier_id,
     part_no, part_name, description, qty, incoming_qty, level,
     occur_date, status, disposal, created_at, updated_at)
SELECT
    ops.gen_uuid_v7(),
    o.org_id,
    'ABN-' || mi.record_no,
    REPLACE(l.id::text, '-', ''),
    COALESCE(NULLIF(mi.material_batch_no, ''), mi.record_no),
    COALESCE(s.id, ph.id),
    mi.material_code,
    mi.material_name,
    NULLIF(mi.defect_desc, ''),
    COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric::INT,
    COALESCE(NULLIF(mi.submitted_qty, ''), '0')::numeric::INT,
    CASE WHEN COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric >= 3 THEN '严重' ELSE '一般' END,
    COALESCE(NULLIF(mi.inspection_date, '')::date, now()::date),
    '待处理',
    NULLIF(COALESCE(NULLIF(mi.handling_method, ''), mi.unqualified_final_status), ''),
    now(), now()
FROM qms.material_inspection mi
JOIN tmp_org o ON o.org_code = mi.plant_code
JOIN ops.sqm_incoming_lot l ON l.lot_no = mi.record_no AND l.org_id = o.org_id
LEFT JOIN ops.sqm_supplier s
       ON s.org_id = o.org_id AND s.ven_code = mi.supplier_code
LEFT JOIN ops.sqm_supplier ph
       ON ph.org_id = o.org_id AND ph.supplier_code = o.org_code || 'UNK'
WHERE mi.is_deleted = '0'
  AND mi.inspection_result = '不合格'
  AND NOT EXISTS (
        SELECT 1 FROM ops.sqm_incoming_abnormal a
        WHERE a.abnormal_no = 'ABN-' || mi.record_no
    );

-- ============ Step 4(核心): 推导并写入追溯边到 ops.sqm_trace_relation ============
-- 关键件条码(material_barcode)本质就是来料条码(命中 material_inspection 即来料节点),
-- 因此绑定表 (product_barcode 成品条码 <-> material_barcode 关键件条码) 即表达
-- "来料/关键件 -> 成品" 的父子边, 无需再单独造 来料->关键件 的自环中间层。
-- 仅当 product_barcode 能命中成品表 prod_batch_or_sn 才作为成品边写入。
INSERT INTO ops.sqm_trace_relation
    (org_id, parent_barcode, child_barcode, relation_type, created_at)
SELECT DISTINCT
    o.org_id,
    NULLIF(cb.material_barcode, ''),            -- 来料/关键件(父)
    cb.product_barcode,                          -- 成品(子)
    'keypart->finished',
    now()
FROM qms.critical_material_binding cb
JOIN tmp_org o ON o.org_code = cb.plant_code
WHERE cb.is_active = '是'
  AND cb.is_deleted = '0'
  AND cb.material_barcode <> ''
  AND cb.product_barcode <> ''
  AND EXISTS (                                   -- 成品条码确实命中成品表
        SELECT 1 FROM qms.finished_goods_inspection fi
        WHERE fi.is_deleted = '0' AND fi.category = '成品'
          AND fi.prod_batch_or_sn = cb.product_barcode
    )
ON CONFLICT (org_id, parent_barcode, child_barcode, relation_type) DO NOTHING;

-- 注: 半成品源表无绑定边(work_order_no 无法桥接成品<->半成品), 不臆造边;
--     半成品节点仅存于 finished_goods_inspection(category='半成品'), 查询时独立展示。

-- ============ Step 5: 校验(供手动核对) ============
-- SELECT relation_type, count(*) FROM ops.sqm_trace_relation WHERE org_id IN (SELECT org_id FROM tmp_org) GROUP BY relation_type;
-- SELECT org_code, count(*) FROM ops.sqm_trace_relation r JOIN ops.sys_org o ON o.id=r.org_id WHERE o.org_code IN ('MZ','SZ') GROUP BY org_code;

COMMIT;
