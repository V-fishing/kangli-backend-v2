-- ============================================================
-- V192 修正 tlm_calib_plan 字符列宽度: owner_id/created_by/updated_by
-- 由 varchar(32) 改为 varchar(36), 容纳标准 UUID 字符串(36 字符),
-- 避免 MyBatis-Plus 自动填充 updated_by 时报 value too long。
-- ============================================================

ALTER TABLE ops.tlm_calib_plan
  ALTER COLUMN owner_id    TYPE varchar(36),
  ALTER COLUMN created_by  TYPE varchar(36),
  ALTER COLUMN updated_by  TYPE varchar(36);
