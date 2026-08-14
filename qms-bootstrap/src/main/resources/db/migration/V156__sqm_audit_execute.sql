-- 审核流程轨迹表:记录 计划→会签→执行→复核→归档 全节点留痕
-- 幂等:仅当表不存在时创建(已有 sqm_audit_checklist_item / sqm_audit_photo,此处不再建)

CREATE TABLE IF NOT EXISTS ops.sqm_audit_workflow_log (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID REFERENCES ops.sys_org(id),
  plan_id     UUID NOT NULL REFERENCES ops.sqm_audit_plan(id),
  node        VARCHAR(32) NOT NULL,
  action      VARCHAR(64),
  operator    VARCHAR(64),
  remark      TEXT,
  create_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sqm_audit_wf_plan ON ops.sqm_audit_workflow_log (plan_id, create_time);

COMMENT ON TABLE ops.sqm_audit_workflow_log IS '供应商审核流程轨迹(全节点留痕,供时间轴审计)';
