-- 追溯树不再强制以来料批次(root_lot_id)为根。
-- 1) root_lot_id 改为可空并去掉外键(来料批次模块移除后, 追溯树可由任意产出节点作为树根)。
-- 2) 新增 root_node_id: 自引用, 标识该节点所属树的树根节点, 便于按树根直接取整棵树。
-- 3) 新增 qualification_type: 节点资格类型(合格/资格直通/常规), 替代原"来料批次级 FIA 状态"。

ALTER TABLE ops.sqm_trace_node ALTER COLUMN root_lot_id DROP NOT NULL;
ALTER TABLE ops.sqm_trace_node DROP CONSTRAINT IF EXISTS sqm_trace_node_root_lot_id_fkey;
ALTER TABLE ops.sqm_trace_node ADD COLUMN root_node_id UUID REFERENCES ops.sqm_trace_node(id);
ALTER TABLE ops.sqm_trace_node ADD COLUMN qualification_type VARCHAR(16);
CREATE INDEX IF NOT EXISTS idx_sqm_trace_root_node ON ops.sqm_trace_node(org_id, root_node_id);
