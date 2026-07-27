-- V25: FIA 来料批次驱动,标准库按 物料编码 + 供应商 + 工序 自动匹配
-- 让"来料之后该物料自动被标准库中的相关标准匹配,工厂只需输入实测值即可判定合规"

-- 标准库:增加按编码+供应商匹配维度
ALTER TABLE ops.fia_insp_std ADD COLUMN IF NOT EXISTS part_no varchar(64);
ALTER TABLE ops.fia_insp_std ADD COLUMN IF NOT EXISTS supplier_id uuid REFERENCES ops.sqm_supplier(id);

-- 任务:记录来源(来料批次 / 物料编码 / 供应商),便于追溯
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS part_no varchar(64);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS supplier_id uuid REFERENCES ops.sqm_supplier(id);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS lot_id uuid REFERENCES ops.sqm_incoming_lot(id);

-- 检验计划:增加供应商维度,区分同一物料不同供应商的标准
ALTER TABLE ops.fia_insp_plan ADD COLUMN IF NOT EXISTS supplier_id uuid REFERENCES ops.sqm_supplier(id);

-- 按 (物料编码, 供应商, 工序) 的唯一生效标准,避免重复
CREATE UNIQUE INDEX IF NOT EXISTS idx_fia_insp_std_part_sup_proc
    ON ops.fia_insp_std (org_id, part_no, supplier_id, proc_name)
    WHERE is_deleted = false AND status = '生效' AND part_no IS NOT NULL;

COMMENT ON COLUMN ops.fia_insp_std.part_no IS '物料编码(来料批次驱动匹配键)';
COMMENT ON COLUMN ops.fia_insp_std.supplier_id IS '供应商ID(来料批次驱动匹配键)';
COMMENT ON COLUMN ops.fia_task.lot_id IS '来源来料批次ID';
