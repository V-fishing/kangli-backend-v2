-- 首件派生 SPC 参数带入来源 FIA 任务的单号/批号,供采集页自动填充
-- 幂等:列不存在时才添加(PostgreSQL)
ALTER TABLE ops.spc_param
  ADD COLUMN IF NOT EXISTS src_wo_no VARCHAR(64);

ALTER TABLE ops.spc_param
  ADD COLUMN IF NOT EXISTS src_batch_no VARCHAR(64);
