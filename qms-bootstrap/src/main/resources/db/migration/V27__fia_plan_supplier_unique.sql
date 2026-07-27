-- V27: fia_insp_plan 唯一索引增加供应商维度,允许同一物料不同供应商各自一套计划
-- 与 V25 的 idx_fia_insp_std_part_sup_proc 对齐,支撑"来料批次按 物料编码+供应商+工序 自动匹配"

DROP INDEX IF EXISTS ops.idx_fia_insp_plan_cat_proc;

CREATE UNIQUE INDEX IF NOT EXISTS idx_fia_insp_plan_cat_sup_proc
    ON ops.fia_insp_plan (org_id, material_category, supplier_id, proc_name)
    WHERE is_deleted = false;
