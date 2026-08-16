-- 方案B: 引入组成关系表 sqm_trace_link, 使追溯层级支持多对多(一个节点可被多个父引用)。
-- 原 sqm_trace_node.parent_node_id 仍保留为"主父"便于兼容, 但所有树查询改为沿 link 遍历。
CREATE TABLE IF NOT EXISTS ops.sqm_trace_link (
    id              BIGSERIAL PRIMARY KEY,
    org_id          UUID,
    parent_node_id  UUID NOT NULL,
    child_node_id   UUID NOT NULL,
    link_type       VARCHAR(32),          -- compose(组成) / shipment(客户出货), 当前统一用 compose
    sort_order      INT  DEFAULT 0,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uk_trace_link UNIQUE (parent_node_id, child_node_id)
);

CREATE INDEX IF NOT EXISTS idx_trace_link_parent ON ops.sqm_trace_link (parent_node_id);
CREATE INDEX IF NOT EXISTS idx_trace_link_child  ON ops.sqm_trace_link (child_node_id);

-- 回填: 将历史 parent_node_id 关系同步为 link(已存在的去重, 不重复插入)
INSERT INTO ops.sqm_trace_link (org_id, parent_node_id, child_node_id, link_type, created_at, created_by)
SELECT n.org_id, n.parent_node_id, n.id, 'compose', n.created_at, n.created_by
FROM ops.sqm_trace_node n
WHERE n.parent_node_id IS NOT NULL
  AND n.is_deleted = FALSE
  AND NOT EXISTS (
        SELECT 1 FROM ops.sqm_trace_link l
        WHERE l.parent_node_id = n.parent_node_id AND l.child_node_id = n.id
    );
