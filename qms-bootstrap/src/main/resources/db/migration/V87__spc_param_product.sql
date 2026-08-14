-- V87: SPC 参数↔产品 多对多关联(参数可绑定多个产品/件号, kind 区分物料/成品)
CREATE TABLE IF NOT EXISTS ops.spc_param_product (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  param_id      UUID NOT NULL REFERENCES ops.spc_param(id),
  product_name  VARCHAR(128) NOT NULL,
  part_no       VARCHAR(64),
  kind          VARCHAR(16) NOT NULL DEFAULT 'product',  -- material(物料/来料首件) / product(成品/产线首件)
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.spc_param_product IS 'SPC 参数-产品关联(多对多, kind 区分物料/成品)';
COMMENT ON COLUMN ops.spc_param_product.kind IS 'material=物料(来料首件) / product=成品(产线首件)';

CREATE INDEX IF NOT EXISTS idx_spc_pp_param ON ops.spc_param_product(param_id);
CREATE INDEX IF NOT EXISTS idx_spc_pp_org ON ops.spc_param_product(org_id);
-- 同一参数下(产品名+件号+类型)唯一;软删除行不计入,允许删除后重建
CREATE UNIQUE INDEX IF NOT EXISTS uq_spc_pp_param_prod
  ON ops.spc_param_product(param_id, product_name, COALESCE(part_no, ''), kind) WHERE is_deleted = false;
