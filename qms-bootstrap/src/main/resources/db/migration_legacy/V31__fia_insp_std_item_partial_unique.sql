-- 编辑标准库时改为软删除旧项再插入新项, 新旧项 seq 相同会与原 UNIQUE(std_id, seq) 冲突。
-- 改为「仅对未删除行生效」的部分唯一索引, 已软删除的旧行不参与唯一约束。
ALTER TABLE ops.fia_insp_std_item DROP CONSTRAINT IF EXISTS fia_insp_std_item_std_id_seq_key;
DROP INDEX IF EXISTS fia_insp_std_item_std_id_seq_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_fia_insp_std_item_std_seq
    ON ops.fia_insp_std_item (std_id, seq) WHERE is_deleted = false;
