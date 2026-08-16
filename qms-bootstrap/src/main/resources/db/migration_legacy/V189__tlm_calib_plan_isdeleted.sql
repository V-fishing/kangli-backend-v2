-- ============================================================
-- V189 补 tlm_calib_plan.is_deleted 逻辑删除列。
-- V188 已应用但缺该列, MyBatis-Plus 全局逻辑删除(logic-delete-field=isDeleted)
-- 会追加 WHERE is_deleted=false, 缺列将导致 calib-plans 接口 500。幂等补充。
-- ============================================================

ALTER TABLE ops.tlm_calib_plan
  ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false;
