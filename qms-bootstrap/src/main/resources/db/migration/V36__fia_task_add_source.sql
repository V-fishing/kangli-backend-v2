-- V36__fia_task_add_source.sql
-- FIA 拆分子域: 产线首件(FACTORY) vs 供应商来料首件(SUPPLIER)

ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS source VARCHAR(16) DEFAULT 'FACTORY';

COMMENT ON COLUMN ops.fia_task.source IS '来源: FACTORY(产线首件) / SUPPLIER(供应商来料首件)';

-- 回填历史数据: 有 supplier_id 且无 wo_no → SUPPLIER, 其余 → FACTORY
UPDATE ops.fia_task SET source = 'SUPPLIER'
WHERE source = 'FACTORY'
  AND supplier_id IS NOT NULL
  AND (wo_no IS NULL OR wo_no = '');

CREATE INDEX IF NOT EXISTS idx_fia_task_source ON ops.fia_task(source) WHERE is_deleted = false;
