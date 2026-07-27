-- V60: 补齐 spc_measurement / spc_subgroup / spc_param 审计列(created_at, created_by),
-- 与最新 V04__spc.sql 口径一致;解决早期基线库缺列导致的 Flyway 漂移。
-- 幂等: ADD COLUMN IF NOT EXISTS。

ALTER TABLE ops.spc_measurement ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT now();
ALTER TABLE ops.spc_measurement ADD COLUMN IF NOT EXISTS created_by uuid;

ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT now();
ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS created_by uuid;

ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT now();
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS created_by uuid;
