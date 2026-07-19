-- V05: NCM 不良管理(9 表)
-- org_id:业务/快照/方案/报表/趋势日志 NOT NULL;不良字典/日报配置/ESC 升级等通用配置 NULLABLE。
-- defect_dict_code、defect_no 为 VARCHAR 业务编号,保留 FK。

-- 不良字典(主数据,通用)
CREATE TABLE ops.ncm_defect_dict (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID REFERENCES ops.sys_org(id),
  code            VARCHAR(16) NOT NULL UNIQUE,
  name            VARCHAR(64) NOT NULL,
  category        VARCHAR(16) NOT NULL,
  level           VARCHAR(8) NOT NULL,
  status          VARCHAR(8) NOT NULL DEFAULT '启用',
  reference_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.ncm_defect_dict IS '不良字典(主数据,停用不可复用)';

-- 不良记录(6维绑定)
CREATE TABLE ops.ncm_defect_record (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID NOT NULL REFERENCES ops.sys_org(id),
  defect_no        VARCHAR(32) NOT NULL UNIQUE,
  wo_no            VARCHAR(32) NOT NULL,
  process_code     VARCHAR(16) NOT NULL,
  defect_dict_code VARCHAR(16) NOT NULL REFERENCES ops.ncm_defect_dict(code),
  severity         VARCHAR(8) NOT NULL,
  defect_count     INT NOT NULL,
  batch_total      INT,
  defect_rate      NUMERIC(6,3),
  device_code      VARCHAR(32),
  batch_no         VARCHAR(32),
  product_model    VARCHAR(64),
  operator_id      UUID NOT NULL,
  source           VARCHAR(8) NOT NULL DEFAULT '手动',
  device_payload   JSONB,
  occurred_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  remark           TEXT,
  disposition      VARCHAR(16),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_ncm_defect_multi ON ops.ncm_defect_record(org_id, defect_dict_code, process_code, device_code, batch_no, occurred_at DESC);
CREATE INDEX idx_ncm_defect_wo    ON ops.ncm_defect_record(org_id, wo_no);
COMMENT ON TABLE ops.ncm_defect_record IS '不良记录(6维绑定:类型/工序/设备/批次/时间/产品)';

-- 纠正措施跟踪(轻量,区别于 CAPA)
CREATE TABLE ops.ncm_corrective_action (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID NOT NULL REFERENCES ops.sys_org(id),
  ca_no      VARCHAR(32) NOT NULL UNIQUE,
  defect_no  VARCHAR(32) REFERENCES ops.ncm_defect_record(defect_no),
  issue      TEXT NOT NULL,
  owner      VARCHAR(64) NOT NULL,
  due_date   DATE NOT NULL,
  status     VARCHAR(16) NOT NULL DEFAULT '待启动',
  progress   SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 实时看板快照(5分钟刷新)
CREATE TABLE ops.ncm_realtime_snapshot (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  snapshot_time   TIMESTAMPTZ NOT NULL,
  shift           VARCHAR(8) NOT NULL,
  defect_count    INT NOT NULL,
  defect_rate     NUMERIC(6,3) NOT NULL,
  top_types       JSONB,
  process_heatmap JSONB,
  data_freshness  VARCHAR(8) NOT NULL
);

-- 分析方案
CREATE TABLE ops.ncm_filter_scheme (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  scheme_name  VARCHAR(64) NOT NULL,
  owner_id     UUID NOT NULL,
  filter_json  JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- BI 固定报表
CREATE TABLE ops.ncm_bi_report (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  report_no    VARCHAR(32) NOT NULL UNIQUE,
  report_type  VARCHAR(32) NOT NULL,
  period       VARCHAR(16) NOT NULL,
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  file_url     VARCHAR(255),
  status       VARCHAR(16) NOT NULL
);

-- 定时日报配置(通用)
CREATE TABLE ops.ncm_daily_report_config (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID REFERENCES ops.sys_org(id),
  push_time  TIME NOT NULL,
  receivers  JSONB NOT NULL,
  enabled    BOOLEAN NOT NULL DEFAULT true
);

-- 告警升级配置(ESC 三级,通用)
CREATE TABLE ops.ncm_alert_escalation (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID REFERENCES ops.sys_org(id),
  level           SMALLINT NOT NULL,
  timeout_minutes INT NOT NULL,
  notify_role     VARCHAR(16) NOT NULL,
  off_hours_delay BOOLEAN NOT NULL DEFAULT true
);
COMMENT ON TABLE ops.ncm_alert_escalation IS 'ESC 三级升级(30min->班组长/60min->主管/120min->经理)';

-- 趋势预警日志
CREATE TABLE ops.ncm_trend_alert (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  alert_type   VARCHAR(16) NOT NULL,
  dimension    VARCHAR(32) NOT NULL,
  notify_role  VARCHAR(16) NOT NULL,
  triggered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
