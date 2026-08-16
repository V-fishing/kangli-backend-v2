-- 为 spc_subgroup 增加 S 图标准差与计数型(P/NP/C/U)字段,支持按标准库 chartTypes 动态渲染多类控制图。
-- 幂等:仅 ADD COLUMN IF NOT EXISTS,已存在则跳过(历史数据这些列为 NULL)。

ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS std_dev numeric(18,6);
ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS nonconforming integer;
ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS inspect_n integer;
ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS defect_count integer;
