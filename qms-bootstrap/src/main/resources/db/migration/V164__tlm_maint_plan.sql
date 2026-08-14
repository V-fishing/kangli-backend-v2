-- V164: 工装保养计划。
CREATE TABLE IF NOT EXISTS ops.tlm_maint_plan (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id     UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  plan_no     VARCHAR(32) NOT NULL UNIQUE,
  cycle_type  VARCHAR(16),        -- WEEK 周 / MONTH 月 / YEAR 年
  next_date   DATE NOT NULL,      -- 下次保养日期
  responsible_id UUID REFERENCES ops.sys_user(id),
  remark      VARCHAR(256),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  UUID,
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  version     INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.tlm_maint_plan IS '工装保养计划';
CREATE INDEX IF NOT EXISTS idx_tlm_maint_plan_org_tool ON ops.tlm_maint_plan (org_id, tool_id);
CREATE INDEX IF NOT EXISTS idx_tlm_maint_plan_org_next ON ops.tlm_maint_plan (org_id, next_date);
