-- 标准检测项模板增加逻辑删除字段:
-- 编辑/覆盖标准库时改为软删除旧项而非物理删除, 避免被首件检验录入表(fia_insp_item)外键引用导致删除失败(系统异常)
ALTER TABLE ops.fia_insp_std_item ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;
CREATE INDEX IF NOT EXISTS idx_fia_insp_std_item_std_id ON ops.fia_insp_std_item (std_id);
