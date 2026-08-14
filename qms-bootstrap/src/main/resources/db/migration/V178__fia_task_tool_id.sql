-- V178: FIA 任务关联工装 —— 工装首件不合格时通过 tool_id 回写 tlm_tooling.locked=true
-- 不设 FK 约束，避免跨模块耦合
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS tool_id UUID;
COMMENT ON COLUMN ops.fia_task.tool_id IS '关联工装 ID(仅 source=TOOLING 时填入，用于不合格回写锁定)';