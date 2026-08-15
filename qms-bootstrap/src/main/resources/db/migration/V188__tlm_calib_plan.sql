-- ============================================================
-- V188 TLM 计量校准计划单 tlm_calib_plan。
-- 计量器具(GAUGE)按校准周期, 由 TlmScanJob 到期自动生成计划单(P1)。
-- 字段: 计划周期(月)/计划到期日/状态(PENDING/DONE/OVERDUE)/负责人/来源(AUTO/MANUAL)。
-- ============================================================

CREATE TABLE IF NOT EXISTS ops.tlm_calib_plan (
  id              uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          uuid,
  tool_id         uuid NOT NULL,
  tool_no         varchar(64),
  tool_name       varchar(128),
  plan_cycle      int,                       -- 计划校验周期(月)
  plan_due_date   date,                      -- 计划到期日(应完成校准日)
  status          varchar(16) NOT NULL DEFAULT 'PENDING',  -- PENDING/DONE/OVERDUE
  owner_id        varchar(36),               -- 计量管理员(负责人)
  source          varchar(16) NOT NULL DEFAULT 'AUTO',     -- AUTO/MANUAL
  is_deleted      boolean NOT NULL DEFAULT false,
  created_at      timestamp DEFAULT now(),
  updated_at      timestamp DEFAULT now(),
  created_by      varchar(36),
  updated_by      varchar(36),
  version         int DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tlm_calib_plan_tool ON ops.tlm_calib_plan (tool_id);
CREATE INDEX IF NOT EXISTS idx_tlm_calib_plan_due  ON ops.tlm_calib_plan (plan_due_date);
CREATE INDEX IF NOT EXISTS idx_tlm_calib_plan_stat ON ops.tlm_calib_plan (status);
