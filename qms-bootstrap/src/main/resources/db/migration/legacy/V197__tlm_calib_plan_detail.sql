-- V197 校准计划单补充校准结果明细列: 录入后回填, 长期留存计量履历(供追溯反查)。
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS calib_no       VARCHAR(32);
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS calib_date     DATE;
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS calib_due_date DATE;
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS calib_cycle    INT;
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS upper_limit    VARCHAR(32);
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS result         VARCHAR(16);
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS remark         VARCHAR(512);
