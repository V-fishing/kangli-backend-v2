-- V20: 修复 qms_8d_report.source_ref_id 列宽不足(原 32 不能容纳 36 位 UUID)
ALTER TABLE ops.qms_8d_report
  ALTER COLUMN source_ref_id TYPE VARCHAR(36);
