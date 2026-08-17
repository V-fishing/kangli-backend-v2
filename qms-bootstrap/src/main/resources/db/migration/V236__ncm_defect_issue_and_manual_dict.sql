-- 第三步:人工建 8D 统一整改源头改造配套迁移。
-- 1) ncm_defect_record 增加 issue 列(问题主题),供从缺陷记录发起 8D 时保留原始问题主题,
--    避免 8D issue 退化为 "不良:DF-xxx" 而丢失人工填写的主题。可空,向后兼容 SPC/SQM 源头。
ALTER TABLE ops.ncm_defect_record ADD COLUMN IF NOT EXISTS issue TEXT;

-- 2) 新增 "NCM" 人工发起不良 字典项,供人工建 8D 时登记的缺陷记录(source=人工)引用。
--    语义:质量人员手动发起的 8D/CAPA/CA 整改(无上游自动触发事件),区别于 SPC判异/来料异常。
--    幂等:org_id=NULL 全局可见,按 code 去重。
INSERT INTO ops.ncm_defect_dict (id, org_id, code, name, category, level, status, reference_count, created_at, created_by, updated_at, updated_by, is_deleted, version)
SELECT '019feae0-0000-0000-0000-0000000000a3', NULL, 'NCM', '人工发起不良', '其他', '一般', '启用', 0, now(), NULL, now(), NULL, false, 0
WHERE NOT EXISTS (SELECT 1 FROM ops.ncm_defect_dict WHERE code = 'NCM' AND is_deleted = false);
