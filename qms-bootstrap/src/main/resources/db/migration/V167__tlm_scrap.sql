-- V167: 工装报废单(接入统一审批中心)。
-- 状态: PENDING 待审批 / APPROVED 已通过 / REJECTED 已驳回 / DONE 已处置。
-- approver_id 指定审批人(仿 FIA 范式, 进审批中心 myPending)。
CREATE TABLE IF NOT EXISTS ops.tlm_scrap (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id     UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  scrap_no    VARCHAR(32) NOT NULL UNIQUE,
  scrap_method VARCHAR(16),    -- RECYCLE 回收 / DESTROY 销毁 / REPAIR 返修
  reason      VARCHAR(512),
  status      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  approver_id UUID REFERENCES ops.sys_user(id),  -- 指定报废审批人
  approval_id UUID,                           -- 关联审批中心记录
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  UUID,
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_by  UUID,
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  version     INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.tlm_scrap IS '工装报废单(接统一审批中心)';
CREATE INDEX IF NOT EXISTS idx_tlm_scrap_org_tool ON ops.tlm_scrap (org_id, tool_id);
CREATE INDEX IF NOT EXISTS idx_tlm_scrap_org_status ON ops.tlm_scrap (org_id, status);
CREATE INDEX IF NOT EXISTS idx_tlm_scrap_org_approver ON ops.tlm_scrap (org_id, approver_id);
