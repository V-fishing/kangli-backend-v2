-- 追溯节点增加 material_code(物料号) 列, 支撑"按物料号正向/召回追溯"(Q3)。
-- 物料条码 = 批号(batch_no) + 物料号(material_code); 此前物料号只散落在各明细表, 节点行缺失, 导致按物料号反查不可靠。
ALTER TABLE ops.sqm_trace_node ADD COLUMN material_code VARCHAR(64);
COMMENT ON COLUMN ops.sqm_trace_node.material_code IS '物料号(与 batch_no 组成物料条码); 按物料号反查构成去向/召回扩展时使用';
CREATE INDEX idx_trace_material ON ops.sqm_trace_node(org_id, material_code);
