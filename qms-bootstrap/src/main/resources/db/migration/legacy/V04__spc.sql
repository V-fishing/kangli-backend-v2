-- V04: SPC 过程能力(12 表,一期人工录入)
-- spc_subgroup 为 RANGE 分区表(按 subgroup_time),建 DEFAULT 分区兜底以便插入。
-- org_id:业务/子表/按公司配置 NOT NULL;WECO 规则/全局配置/通知渠道等通用主数据 NULLABLE。

-- 参数配置(主数据,按公司)
CREATE TABLE ops.spc_param (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  param_name     VARCHAR(64) NOT NULL,
  proc_name      VARCHAR(32) NOT NULL,
  unit           VARCHAR(16) NOT NULL,
  spec_lower     NUMERIC(12,4),
  spec_upper     NUMERIC(12,4),
  spec_text      VARCHAR(32) NOT NULL,
  target_value   NUMERIC(12,4),
  subgroup_size  INT NOT NULL DEFAULT 5 CHECK (subgroup_size BETWEEN 1 AND 25),
  collect_freq   VARCHAR(32) NOT NULL,
  chart_type     VARCHAR(8),
  is_active      BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.spc_param IS 'SPC 参数配置(工序-参数 CRUD)';

-- 子组数据(时序,按月分区;DEFAULT 分区兜底)
CREATE TABLE ops.spc_subgroup (
  id            UUID DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  param_id      UUID NOT NULL REFERENCES ops.spc_param(id),
  subgroup_no   INT NOT NULL,
  subgroup_time TIMESTAMPTZ NOT NULL,
  shift         VARCHAR(8),
  n             INT NOT NULL,
  xbar          NUMERIC(12,4),
  range_r       NUMERIC(12,4),
  judge         VARCHAR(8) NOT NULL DEFAULT '正常',
  is_outlier    BOOLEAN NOT NULL DEFAULT false,
  data_source   VARCHAR(8) NOT NULL DEFAULT 'manual',
  operator_id   UUID,
  wo_no         VARCHAR(32),
  batch_no      VARCHAR(32),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  PRIMARY KEY (id, subgroup_time)
) PARTITION BY RANGE (subgroup_time);
CREATE INDEX idx_spc_sub_param ON ops.spc_subgroup(org_id, param_id, subgroup_time DESC);
CREATE TABLE ops.spc_subgroup_default PARTITION OF ops.spc_subgroup DEFAULT;
COMMENT ON TABLE ops.spc_subgroup IS 'SPC 子组数据(时序,按月分区)';

-- 子组测量值(子表)
CREATE TABLE ops.spc_measurement (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  subgroup_id   UUID NOT NULL,
  subgroup_time TIMESTAMPTZ NOT NULL,
  seq           INT NOT NULL,
  value         NUMERIC(12,4) NOT NULL,
  UNIQUE(subgroup_id, seq)
);

-- 控制限(前25子组建基线)
CREATE TABLE ops.spc_control_limit (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  param_id        UUID NOT NULL REFERENCES ops.spc_param(id),
  chart_type      VARCHAR(8) NOT NULL,
  baseline_source VARCHAR(16) NOT NULL,
  n_subgroups     INT NOT NULL,
  xbar_ucl        NUMERIC(12,4) NOT NULL,
  xbar_cl         NUMERIC(12,4) NOT NULL,
  xbar_lcl        NUMERIC(12,4) NOT NULL,
  r_ucl           NUMERIC(12,4),
  r_cl            NUMERIC(12,4),
  r_lcl           NUMERIC(12,4),
  calc_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_active       BOOLEAN NOT NULL DEFAULT true
);
COMMENT ON TABLE ops.spc_control_limit IS 'SPC 控制限(前25子组建基线,动态更新)';

-- SPC 告警(WECO 规则,30min 抑制)
CREATE TABLE ops.spc_alarm (
  id                UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id            UUID NOT NULL REFERENCES ops.sys_org(id),
  code              VARCHAR(32) NOT NULL UNIQUE,
  param_id          UUID NOT NULL REFERENCES ops.spc_param(id),
  param_name        VARCHAR(64) NOT NULL,
  current_value     NUMERIC(12,4),
  triggered_rule    VARCHAR(32) NOT NULL,
  level             VARCHAR(8) NOT NULL,
  subgroup_start_no INT,
  subgroup_end_no   INT,
  alarm_time        TIMESTAMPTZ NOT NULL DEFAULT now(),
  status            VARCHAR(16) NOT NULL DEFAULT '待确认',
  close_reason      TEXT,
  disposition       TEXT,
  closed_by         UUID,
  closed_at         TIMESTAMPTZ,
  suppress_until    TIMESTAMPTZ,
  wo_no             VARCHAR(32),
  batch_no          VARCHAR(32),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_spc_alarm_status ON ops.spc_alarm(org_id, status, alarm_time DESC);
COMMENT ON TABLE ops.spc_alarm IS 'SPC 告警(WECO 规则,30min 抑制)';

-- 采集任务
CREATE TABLE ops.spc_collect_task (
  id                  UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id              UUID NOT NULL REFERENCES ops.sys_org(id),
  param_id            UUID NOT NULL REFERENCES ops.spc_param(id),
  collect_freq        VARCHAR(32) NOT NULL,
  last_value          NUMERIC(12,4),
  last_at             TIMESTAMPTZ,
  next_due_at         TIMESTAMPTZ,
  status              VARCHAR(16) NOT NULL DEFAULT '待采集',
  is_planned_downtime BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 数据导入日志
CREATE TABLE ops.spc_import_log (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  code         VARCHAR(32) NOT NULL UNIQUE,
  param_id     UUID NOT NULL REFERENCES ops.spc_param(id),
  param_name   VARCHAR(64) NOT NULL,
  file_name    VARCHAR(255) NOT NULL,
  file_type    VARCHAR(8),
  import_mode  VARCHAR(8) NOT NULL,
  record_count INT NOT NULL,
  error_count  INT NOT NULL DEFAULT 0,
  status       VARCHAR(16) NOT NULL,
  file_size    VARCHAR(16),
  operator_id  UUID NOT NULL,
  imported_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 过程能力快照(CPK/PPK)
CREATE TABLE ops.spc_capability (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID NOT NULL REFERENCES ops.sys_org(id),
  param_id         UUID NOT NULL REFERENCES ops.spc_param(id),
  period_type      VARCHAR(8) NOT NULL,
  period_value     VARCHAR(16) NOT NULL,
  cpk              NUMERIC(6,3),
  ppk              NUMERIC(6,3),
  level            VARCHAR(8),
  sample_count     INT,
  usl              NUMERIC(12,4),
  lsl              NUMERIC(12,4),
  calc_window_days INT NOT NULL DEFAULT 30,
  calc_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(param_id, period_type, period_value)
);
COMMENT ON TABLE ops.spc_capability IS '过程能力快照(CPK/PPK,月度趋势)';

-- 供应商来料能力(弱关联,主数据归 SQM)
CREATE TABLE ops.spc_supplier_capability (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_id  UUID NOT NULL,
  material_id  UUID NOT NULL,
  cpk          NUMERIC(6,3),
  level        VARCHAR(8),
  period_value VARCHAR(16) NOT NULL,
  UNIQUE(supplier_id, material_id, period_value)
);

-- 判异规则(主数据,Western Electric 8 规则,通用)
CREATE TABLE ops.spc_rule (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID REFERENCES ops.sys_org(id),
  rule_code  VARCHAR(4) NOT NULL UNIQUE,
  rule_name  VARCHAR(64) NOT NULL,
  level      VARCHAR(8) NOT NULL,
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  sort_no    INT
);
COMMENT ON TABLE ops.spc_rule IS 'WECO 判异规则(①-⑧)';

-- SPC 全局配置(单行,通用)
CREATE TABLE ops.spc_global_config (
  id                    UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id                UUID REFERENCES ops.sys_org(id),
  baseline_mode         VARCHAR(16) NOT NULL,
  default_subgroup_size INT NOT NULL DEFAULT 5,
  chart_auto_rules      VARCHAR(64)[] NOT NULL,
  cpk_period            VARCHAR(8) NOT NULL DEFAULT 'month',
  cpk_sufficient        NUMERIC(4,2) NOT NULL DEFAULT 1.33,
  cpk_acceptable        NUMERIC(4,2) NOT NULL DEFAULT 1.00,
  spec_source           VARCHAR(16) NOT NULL DEFAULT '检验标准库',
  alert_level           VARCHAR(16) NOT NULL,
  suppress_minutes      INT NOT NULL DEFAULT 30
);

-- 通知渠道(通用)
CREATE TABLE ops.spc_notify_channel (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID REFERENCES ops.sys_org(id),
  channel    VARCHAR(16) NOT NULL UNIQUE,
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  config_json JSONB
);
