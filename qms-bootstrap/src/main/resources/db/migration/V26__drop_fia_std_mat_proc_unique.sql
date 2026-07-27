-- V26: 移除 fia_insp_std 上 (org_id, material, proc_name) 的旧唯一索引
-- 该索引不含供应商维度,会阻止"同一物料、不同供应商各自一套标准",
-- 与"来料批次按 物料编码+供应商+工序 自动匹配"的模型冲突。
-- 唯一性现由 V25 的 idx_fia_insp_std_part_sup_proc (org_id, part_no, supplier_id, proc_name) 保证。

DROP INDEX IF EXISTS ops.uk_fia_std_mat_proc;
