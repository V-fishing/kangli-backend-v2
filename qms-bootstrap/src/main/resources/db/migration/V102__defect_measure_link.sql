-- 不良记录关联 8D/CAPA/CA 措施单号(发起时回写,用于列表禁止重复发起+状态回流)
ALTER TABLE ops.ncm_defect_record ADD COLUMN IF NOT EXISTS d8_no VARCHAR(50);
ALTER TABLE ops.ncm_defect_record ADD COLUMN IF NOT EXISTS capa_no VARCHAR(50);
ALTER TABLE ops.ncm_defect_record ADD COLUMN IF NOT EXISTS ca_no VARCHAR(50);
