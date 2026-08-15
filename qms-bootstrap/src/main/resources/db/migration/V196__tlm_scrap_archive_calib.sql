-- V196 报废归档表补充计量履历列: 计量器具(GAUGE)报废归档时留存最后校准日期与有效期,
-- 使计量器具退出生命周期时仍保留其计量状态痕迹(需求3 计量报废归档)。
ALTER TABLE ops.tlm_scrap_archive ADD COLUMN IF NOT EXISTS last_calib_date DATE;
ALTER TABLE ops.tlm_scrap_archive ADD COLUMN IF NOT EXISTS calib_due_date  DATE;
ALTER TABLE ops.tlm_scrap_archive ADD COLUMN IF NOT EXISTS tool_category   VARCHAR(16);
