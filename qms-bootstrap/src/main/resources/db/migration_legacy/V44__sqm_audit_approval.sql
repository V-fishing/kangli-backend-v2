-- V44:审核会签表(质量/采购/研发并行 + 质量一票否决),与变更会签同构,外键指向审核计划。
CREATE TABLE ops.sqm_audit_approval (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  audit_id      UUID NOT NULL REFERENCES ops.sqm_audit_plan(id),
  approval_role VARCHAR(16) NOT NULL,
  role_label    VARCHAR(32) NOT NULL,
  status        VARCHAR(16) NOT NULL DEFAULT 'pending',
  operator      VARCHAR(64),
  operate_date  TIMESTAMPTZ,
  opinion       TEXT,
  has_veto      BOOLEAN NOT NULL DEFAULT false,
  seq_order     INT NOT NULL,
  UNIQUE(audit_id, approval_role)
);
COMMENT ON TABLE ops.sqm_audit_approval IS '审核会签(质量/采购/研发并行 + 质量一票否决)';
