-- V165: 工装保养记录。
CREATE TABLE IF NOT EXISTS ops.tlm_maint_record (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  plan_id     UUID REFERENCES ops.tlm_maint_plan(id) ON DELETE SET NULL,
  tool_id     UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  maint_date  DATE NOT NULL,
  result      VARCHAR(256),
  responsible_id UUID REFERENCES ops.sys_user(id),
  attachment  VARCHAR(256),       -- FileController 上传路径(objectKey)
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  UUID,
  is_deleted  BOOLEAN NOT NULL DEFAULT false
);
COMMENT ON TABLE ops.tlm_maint_record IS '工装保养记录';
CREATE INDEX IF NOT EXISTS idx_tlm_maint_record_org_tool ON ops.tlm_maint_record (org_id, tool_id);
