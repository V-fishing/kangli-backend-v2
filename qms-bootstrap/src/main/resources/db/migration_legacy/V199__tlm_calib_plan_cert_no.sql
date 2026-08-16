-- V199 校准计划单补充证书编号列 cert_no: 校准录入时由用户填写校准证书/报告编号,
-- 与系统自动生成的校准记录流水号 calib_no 区分, 用于计量履历审计追溯(需求3 计量合规)。
ALTER TABLE ops.tlm_calib_plan ADD COLUMN IF NOT EXISTS cert_no VARCHAR(64);
