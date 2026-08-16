-- V90: spc_subgroup 新增 task_id / product_code 列,支持从 FIA 任务关联子组
-- shift 列标记废弃(不再使用,保留兼容历史数据)
ALTER TABLE ops.spc_subgroup
  ADD COLUMN IF NOT EXISTS task_id VARCHAR(36),
  ADD COLUMN IF NOT EXISTS product_code VARCHAR(100);

COMMENT ON COLUMN ops.spc_subgroup.task_id IS '关联FIA任务ID(fia_task.id)';
COMMENT ON COLUMN ops.spc_subgroup.product_code IS '产品料号(来自FIA任务)';
COMMENT ON COLUMN ops.spc_subgroup.shift IS '班次(已废弃,保留兼容历史数据)';
