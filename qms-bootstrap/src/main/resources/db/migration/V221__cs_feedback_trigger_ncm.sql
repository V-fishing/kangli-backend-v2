-- ============================================================
-- V221 售后管理 · 客户反馈直接触发 8D/CAPA/CA 纠正措施(需求 2.4.2.5 闭环升级)。
-- 原 link-ncm 仅手动填写 NCM ID, 未实际创建纠正措施; 现改为从反馈页直接选择触发
-- 8D / CAPA / CA, 后端实际创建对应记录并回填来源, 实现真正的闭环联动。
-- 1) cs_feedback 增加 related_8d_id / related_capa_id / related_ca_id 三关联字段。
-- 2) ncm_corrective_action 增加 source_ref_id / source_type 来源追溯字段(与 CAPA 对齐)。
-- 全部幂等, 可重复执行。
-- ============================================================

ALTER TABLE ops.cs_feedback
  ADD COLUMN IF NOT EXISTS related_8d_id UUID,
  ADD COLUMN IF NOT EXISTS related_capa_id UUID,
  ADD COLUMN IF NOT EXISTS related_ca_id UUID;

CREATE INDEX IF NOT EXISTS idx_cs_feedback_8d
  ON ops.cs_feedback(related_8d_id) WHERE is_deleted = false AND related_8d_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cs_feedback_capa
  ON ops.cs_feedback(related_capa_id) WHERE is_deleted = false AND related_capa_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cs_feedback_ca
  ON ops.cs_feedback(related_ca_id) WHERE is_deleted = false AND related_ca_id IS NOT NULL;

ALTER TABLE ops.ncm_corrective_action
  ADD COLUMN IF NOT EXISTS source_ref_id VARCHAR(64),
  ADD COLUMN IF NOT EXISTS source_type VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_ncm_ca_src
  ON ops.ncm_corrective_action(source_ref_id) WHERE source_ref_id IS NOT NULL;
