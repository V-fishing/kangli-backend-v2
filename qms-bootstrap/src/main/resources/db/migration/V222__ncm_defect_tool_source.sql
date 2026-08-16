-- ============================================================
-- V222 工装异常/维修发起不良 · 不良记录反查工装(需求 2.5.x 闭环)。
-- 工装异常/维修页可直接"发起不良", 在 NCM 不良列表中生成一条 source='工装' 的记录,
-- 并回填来源工装 tool_id / tool_no, 使不良记录可反查工装。
-- 1) ncm_defect_record 增加 tool_id / tool_no 两列。
-- 全部幂等, 可重复执行。
-- ============================================================

ALTER TABLE ops.ncm_defect_record
  ADD COLUMN IF NOT EXISTS tool_id UUID,
  ADD COLUMN IF NOT EXISTS tool_no VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_ncm_defect_tool
  ON ops.ncm_defect_record(tool_id) WHERE is_deleted = false AND tool_id IS NOT NULL;
