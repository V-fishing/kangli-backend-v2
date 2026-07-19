-- V02: 全局/跨模块共享表(8D / CAPA / FMEA / 待办 / 通知 / 审计 / 附件)
-- qms_esign_log 已在 V01 建。本脚本建其余 15 张全局表。
-- org_id 规则:业务记录/子表/操作型 NOT NULL;配置/主数据/审计日志 NULLABLE(全局时为 null)。
-- BIGINT 外键/created_by/updated_by/用户引用 -> UUID;业务单号(*_no)保持全局 UNIQUE。

-- 8D 整改报告(NCM/SQM/ASM/QSM 共用)
CREATE TABLE ops.qms_8d_report (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  d8_no          VARCHAR(32) NOT NULL UNIQUE,
  source         VARCHAR(16) NOT NULL,
  source_ref_id  VARCHAR(32),
  issue          TEXT NOT NULL,
  severity       VARCHAR(8) NOT NULL,
  current_stage  VARCHAR(4) NOT NULL DEFAULT 'D1',
  status         VARCHAR(16) NOT NULL DEFAULT '进行中',
  flow_type      VARCHAR(16),
  team           JSONB,
  capa_triggered BOOLEAN NOT NULL DEFAULT false,
  close_date     DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_8d_source ON ops.qms_8d_report(org_id, source, source_ref_id);
COMMENT ON TABLE ops.qms_8d_report IS '8D 整改报告(跨模块共享)';

-- 8D 阶段明细(D1~D8 内容/审批/SLA/证据)
CREATE TABLE ops.qms_8d_stage_detail (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID NOT NULL REFERENCES ops.sys_org(id),
  d8_id            UUID NOT NULL REFERENCES ops.qms_8d_report(id),
  stage_code       VARCHAR(4) NOT NULL,
  content          TEXT,
  team_members     JSONB,
  owner            VARCHAR(64),
  plan_date        DATE,
  actual_cost_hours NUMERIC(10,2),
  approval_status  VARCHAR(16),
  approved_by      UUID,
  approved_at      TIMESTAMPTZ,
  approval_comment TEXT,
  esign_id         UUID,
  evidence_files   JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0,
  UNIQUE(d8_id, stage_code)
);
COMMENT ON TABLE ops.qms_8d_stage_detail IS '8D 各阶段内容/审批/SLA/证据明细';

-- 8D 阶段历史(时间线全量留痕)
CREATE TABLE ops.qms_8d_stage_history (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  d8_id        UUID NOT NULL REFERENCES ops.qms_8d_report(id),
  stage_code   VARCHAR(4) NOT NULL,
  operation    VARCHAR(64) NOT NULL,
  operator     VARCHAR(64) NOT NULL,
  operated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  cost_hours   VARCHAR(16),
  note         TEXT
);
CREATE INDEX idx_8d_hist ON ops.qms_8d_stage_history(org_id, d8_id);
COMMENT ON TABLE ops.qms_8d_stage_history IS '8D 阶段变更历史(全量留痕)';

-- 8D 鱼骨图(5M1E 根因)
CREATE TABLE ops.qms_8d_fishbone (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  d8_id       UUID NOT NULL REFERENCES ops.qms_8d_report(id),
  problem     TEXT NOT NULL,
  category    VARCHAR(4) NOT NULL,
  cause_text  TEXT NOT NULL,
  sort_order  SMALLINT
);
COMMENT ON TABLE ops.qms_8d_fishbone IS '8D 鱼骨图 5M1E 根因';

-- 8D 阶段配置(主数据,全局)
CREATE TABLE ops.qms_8d_stage_config (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID REFERENCES ops.sys_org(id),
  stage_code    VARCHAR(4) NOT NULL UNIQUE,
  stage_name    VARCHAR(32) NOT NULL,
  sort_order    SMALLINT NOT NULL,
  need_approval BOOLEAN NOT NULL DEFAULT false,
  sla_duration  INTERVAL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.qms_8d_stage_config IS '8D 阶段配置(主数据)';

-- CAPA 纠正与预防措施
CREATE TABLE ops.qms_capa (
  id                UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id            UUID NOT NULL REFERENCES ops.sys_org(id),
  capa_no           VARCHAR(32) NOT NULL UNIQUE,
  d8_id             UUID REFERENCES ops.qms_8d_report(id),
  abnormal_id       VARCHAR(64),
  issue             TEXT NOT NULL,
  trigger_stage     VARCHAR(4),
  trigger_type      VARCHAR(16) NOT NULL,
  trigger_condition VARCHAR(32),
  capa_type         VARCHAR(16) NOT NULL,
  rootcause         TEXT,
  action_plan       TEXT,
  owner             VARCHAR(64) NOT NULL,
  due_date          DATE NOT NULL,
  progress          SMALLINT NOT NULL DEFAULT 0,
  status            VARCHAR(16) NOT NULL DEFAULT '待启动',
  esign_id          UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_capa_d8 ON ops.qms_capa(org_id, d8_id);
COMMENT ON TABLE ops.qms_capa IS 'CAPA 纠正与预防措施(跨模块共享)';

-- CAPA 措施明细
CREATE TABLE ops.qms_capa_action (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  capa_id     UUID NOT NULL REFERENCES ops.qms_capa(id),
  seq         SMALLINT NOT NULL,
  action_text TEXT NOT NULL,
  done        BOOLEAN NOT NULL DEFAULT false,
  complete_date DATE,
  UNIQUE(capa_id, seq)
);
COMMENT ON TABLE ops.qms_capa_action IS 'CAPA 措施明细';

-- CAPA 触发规则(配置)
CREATE TABLE ops.qms_capa_trigger_rule (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID REFERENCES ops.sys_org(id),
  condition     VARCHAR(32) NOT NULL,
  auto_or_manual VARCHAR(16) NOT NULL,
  enabled       BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.qms_capa_trigger_rule IS 'CAPA 触发规则(配置)';

-- FMEA 高风险项(NCM/SQM 共用)
CREATE TABLE ops.qms_fmea_risk (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  risk_no        VARCHAR(32) NOT NULL UNIQUE,
  fmea_type      VARCHAR(8) NOT NULL,
  product        VARCHAR(128) NOT NULL,
  process        VARCHAR(128) NOT NULL,
  failure_mode   VARCHAR(255) NOT NULL,
  severity_s     SMALLINT NOT NULL CHECK (severity_s BETWEEN 1 AND 10),
  occurrence_o   SMALLINT NOT NULL CHECK (occurrence_o BETWEEN 1 AND 10),
  detection_d    SMALLINT NOT NULL CHECK (detection_d BETWEEN 1 AND 10),
  rpn            SMALLINT NOT NULL,
  risk_level     VARCHAR(8) NOT NULL,
  high_risk_flag BOOLEAN NOT NULL DEFAULT false,
  status         VARCHAR(16) NOT NULL DEFAULT '待闭环',
  action         TEXT,
  owner          VARCHAR(64),
  target_date    DATE,
  evidence       TEXT,
  close_date     DATE,
  change_order_id VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_fmea_rpn ON ops.qms_fmea_risk(org_id, rpn DESC);
COMMENT ON TABLE ops.qms_fmea_risk IS 'FMEA 高风险项(跨模块共享,RPN>=100 入清单)';

-- FMEA 闭环跟踪日志
CREATE TABLE ops.qms_fmea_risk_track (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  risk_id       UUID NOT NULL REFERENCES ops.qms_fmea_risk(id),
  from_status   VARCHAR(16),
  to_status     VARCHAR(16) NOT NULL,
  operator      VARCHAR(64) NOT NULL,
  operate_time  TIMESTAMPTZ NOT NULL DEFAULT now(),
  action_note   TEXT,
  evidence      TEXT,
  esign_id      UUID
);
COMMENT ON TABLE ops.qms_fmea_risk_track IS 'FMEA 高风险闭环跟踪日志';

-- FMEA 评估模板(配置主数据)
CREATE TABLE ops.qms_fmea_template (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID REFERENCES ops.sys_org(id),
  template_no VARCHAR(32) NOT NULL UNIQUE,
  fmea_type   VARCHAR(8) NOT NULL,
  items       JSONB NOT NULL,
  rpn_threshold JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.qms_fmea_template IS 'FMEA 评估模板(配置主数据)';

-- 统一待办收件箱
CREATE TABLE ops.todo_item (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID NOT NULL REFERENCES ops.sys_org(id),
  title            TEXT NOT NULL,
  biz_type         VARCHAR(32) NOT NULL,
  biz_id           VARCHAR(64) NOT NULL,
  source_module    VARCHAR(16) NOT NULL,
  assignee_role    VARCHAR(32),
  assignee_user_id UUID,
  priority         VARCHAR(8) DEFAULT 'normal',
  status           VARCHAR(16) NOT NULL DEFAULT '待处理',
  due_at           TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_todo_assignee ON ops.todo_item(org_id, assignee_user_id, status);
COMMENT ON TABLE ops.todo_item IS '统一待办收件箱(各模块推送)';

-- 通知发送日志
CREATE TABLE ops.notification_log (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  biz_type    VARCHAR(32) NOT NULL,
  biz_id      VARCHAR(64) NOT NULL,
  channel     VARCHAR(16) NOT NULL,
  receiver    VARCHAR(128) NOT NULL,
  content     TEXT NOT NULL,
  level       VARCHAR(8),
  send_status VARCHAR(16) NOT NULL,
  fail_reason TEXT,
  retry_count INTEGER NOT NULL DEFAULT 0,
  sent_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notif_biz ON ops.notification_log(org_id, biz_type, biz_id);
COMMENT ON TABLE ops.notification_log IS '通知发送日志(多通道,失败重试)';

-- 操作审计日志(写审计库,ALCOA+;全局操作 org_id 为 null)
CREATE TABLE ops.audit_log (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID REFERENCES ops.sys_org(id),
  biz_type      VARCHAR(32) NOT NULL,
  biz_id        VARCHAR(64),
  operator_id   UUID NOT NULL,
  operator_name VARCHAR(64) NOT NULL,
  operate_time  TIMESTAMPTZ NOT NULL DEFAULT now(),
  client_ip     VARCHAR(64),
  user_agent    VARCHAR(255),
  action        VARCHAR(64) NOT NULL,
  before_value  JSONB,
  after_value   JSONB,
  remark        TEXT
);
CREATE INDEX idx_audit_biz ON ops.audit_log(biz_type, biz_id);
CREATE INDEX idx_audit_op ON ops.audit_log(operator_id, operate_time);
COMMENT ON TABLE ops.audit_log IS '操作审计日志(写审计库,不可删改,ALCOA+)';

-- 通用附件(MinIO 存储)
CREATE TABLE ops.sys_attachment (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  biz_type     VARCHAR(32) NOT NULL,
  biz_id       VARCHAR(64) NOT NULL,
  file_name    VARCHAR(255) NOT NULL,
  file_path    VARCHAR(512) NOT NULL,
  file_hash    CHAR(64) NOT NULL,
  file_size    BIGINT,
  mime_type    VARCHAR(64),
  uploaded_by  UUID,
  uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_deleted   BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_attach_biz ON ops.sys_attachment(org_id, biz_type, biz_id);
COMMENT ON TABLE ops.sys_attachment IS '通用附件(照片/报告/证据,MinIO 存储)';
