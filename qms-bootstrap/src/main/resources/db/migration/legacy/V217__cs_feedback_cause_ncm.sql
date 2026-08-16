-- ============================================================
-- V217 售后管理 · 客户反馈低分诱因维度 + 质量改进联动。
-- 支撑需求 2.4.2.2(多维低分诱因分析) / 2.4.2.5(反馈与质量改进联动闭环)。
-- 全部幂等, 可重复执行。
-- ============================================================

ALTER TABLE ops.cs_feedback
  ADD COLUMN IF NOT EXISTS cause VARCHAR(32),            -- 低分诱因: RESPONSE_SLOW / REPAIR_INCOMPLETE / ATTITUDE / OTHER
  ADD COLUMN IF NOT EXISTS related_ncm_id UUID;          -- 联动 NCM 8D/CAPA 纠正措施 ID(质量改进闭环)

CREATE INDEX IF NOT EXISTS idx_cs_feedback_cause
  ON ops.cs_feedback(cause) WHERE is_deleted = false AND cause IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cs_feedback_ncm
  ON ops.cs_feedback(related_ncm_id) WHERE is_deleted = false AND related_ncm_id IS NOT NULL;
