-- V97: 从 MES material_inspection 生成真实供应商主数据, 并回填来料异常/批次的 supplier_id
--
-- 背景: V90/V96 导入来料批次/异常时, ops.sqm_supplier 仅有占位"未知供应商-MZ/SZ"两行,
--       源 material_inspection.supplier_code / supplier_name 含真实供应商, 但无对应主数据,
--       导致 s.ven_code = mi.supplier_code 全部 LEFT JOIN 落空, supplier_id 指向占位供应商,
--       页面供应商列一律显示"未知供应商-MZ"。
--
-- 修复:
--   1) 按 (plant_code, supplier_code) 去重, 生成真实供应商主数据; 匹配键 ven_code = mi.supplier_code
--      (与 V90/V96 导入 JOIN 键一致, 保证回填可命中)。
--   2) 用 lot.ven_code 回填 sqm_incoming_lot.supplier_id (仅更新当前为占位的行)。
--   3) 通过 lot 关联回填 sqm_incoming_abnormal.supplier_id (仅更新当前为占位的行)。
--
-- 约束处理:
--   * sqm_supplier.supplier_code VARCHAR(16) NOT NULL UNIQUE (全局): 用 org_code||'_'||supplier_code 派生防跨组织冲突, left(,16)。
--   * supplier_no / credit_code VARCHAR(32) UNIQUE: 用 md5 派生, 保证全局唯一且长度合规。
--   * ven_code 由 V81 添加(VARCHAR(32)), 非唯一, 允许多供应商共用同一 MES 编码。
--
-- 幂等: 供应商 INSERT 用 NOT EXISTS(ven_code) 去重; 回填只动占位行; 可重跑。

BEGIN;

-- ============ Step 0: MZ/SZ 组织 UUID ============
DO $$
DECLARE
    v_cnt INT;
BEGIN
    SELECT count(*) INTO v_cnt FROM ops.sys_org WHERE org_code IN ('MZ','SZ');
    IF v_cnt < 2 THEN
        RAISE EXCEPTION 'MZ/SZ 组织不存在(当前仅 % 个), 无法生成供应商主数据', v_cnt;
    END IF;
END $$;

CREATE TEMPORARY TABLE tmp_org (org_code TEXT, org_id UUID) ON COMMIT DROP;
INSERT INTO tmp_org (org_code, org_id)
SELECT org_code, id FROM ops.sys_org WHERE org_code IN ('MZ','SZ');

-- ============ Step 1: 生成真实供应商主数据 ============
INSERT INTO ops.sqm_supplier
    (id, org_id, supplier_no, supplier_code, name, credit_code, category, status, ven_code, created_at, updated_at, is_deleted)
SELECT
    ops.gen_uuid_v7(),
    o.org_id,
    md5(o.org_code || '|' || mi.supplier_code),
    left(o.org_code || '_' || mi.supplier_code, 16),
    CASE WHEN mi.supplier_name IS NULL OR mi.supplier_name = ''
         THEN '供应商-' || mi.supplier_code
         ELSE mi.supplier_name END,
    'C' || right(md5(o.org_code || '|' || mi.supplier_code), 31),
    '其它', '合格',
    mi.supplier_code,
    now(), now(), false
FROM (
    SELECT DISTINCT ON (plant_code, supplier_code)
        plant_code,
        supplier_code,
        COALESCE(NULLIF(supplier_name, ''), supplier_code) AS supplier_name
    FROM qms.material_inspection
    WHERE is_deleted = '0'
      AND supplier_code IS NOT NULL
      AND supplier_code <> ''
      AND supplier_code NOT IN ('未知', 'UNKNOWN', '无', '-', 'N/A')
    ORDER BY plant_code, supplier_code, supplier_name
) mi
JOIN tmp_org o ON o.org_code = mi.plant_code
WHERE NOT EXISTS (
    SELECT 1 FROM ops.sqm_supplier s
    WHERE s.org_id = o.org_id AND s.ven_code = mi.supplier_code
);

-- ============ Step 2: 回填 sqm_incoming_lot.supplier_id ============
UPDATE ops.sqm_incoming_lot l
SET supplier_id = s.id
FROM ops.sqm_supplier s
WHERE s.org_id = l.org_id
  AND s.ven_code = l.ven_code
  AND l.ven_code IS NOT NULL AND l.ven_code <> ''
  AND EXISTS (
    SELECT 1 FROM ops.sqm_supplier p
    WHERE p.id = l.supplier_id AND p.supplier_code IN ('MZUNK', 'SZUNK')
  );

-- ============ Step 3: 回填 sqm_incoming_abnormal.supplier_id (经 lot 关联) ============
UPDATE ops.sqm_incoming_abnormal a
SET supplier_id = l.supplier_id
FROM ops.sqm_incoming_lot l
WHERE l.org_id = a.org_id
  AND l.lot_no = a.abnormal_no
  AND l.supplier_id IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM ops.sqm_supplier p
    WHERE p.id = a.supplier_id AND p.supplier_code IN ('MZUNK', 'SZUNK')
  );

COMMIT;
