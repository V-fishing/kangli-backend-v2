-- V168: 工装-工单绑定表。绑定写入时触发 tlm_tooling.bind_count +1,
-- 达 design_life 上限自动 locked=true + 预警(见 TlmScanJob)。
CREATE TABLE IF NOT EXISTS ops.tlm_tool_wo_bind (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id     UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  wo_no       VARCHAR(32) NOT NULL,   -- 复用四元追溯键字符串
  bound_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  unbound_at  TIMESTAMPTZ,
  created_by  UUID,
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  UNIQUE(tool_id, wo_no)
);
COMMENT ON TABLE ops.tlm_tool_wo_bind IS '工装-工单绑定(寿命计数触发)';
CREATE INDEX IF NOT EXISTS idx_tlm_tool_wo_bind_org_tool ON ops.tlm_tool_wo_bind (org_id, tool_id);
