-- V163: 工装-产品关联表(一工具多产品预留, 当前甲方数据为空)。
CREATE TABLE IF NOT EXISTS ops.tlm_tool_product (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id     UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(128),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  UUID,
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  UNIQUE(org_id, tool_id, product_code)
);
COMMENT ON TABLE ops.tlm_tool_product IS '工装与产品的关联(一工具多产品)';
CREATE INDEX IF NOT EXISTS idx_tlm_tool_product_org_tool ON ops.tlm_tool_product (org_id, tool_id);
