-- ============================================================
-- V191 为已创建的 tlm_calib_plan 补 BaseEntity 审计/乐观锁列。
-- V188 已应用但缺少 created_by/updated_by/version, 导致 insert/update 失败。
-- ============================================================

ALTER TABLE ops.tlm_calib_plan
  ADD COLUMN IF NOT EXISTS created_by varchar(32),
  ADD COLUMN IF NOT EXISTS updated_by varchar(32),
  ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 0;
