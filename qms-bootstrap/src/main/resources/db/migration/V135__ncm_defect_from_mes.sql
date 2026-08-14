-- V135: 把 SZ-MES 三张导入表中的"不合格"检验记录映射为 NCM 不良记录。
-- 目的: 让真实业务数据(来料/成品/半成品检验不合格)真正进入"不良管理",
--       与 dev 环境 NcmDefectDemoSeeder 灌入的演示数据(来源=手动)并存且可用 source 区分。
--
-- 映射范围(判定规则: unqualified_qty::numeric > 0 视为该检验批有不良, 覆盖所有业务结果文本):
--   * material_inspection(来料)    unqualified_qty>0 -> stage='来料不良', process_code='IQC'
--   * finished_goods_inspection(成品)   unqualified_qty>0 AND category='成品' -> stage='成品不良', process_code='FQC'
--   * finished_goods_inspection(半成品) unqualified_qty>0 AND category='半成品' -> stage='半成品不良', process_code='IPQC'
--   * critical_material_binding 是绑定关系表, 无检验结果概念, 不映射。
-- 说明: SZ-MES 成品/半成品的"不良"并非都用 inspection_result='不合格' 表示, 而是分散在
--       挑选/返工/报废/合格的入库、不合格的返工 等业务结果里; 但它们的 unqualified_qty 均 > 0。
--       统一用 unqualified_qty>0 判定, 可覆盖来料/半成品/成品的全部真实不良。
--
-- 约定:
--   * 占位操作人 MES_SYNC(每组织一个), 见下方 DO 块。
--   * 通用不良字典 MES('MES导入不良', 全局, org_id=NULL), 所有 MES 导入记录统一引用。
--   * source='MES导入'(VARCHAR(8), 4 字符), 天然区别于 Seeder 的'手动'。
--   * defect_no 以 'MESMAT-'/ 'MESFIN-' 前缀保证唯一且可重跑。
--
-- 全部幂等: INSERT ... NOT EXISTS / 字典/操作人 INSERT ... WHERE NOT EXISTS。

BEGIN;

-- ============ Step 0: 清空旧版 MES 导入记录(保证幂等重跑时与最新判定规则一致) ============
-- 旧版曾仅按 inspection_result='不合格' 映射(漏掉成品/半成品的挑选/返工/报废等不良, 且来料含 unqualified_qty=0 脏数据)。
-- 删除后由下方 Step 3/4 以 unqualified_qty>0 重新映射, 确保数据干净。
-- 仅删除本迁移产生的 MES导入记录, 不影响手动/演示数据。
DELETE FROM ops.ncm_defect_record WHERE source = 'MES导入' AND is_deleted = false;

-- ============ Step 1: 占位操作人 MES_SYNC(每组织 MZ/SZ) ============
DO $$
DECLARE
    v_org_code TEXT;
    v_org_id   UUID;
    v_uid      UUID;
BEGIN
    FOR v_org_code, v_org_id IN
        SELECT org_code, id FROM ops.sys_org WHERE org_code IN ('MZ', 'SZ')
    LOOP
        SELECT id INTO v_uid FROM ops.sys_user WHERE username = v_org_code || '_mes_sync';
        IF v_uid IS NULL THEN
            v_uid := ops.gen_uuid_v7();
            INSERT INTO ops.sys_user
                (id, org_id, username, password_hash, real_name, status, created_at, updated_at, is_deleted, version)
            VALUES
                (v_uid, v_org_id, v_org_code || '_mes_sync',
                 '$2a$10$placeholder_mes_sync_not_used_for_login', 'MES同步', '1', now(), now(), false, 0);
        END IF;
    END LOOP;
END $$;

-- ============ Step 2: 通用不良字典 MES(全局, org_id=NULL) ============
INSERT INTO ops.ncm_defect_dict (id, org_id, code, name, category, level, status, created_at, is_deleted, version)
SELECT ops.gen_uuid_v7(), NULL, 'MES', 'MES导入不良', '其它', '一般', '启用', now(), false, 0
WHERE NOT EXISTS (SELECT 1 FROM ops.ncm_defect_dict WHERE code = 'MES');

-- ============ Step 3: 来料不合格 -> 不良记录 ============
INSERT INTO ops.ncm_defect_record
    (id, org_id, defect_no, wo_no, process_code, defect_dict_code, severity,
     defect_count, batch_total, defect_rate, batch_no, product_model, operator_id,
     source, stage, occurred_at, remark, created_at, updated_at, is_deleted, version)
SELECT
    ops.gen_uuid_v7(),
    o.id,
    'MESMAT-' || mi.record_no,
    COALESCE(NULLIF(mi.record_no, ''), 'WO-NA'),
    'IQC',
    'MES',
    CASE WHEN COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric >= 3 THEN '严重' ELSE '一般' END,
    GREATEST(1, COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric::INT),
    COALESCE(NULLIF(mi.submitted_qty, ''), '0')::numeric::INT,
    CASE WHEN COALESCE(NULLIF(mi.submitted_qty, ''), '0')::numeric > 0
         THEN ROUND(COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric
                    / COALESCE(NULLIF(mi.submitted_qty, ''), '0')::numeric, 4)
         ELSE NULL END,
    NULLIF(mi.material_batch_no, ''),
    LEFT(NULLIF(mi.material_code, ''), 64),
    u.id,
    'MES导入',
    '来料不良',
    COALESCE(NULLIF(mi.inspection_date, '')::timestamptz, now()),
    NULLIF(COALESCE(NULLIF(mi.defect_desc, ''), mi.handling_method), ''),
    now(), now(), false, 0
FROM qms.material_inspection mi
JOIN ops.sys_org o ON o.org_code = mi.plant_code
JOIN ops.sys_user u ON u.org_id = o.id AND u.username = o.org_code || '_mes_sync'
WHERE mi.is_deleted = '0'
  AND COALESCE(NULLIF(mi.unqualified_qty, ''), '0')::numeric > 0
  AND NOT EXISTS (
        SELECT 1 FROM ops.ncm_defect_record d WHERE d.defect_no = 'MESMAT-' || mi.record_no
    );

-- ============ Step 4: 成品/半成品不合格 -> 不良记录 ============
INSERT INTO ops.ncm_defect_record
    (id, org_id, defect_no, wo_no, process_code, defect_dict_code, severity,
     defect_count, batch_total, defect_rate, batch_no, product_model, operator_id,
     source, stage, occurred_at, remark, created_at, updated_at, is_deleted, version)
SELECT
    ops.gen_uuid_v7(),
    o.id,
    'MESFIN-' || fi.report_no,
    COALESCE(NULLIF(fi.production_order_no, ''), 'WO-NA'),
    CASE WHEN fi.category = '半成品' THEN 'IPQC' ELSE 'FQC' END,
    'MES',
    CASE WHEN COALESCE(NULLIF(fi.unqualified_qty, ''), '0')::numeric >= 3 THEN '严重' ELSE '一般' END,
    GREATEST(1, COALESCE(NULLIF(fi.unqualified_qty, ''), '0')::numeric::INT),
    COALESCE(NULLIF(fi.submitted_qty, ''), NULLIF(fi.inspected_qty, ''), '0')::numeric::INT,
    CASE WHEN COALESCE(NULLIF(fi.submitted_qty, ''), NULLIF(fi.inspected_qty, ''), '0')::numeric > 0
         THEN ROUND(COALESCE(NULLIF(fi.unqualified_qty, ''), '0')::numeric
                    / COALESCE(NULLIF(fi.submitted_qty, ''), NULLIF(fi.inspected_qty, ''), '0')::numeric, 4)
         ELSE NULL END,
    NULLIF(fi.prod_batch_or_sn, ''),
    LEFT(NULLIF(fi.product_name || COALESCE(' ' || NULLIF(fi.model_spec, ''), ''), ''), 64),
    u.id,
    'MES导入',
    CASE WHEN fi.category = '半成品' THEN '半成品不良' ELSE '成品不良' END,
    COALESCE(NULLIF(fi.production_date, '')::timestamptz, now()),
    NULLIF(fi.qc_review, ''),
    now(), now(), false, 0
FROM qms.finished_goods_inspection fi
JOIN ops.sys_org o ON o.org_code = fi.plant_code
JOIN ops.sys_user u ON u.org_id = o.id AND u.username = o.org_code || '_mes_sync'
WHERE fi.is_deleted = '0'
  AND COALESCE(NULLIF(fi.unqualified_qty, ''), '0')::numeric > 0
  AND fi.category IN ('成品', '半成品')
  AND NOT EXISTS (
        SELECT 1 FROM ops.ncm_defect_record d WHERE d.defect_no = 'MESFIN-' || fi.report_no
    );

COMMIT;
