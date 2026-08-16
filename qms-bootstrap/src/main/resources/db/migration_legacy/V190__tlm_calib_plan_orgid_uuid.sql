-- ============================================================
-- V190 修正 tlm_calib_plan.org_id 类型: V188 误用 varchar(32),
-- 标准 UUID 为 36 字符, 插入会报 value too long。
-- 幂等改为 uuid 类型(与 tlm_tooling.org_id 一致)。
-- ============================================================

ALTER TABLE ops.tlm_calib_plan
  ALTER COLUMN org_id TYPE uuid USING org_id::uuid;
