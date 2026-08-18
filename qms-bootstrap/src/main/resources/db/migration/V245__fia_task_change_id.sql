-- 首件检验任务新增 change_id：建立「首件 ↔ 物料变更单」直连绑定外键
-- 用于供应商物料变更 → 首件 FIA → SPC → 变更归档 业务闭环的门禁与追溯。
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS change_id uuid;

-- 变更单驱动的首件反查索引（close 门禁按 change_id 精确命中）
CREATE INDEX IF NOT EXISTS idx_fia_task_change_id ON ops.fia_task (change_id);
