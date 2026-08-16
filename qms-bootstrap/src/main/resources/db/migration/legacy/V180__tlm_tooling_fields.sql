-- ============================================================
-- V154 工装台账字段补齐(覆盖 CSV 工装夹具台账 / 监视和测量设备总表)
-- 新增列全部幂等(IF NOT EXISTS)，可重复执行。
-- 说明：
--   CSV 中「数量/验证周期/备注」无对应列，新增 quantity/verify_cycle/remark；
--   CSV 中「领用人/设备管理员/供应商(生产厂家)」为纯文本，新增 *_name 文本列承载
--   (与现有 precision_val/measure_point 等文本字段风格一致，避免强依赖用户/供应商 UUID 表)；
--   其余 CSV 字段(tool_type/material/calib_date/calib_cycle/maint_cycle/
--   owner_id/admin_id/supplier_id/inbound_date/cost 等)列已存在，仅前端补齐显示。
-- ============================================================

ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS quantity      INTEGER      DEFAULT 1;
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS verify_cycle  VARCHAR(32);
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS remark        VARCHAR(512);
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS owner_name    VARCHAR(64);
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS admin_name    VARCHAR(64);
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(128);

COMMENT ON COLUMN ops.tlm_tooling.quantity      IS '数量(工装夹具台账/设备总表)';
COMMENT ON COLUMN ops.tlm_tooling.verify_cycle  IS '验证周期(工装夹具台账,如 一年)';
COMMENT ON COLUMN ops.tlm_tooling.remark        IS '备注(工装夹具台账/设备总表)';
COMMENT ON COLUMN ops.tlm_tooling.owner_name    IS '领用人(设备总表,纯文本)';
COMMENT ON COLUMN ops.tlm_tooling.admin_name    IS '设备管理员(设备总表,纯文本)';
COMMENT ON COLUMN ops.tlm_tooling.supplier_name IS '供应商/生产厂家(设备总表,纯文本)';
