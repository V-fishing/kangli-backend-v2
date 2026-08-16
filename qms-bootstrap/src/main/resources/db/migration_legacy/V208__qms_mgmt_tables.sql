-- ============================================================
-- V208 体系管理(QMS-MGMT) 数据表: 质量目标 / 内审 / 不良事件。
-- 模块定位(用户确认): 体系运行监控, 数据以"体系自己维护的主数据 + 跨模块分析"为主。
--   1) 质量目标 qms_quality_goal: 目标定义(目标值)+ 实际填报值, 达成率=实际/目标(后端算)。
--   2) 内审计划 qms_internal_audit + 不符合项 qms_audit_nc(含整改字段)。
--   3) 不良事件 qms_adverse_event: 医疗器械不良事件(法规口径)。
-- 顾客反馈分析复用售后 CS 模块(cs_feedback), 此处不建表。
-- 全部幂等, org_id 外键 sys_org。
-- ============================================================

-- 1) 质量目标
CREATE TABLE IF NOT EXISTS ops.qms_quality_goal (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  goal_name     VARCHAR(120) NOT NULL,         -- 目标名称(如 出厂合格率/交付及时率)
  goal_type     VARCHAR(32) NOT NULL,          -- 目标分类(QUALITY/DELIVERY/SATISFACTION/COST/OTHER)
  period        VARCHAR(32) NOT NULL,          -- 统计周期(如 2026Q1 / 2026年度)
  target_value  NUMERIC(12,4) NOT NULL,        -- 目标值
  actual_value  NUMERIC(12,4) NOT NULL DEFAULT 0,  -- 实际填报值
  unit          VARCHAR(16) NOT NULL DEFAULT '%',   -- 单位(%/天/分/件)
  owner         VARCHAR(80),                   -- 责任人
  deadline      TIMESTAMP,                     -- 目标截止时间
  remark        VARCHAR(400),                  -- 备注

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_qms_goal_org_period
  ON ops.qms_quality_goal(org_id, period) WHERE is_deleted = false;

-- 2) 内审计划
CREATE TABLE IF NOT EXISTS ops.qms_internal_audit (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  audit_no      VARCHAR(32) NOT NULL,          -- 内审编号(业务唯一)
  audit_name    VARCHAR(160) NOT NULL,         -- 内审名称/主题
  audit_scope   VARCHAR(240),                  -- 审核范围
  plan_date     TIMESTAMP,                     -- 计划审核时间
  auditor       VARCHAR(80),                   -- 审核员
  status        VARCHAR(16) NOT NULL DEFAULT 'PLANNED',  -- PLANNED/ONGOING/DONE/CLOSED
  remark        VARCHAR(400),

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_qms_audit_no
  ON ops.qms_internal_audit(org_id, audit_no) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_qms_audit_org_status
  ON ops.qms_internal_audit(org_id, status) WHERE is_deleted = false;

-- 3) 内审不符合项(含整改)
CREATE TABLE IF NOT EXISTS ops.qms_audit_nc (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  audit_id      UUID REFERENCES ops.qms_internal_audit(id),  -- 关联内审计划
  nc_no         VARCHAR(32) NOT NULL,          -- 不符合项编号
  nc_desc       TEXT NOT NULL,                 -- 不符合描述
  clause        VARCHAR(80),                   -- 对应条款/标准
  severity      VARCHAR(16) NOT NULL DEFAULT 'MINOR',  -- MAJOR/MINOR/OBSERVATION
  status        VARCHAR(16) NOT NULL DEFAULT 'OPEN',  -- OPEN/IN_PROGRESS/CLOSED
  owner         VARCHAR(80),                   -- 责任部门/人
  due_date      TIMESTAMP,                     -- 整改期限
  corrective    TEXT,                          -- 纠正/纠正措施
  verify_result VARCHAR(400),                  -- 验证结果
  closed_at     TIMESTAMP,                     -- 关闭时间

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_qms_nc_audit
  ON ops.qms_audit_nc(audit_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_qms_nc_org_status
  ON ops.qms_audit_nc(org_id, status) WHERE is_deleted = false;

-- 4) 不良事件
CREATE TABLE IF NOT EXISTS ops.qms_adverse_event (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),

  event_no      VARCHAR(32) NOT NULL,          -- 事件编号
  event_type    VARCHAR(32) NOT NULL,          -- 事件类型(投诉/器械故障/伤害/召回/其他)
  occur_stage   VARCHAR(80),                   -- 发生环节(生产/流通/使用/其他)
  severity      VARCHAR(16) NOT NULL DEFAULT 'GENERAL',  -- GENERAL/SERIOUS/CRITICAL
  occur_at      TIMESTAMP,                     -- 发生时间
  report_at     TIMESTAMP,                     -- 上报时间
  root_cause    TEXT,                          -- 根源分析
  handle_desc   TEXT,                          -- 处理措施
  handle_timeliness VARCHAR(16),               -- 处理时效(及时/逾期)
  status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',  -- PENDING/HANDLING/DONE
  remark        VARCHAR(400),

  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now(),
  created_by    VARCHAR(64),
  updated_by    VARCHAR(64),
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_qms_adverse_no
  ON ops.qms_adverse_event(org_id, event_no) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_qms_adverse_org_type
  ON ops.qms_adverse_event(org_id, event_type) WHERE is_deleted = false;
