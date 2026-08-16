-- V96: 重导来料批次 sqm_incoming_lot(覆盖旧数据) + 同步重导 sqm_incoming_abnormal
-- 背景: 源表 qms.material_inspection 已更新(含 supplier_name 等更全字段),
--       需覆盖 ops.sqm_incoming_lot 旧快照, 并把供应商名称等字段显式落到正式列。
--
-- 与 V90 的区别:
--   1) 新增正式列 supplier_name(text), 显式写入 mi.supplier_name。
--   2) 重导前按 org(MZ/SZ) 清空旧数据, 实现"覆盖"而非增量叠加。
--   3) 去掉 V90 的 NOT EXISTS 去重(已先清旧, 全量重插)。
--   4) INSERT 列严格对齐 sqm_incoming_lot / sqm_incoming_abnormal 当前真实表结构,
--      源字段以 material_inspection INSERT 列实测为准:
--        record_no->lot_no, supplier_code->ven_code, supplier_name->supplier_name,
--        material_barcode->material_barcode, material_code->part_no, material_name->part_name,
--        submitted_qty->qty(来料数量), arrival_date->incoming_date(到货日期),
--        inspection_result->inspect_result, purchase_order->po_no,
--        unqualified_qty->abnormal.qty, inspection_date->abnormal.occur_date
--
-- 安全约束(基于实测 FK 链):
--   * sqm_key_part_sn / sqm_iqc_inspect_record 当前为空(0 行), fia_task.lot_id 全 NULL,
--     删除 sqm_incoming_lot 旧行不会触发子表 FK 冲突, 无需级联删子表。
--   * sqm_incoming_abnormal 的 6 张子表(sqm_rectify_notice 等)当前均为空(0 行),
--     删除 abnormal 旧行安全。
--   * 占位供应商沿用 V90 已生成的 MZ/SZ 未知供应商(幂等 NOT EXISTS)。
--   * 全部在单事务内, 失败回滚。

BEGIN;

-- ============ Step 0: 新增 supplier_name 正式列 ============
ALTER TABLE ops.sqm_incoming_lot ADD COLUMN IF NOT EXISTS supplier_name TEXT;
COMMENT ON COLUMN ops.sqm_incoming_lot.supplier_name IS '来料供应商名称(MES material_inspection.supplier_name 直接映射, 覆盖旧快照)';

-- ============ Step 1: MZ/SZ 组织 UUID ============
DO $$
DECLARE
    v_cnt INT;
BEGIN
    SELECT count(*) INTO v_cnt FROM ops.sys_org WHERE org_code IN ('MZ','SZ');
    IF v_cnt < 2 THEN
        RAISE EXCEPTION 'MZ/SZ 组织不存在(当前仅 % 个), 无法重导 MES 来料数据', v_cnt;
    END IF;
END $$;

CREATE TEMPORARY TABLE tmp_org (org_code TEXT, org_id UUID) ON COMMIT DROP;
INSERT INTO tmp_org (org_code, org_id)
SELECT org_code, id FROM ops.sys_org WHERE org_code IN ('MZ','SZ');

-- 占位供应商(未知供应商-MZ / 未知供应商-SZ): 幂等, 若 V90 已生成则跳过
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

-- ============ Step 2: 清理旧数据(按组织, 仅主表 + abnormal) ============
-- 实测: sqm_key_part_sn/sqm_iqc_inspect_record 为空, fia_task.lot_id 全 NULL,
--       故删除 lot 旧行无子表 FK 冲突; abnormal 的 6 张子表均为空, 删除 abnormal 安全。
DELETE FROM ops.sqm_incoming_abnormal WHERE org_id IN (SELECT org_id FROM tmp_org);
DELETE FROM ops.sqm_incoming_lot     WHERE org_id IN (SELECT org_id FROM tmp_org);

-- ============ Step 3: 全量重插 sqm_incoming_lot(覆盖) ============
-- 目标列: id,org_id,lot_no,supplier_id,ven_code,supplier_name,
--   material_barcode,part_no,part_name,qty,unit,incoming_date,inspect_result,
--   po_no,inspect_type,iqc_pass,is_key_part,ext_json,created_at,updated_at,is_deleted
INSERT INTO ops.sqm_incoming_lot (
    id, org_id, lot_no, supplier_id, ven_code, supplier_name,
    material_barcode, part_no, part_name, qty, unit, incoming_date,
    inspect_result, po_no, inspect_type, iqc_pass, is_key_part,
    ext_json, created_at, updated_at, is_deleted
)
SELECT
    ops.gen_uuid_v7(),
    o.org_id,
    mi.record_no,
    COALESCE(s.id, ph.id),
    mi.supplier_code,
    mi.supplier_name,
    mi.material_barcode,
    mi.material_code,
    mi.material_name,
    COALESCE(NULLIF(mi.submitted_qty,'')::numeric,0)::int,
    mi.unit,
    NULLIF(mi.arrival_date,'')::date,
    mi.inspection_result,
    mi.purchase_order,
    '正常',
    false,
    false,
    to_jsonb(mi),
    now(), now(), false
FROM qms.material_inspection mi
JOIN tmp_org o ON o.org_code = mi.plant_code
LEFT JOIN ops.sqm_supplier s
       ON s.org_id = o.org_id AND s.ven_code = mi.supplier_code
CROSS JOIN LATERAL (
    SELECT id FROM ops.sqm_supplier
    WHERE org_id = o.org_id AND supplier_code = o.org_code || 'UNK'
    LIMIT 1
) ph
WHERE mi.is_deleted = '0'
  AND mi.record_no <> '';

-- ============ Step 4: 全量重插 sqm_incoming_abnormal(覆盖, 仅不合格行) ============
-- 目标列: id,org_id,abnormal_no,lot_id,supplier_id,part_no,part_name,
--   description,qty,level,occur_date,handler_id,status,incoming_qty,batch_no,created_at,updated_at,is_deleted
-- 目标列: id,org_id,abnormal_no,lot_id,supplier_id,part_no,part_name,
--   description,qty,level,occur_date,handler_id,status,overdue_days,version,
--   incoming_qty,batch_no,created_at,updated_at,is_deleted
INSERT INTO ops.sqm_incoming_abnormal (
    id, org_id, abnormal_no, lot_id, supplier_id, part_no, part_name,
    description, qty, level, occur_date, handler_id, status,
    overdue_days, version, incoming_qty, batch_no, created_at, updated_at, is_deleted
)
SELECT
    ops.gen_uuid_v7(),
    o.org_id,
    mi.record_no,
    REPLACE(mi.record_no, '-', ''),
    COALESCE(s.id, ph.id),
    mi.material_code,
    mi.material_name,
    COALESCE(NULLIF(mi.remark,''), mi.inspection_result),
    COALESCE(NULLIF(mi.unqualified_qty,'')::numeric,0)::int,
    '一般',
    NULLIF(mi.inspection_date,'')::date,
    NULL,
    CASE WHEN mi.inspection_result = '不合格' THEN '待处理' ELSE '已关闭' END,
    0,
    0,
    COALESCE(NULLIF(mi.submitted_qty,'')::numeric,0)::int,
    mi.material_batch_no,
    now(), now(), false
FROM qms.material_inspection mi
JOIN tmp_org o ON o.org_code = mi.plant_code
LEFT JOIN ops.sqm_supplier s
       ON s.org_id = o.org_id AND s.ven_code = mi.supplier_code
CROSS JOIN LATERAL (
    SELECT id FROM ops.sqm_supplier
    WHERE org_id = o.org_id AND supplier_code = o.org_code || 'UNK'
    LIMIT 1
) ph
WHERE mi.is_deleted = '0'
  AND mi.record_no <> ''
  AND mi.inspection_result = '不合格';

COMMIT;
