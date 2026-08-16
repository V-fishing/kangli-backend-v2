-- CAPA 通用来源单据:内审不符合项 / 不良趋势异常记录 等未单独建字段的触发来源。
-- 旧数据(8D/来料异常)仍走 d8_id / abnormal_id,新增 source_ref_id + source_type 兜底触达跳转。
ALTER TABLE ops.qms_capa ADD COLUMN IF NOT EXISTS source_ref_id VARCHAR(64);
ALTER TABLE ops.qms_capa ADD COLUMN IF NOT EXISTS source_type VARCHAR(32);
COMMENT ON COLUMN ops.qms_capa.source_ref_id IS '通用来源单据 ID(内审不符合项/不良记录等)';
COMMENT ON COLUMN ops.qms_capa.source_type IS '来源类型(审核不符合项/不良记录/8D/来料异常)';
CREATE INDEX IF NOT EXISTS idx_capa_source ON ops.qms_capa(org_id, source_type, source_ref_id);
