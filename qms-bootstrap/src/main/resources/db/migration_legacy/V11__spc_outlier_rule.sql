-- V11: spc_subgroup 加 outlier_rule 列(判异规则编号)
ALTER TABLE ops.spc_subgroup ADD COLUMN outlier_rule VARCHAR(32);
COMMENT ON COLUMN ops.spc_subgroup.outlier_rule IS '命中判异规则编号(①-⑧)';
