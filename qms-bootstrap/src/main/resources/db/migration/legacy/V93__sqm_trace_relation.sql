-- V93: 新建统一追溯关系表 ops.sqm_trace_relation
-- 方案 B: 以三张源表为节点权威(material_inspection / finished_goods_inspection / critical_material_binding),
--        关系表只存"条码 <-> 条码"的边(parent_barcode, child_barcode, relation_type), 不再造任何加工/占位表。
--        追溯树查询时沿本表递归条码, 再回查源表取节点详情(物料名/批次/检验结果等)。
--
-- relation_type 枚举:
--   incoming->keypart  来料条码 -> 关键件条码 (material_barcode 在来料表命中)
--   keypart->finished  关键件条码 -> 成品条码  (绑定表 product_barcode 命中成品)
--   keypart->semi      关键件条码 -> 半成品条码 (绑定表 product_barcode 命中半成品)
--   semi->finished     半成品条码 -> 成品条码  (绑定表 product_barcode 既有半成品又有成品, 经 work_order_no 桥)
--
-- 唯一约束避免重复边; parent/child 建索引提升递归查询效率。

CREATE TABLE IF NOT EXISTS ops.sqm_trace_relation (
    id              BIGSERIAL PRIMARY KEY,
    org_id          UUID        NOT NULL,
    parent_barcode  TEXT        NOT NULL,
    child_barcode   TEXT        NOT NULL,
    relation_type   VARCHAR(32) NOT NULL,
    is_deleted      CHAR(1)     NOT NULL DEFAULT '0',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_sqm_trace_relation_edge
        UNIQUE (org_id, parent_barcode, child_barcode, relation_type)
);

CREATE INDEX IF NOT EXISTS idx_sqm_trace_relation_parent
    ON ops.sqm_trace_relation (org_id, parent_barcode) WHERE is_deleted = '0';
CREATE INDEX IF NOT EXISTS idx_sqm_trace_relation_child
    ON ops.sqm_trace_relation (org_id, child_barcode) WHERE is_deleted = '0';
CREATE INDEX IF NOT EXISTS idx_sqm_trace_relation_type
    ON ops.sqm_trace_relation (org_id, relation_type) WHERE is_deleted = '0';

COMMENT ON TABLE  ops.sqm_trace_relation IS '统一追溯关系表: 仅存条码之间的边, 节点详情取自三张源表';
COMMENT ON COLUMN ops.sqm_trace_relation.parent_barcode IS '父节点业务条码(来料/关键件/半成品条码)';
COMMENT ON COLUMN ops.sqm_trace_relation.child_barcode  IS '子节点业务条码(关键件/成品/半成品条码)';
COMMENT ON COLUMN ops.sqm_trace_relation.relation_type  IS 'incoming->keypart / keypart->finished / keypart->semi / semi->finished';
