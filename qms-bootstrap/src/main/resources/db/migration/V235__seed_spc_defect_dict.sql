-- 新增 SPC 判异不良字典项,供 SPC 告警一键发起8D时登记缺陷记录(source=SPC报警)引用。
-- 语义:SPC 控制图判异(超界/趋势/链)产生的过程统计异常,区别于外观/焊接等物理不良。
-- 幂等:org_id=NULL 全局可见(仿 MES 导入不良),按 code 去重,避免重复 seed。
INSERT INTO ops.ncm_defect_dict (id, org_id, code, name, category, level, status, reference_count, created_at, created_by, updated_at, updated_by, is_deleted, version)
SELECT '019feae0-0000-0000-0000-0000000000a1', NULL, 'SPC', 'SPC判异', '过程统计', '一般', '启用', 0, now(), NULL, now(), NULL, false, 0
WHERE NOT EXISTS (SELECT 1 FROM ops.ncm_defect_dict WHERE code = 'SPC' AND is_deleted = false);

-- 新增 来料异常 不良字典项,供来料异常单发起 8D/CAPA 时登记缺陷记录(source=SQM异常)引用。
-- 语义:供应商来料检验/使用过程中发现的不良(退货/特采/挑选使用/报废),区别于 SPC 过程统计异常。
INSERT INTO ops.ncm_defect_dict (id, org_id, code, name, category, level, status, reference_count, created_at, created_by, updated_at, updated_by, is_deleted, version)
SELECT '019feae0-0000-0000-0000-0000000000a2', NULL, 'SQM', '来料异常', '来料', '一般', '启用', 0, now(), NULL, now(), NULL, false, 0
WHERE NOT EXISTS (SELECT 1 FROM ops.ncm_defect_dict WHERE code = 'SQM' AND is_deleted = false);
