-- V70: SPC 参数关联供应商,使 CPK 能力等级可联动更新供应商质量分
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS supplier_id UUID REFERENCES ops.sqm_supplier(id);
COMMENT ON COLUMN ops.spc_param.supplier_id IS '关联供应商:该工序参数归属的供应商,用于 CPK→供应商质量分联动';
