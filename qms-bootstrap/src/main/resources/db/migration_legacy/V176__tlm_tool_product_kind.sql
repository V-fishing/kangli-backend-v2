-- 工装-产品关联: 来源改为 MES 真实产品(material_code 聚合), 补充类型与规格
ALTER TABLE ops.tlm_tool_product ADD COLUMN IF NOT EXISTS kind VARCHAR(16);
-- MATERIAL(物料) / SEMI(半成品) / FINISHED(成品)
ALTER TABLE ops.tlm_tool_product ADD COLUMN IF NOT EXISTS spec_model VARCHAR(256);
COMMENT ON COLUMN ops.tlm_tool_product.kind IS '产品类型: MATERIAL/SEMI/FINISHED';
COMMENT ON COLUMN ops.tlm_tool_product.spec_model IS '规格型号(取自 MES 源表)';
