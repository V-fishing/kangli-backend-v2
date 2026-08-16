-- V80: SPC 参数关联 FIA 检验标准项,使 SpcParam 可从检验标准库获取规格界限(URS/SRS 要求)
-- 逻辑: FiaInspStdItem(唯一真相源) ← SpcParam.fiaStdItemId(可选FK) → 自动填充 spec_lower/spec_upper/target_value/unit
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS fia_std_item_id UUID REFERENCES ops.fia_insp_std_item(id);
COMMENT ON COLUMN ops.spc_param.fia_std_item_id IS '关联的FIA检验标准项ID(可空);关联后规格字段从标准项的stdValue+tolerance派生';

CREATE INDEX IF NOT EXISTS idx_spc_param_fia_item ON ops.spc_param(fia_std_item_id);
