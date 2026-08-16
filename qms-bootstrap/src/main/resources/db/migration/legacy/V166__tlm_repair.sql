-- V166: 工装维修工单。
-- 状态: PENDING 待维修 / REPAIRING 维修中 / DONE 已完成 / VERIFYING 待验证 / VERIFIED 验证通过。
-- verify_task_id 关联 FIA 首件任务单号(维修完成触发首件验证)。
CREATE TABLE IF NOT EXISTS ops.tlm_repair (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id       UUID NOT NULL REFERENCES ops.tlm_tooling(id) ON DELETE CASCADE,
  repair_no     VARCHAR(32) NOT NULL UNIQUE,
  fault_desc    VARCHAR(512),
  measure       VARCHAR(512),
  status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  approver_id   UUID REFERENCES ops.sys_user(id),  -- 维修审批/负责人(可选, 进个人任务中心)
  verify_task_id VARCHAR(64),                      -- 关联 FIA 首件任务单号
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by    UUID,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_by    UUID,
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.tlm_repair IS '工装维修工单';
CREATE INDEX IF NOT EXISTS idx_tlm_repair_org_tool ON ops.tlm_repair (org_id, tool_id);
CREATE INDEX IF NOT EXISTS idx_tlm_repair_org_status ON ops.tlm_repair (org_id, status);
CREATE INDEX IF NOT EXISTS idx_tlm_repair_org_approver ON ops.tlm_repair (org_id, approver_id);
