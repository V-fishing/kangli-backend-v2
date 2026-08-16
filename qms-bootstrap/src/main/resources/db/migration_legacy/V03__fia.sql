-- V03: FIA 首件检验(10 表)
-- 注:fia_insp_std 原 doc 有两个 version 列(业务版本 v1/v2/v3 + 乐观锁 integer),冲突;
--     业务版本改名 std_version(对齐 fia_task.std_version),保留 version INTEGER 给 BaseEntity 乐观锁。
-- 审计字段按 DB 设计文档:业务/配置主表含,子表/日志/单行配置不含。

-- 检验标准库(主数据,版本化;物料+工序按公司唯一生效)
CREATE TABLE ops.fia_insp_std (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  code            VARCHAR(32) NOT NULL UNIQUE,
  material        VARCHAR(128) NOT NULL,
  proc_name       VARCHAR(32) NOT NULL,
  aql             VARCHAR(16),
  inspect_level   VARCHAR(4),
  sample_plan     VARCHAR(16),
  ctq_text        VARCHAR(255),
  std_version     VARCHAR(16) NOT NULL,        -- 业务版本 v1/v2/v3
  status          VARCHAR(16) NOT NULL DEFAULT '草稿',
  prev_version_id UUID REFERENCES ops.fia_insp_std(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX uk_fia_std_mat_proc ON ops.fia_insp_std(org_id, material, proc_name) WHERE status='生效' AND is_deleted=false;
COMMENT ON TABLE ops.fia_insp_std IS '检验标准库(主数据,物料+工序按公司唯一生效版本)';

-- 标准检测项模板(子表,无审计字段)
CREATE TABLE ops.fia_insp_std_item (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  std_id      UUID NOT NULL REFERENCES ops.fia_insp_std(id),
  seq         INT NOT NULL,
  item_name   VARCHAR(128) NOT NULL,
  is_ctq      BOOLEAN NOT NULL DEFAULT false,
  std_value   VARCHAR(32),
  tolerance   VARCHAR(32),
  unit        VARCHAR(16),
  value_type  VARCHAR(8),
  enum_values VARCHAR(128),
  UNIQUE(std_id, seq)
);

-- 触发事件类型(主数据,全局)
CREATE TABLE ops.fia_trigger_type (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID REFERENCES ops.sys_org(id),
  name        VARCHAR(32) NOT NULL UNIQUE,
  is_enabled  BOOLEAN NOT NULL DEFAULT true,
  description VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 首件检验任务(校验单)
CREATE TABLE ops.fia_task (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  code          VARCHAR(32) NOT NULL UNIQUE,
  wo_no         VARCHAR(32) NOT NULL,
  line_name     VARCHAR(64) NOT NULL,
  product_name  VARCHAR(128) NOT NULL,
  proc_name     VARCHAR(32) NOT NULL,
  trigger_type  VARCHAR(32) NOT NULL,
  std_id        UUID NOT NULL REFERENCES ops.fia_insp_std(id),
  std_version   VARCHAR(16) NOT NULL,
  aql           VARCHAR(16),
  sample_size   INT,
  sample_count  INT,
  batch_no      VARCHAR(32),
  status        VARCHAR(16) NOT NULL DEFAULT '待检',
  overall_judge VARCHAR(16),
  inspector_id  UUID,
  is_urgent     BOOLEAN NOT NULL DEFAULT false,
  sla_due_at    TIMESTAMPTZ,
  is_overdue    BOOLEAN NOT NULL DEFAULT false,
  disposition   VARCHAR(16),
  remark        TEXT,
  submitted_at  TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_fia_task_wo     ON ops.fia_task(org_id, wo_no);
CREATE INDEX idx_fia_task_status ON ops.fia_task(org_id, status, is_overdue);
COMMENT ON TABLE ops.fia_task IS '首件检验任务(校验单)';

-- 检验项目录入(子表,无审计字段;CTQ 超差整单不合格)
CREATE TABLE ops.fia_insp_item (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  task_id        UUID NOT NULL REFERENCES ops.fia_task(id),
  seq            INT NOT NULL,
  item_name      VARCHAR(128) NOT NULL,
  is_ctq         BOOLEAN NOT NULL DEFAULT false,
  std_value      VARCHAR(32),
  tolerance      VARCHAR(32),
  unit           VARCHAR(16),
  measured_value VARCHAR(32),
  judge          VARCHAR(16),
  std_item_id    UUID REFERENCES ops.fia_insp_std_item(id),
  UNIQUE(task_id, seq)
);
COMMENT ON TABLE ops.fia_insp_item IS '检验项目录入(CTQ 超差整单不合格)';

-- 审批(豁免/紧急放行/让步接收)
CREATE TABLE ops.fia_approval (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  code            VARCHAR(32) NOT NULL UNIQUE,
  approval_type   VARCHAR(16) NOT NULL,
  wo_no           VARCHAR(32) NOT NULL,
  task_id         UUID REFERENCES ops.fia_task(id),
  reason          TEXT NOT NULL,
  applicant_id    UUID NOT NULL,
  apply_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  status          VARCHAR(16) NOT NULL DEFAULT '待审批',
  approver_id     UUID,
  approve_opinion TEXT,
  approve_at      TIMESTAMPTZ,
  esign_id        UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.fia_approval IS '首件审批(豁免/紧急放行/让步接收)';

-- 归档报告(15 年留存,无审计字段)
CREATE TABLE ops.fia_archived_report (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  report_no       VARCHAR(32) NOT NULL UNIQUE,
  task_id         UUID NOT NULL REFERENCES ops.fia_task(id),
  wo_no           VARCHAR(32) NOT NULL,
  archive_date    DATE NOT NULL,
  status          VARCHAR(16) NOT NULL DEFAULT '已归档',
  pdf_ref         VARCHAR(255) NOT NULL,
  report_hash     CHAR(64) NOT NULL,
  retention_until DATE NOT NULL
);
COMMENT ON TABLE ops.fia_archived_report IS '首件检验归档报告(15 年留存)';

-- 任务时间线(日志,无审计字段)
CREATE TABLE ops.fia_task_log (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID NOT NULL REFERENCES ops.sys_org(id),
  task_id    UUID NOT NULL REFERENCES ops.fia_task(id),
  node_seq   INT NOT NULL,
  node_name  VARCHAR(64) NOT NULL,
  op_time    TIMESTAMPTZ NOT NULL DEFAULT now(),
  operator   VARCHAR(64) NOT NULL,
  op_type    VARCHAR(8) NOT NULL,
  is_done    BOOLEAN NOT NULL DEFAULT true
);
CREATE INDEX idx_fia_log_task ON ops.fia_task_log(org_id, task_id);

-- 拦截与时效配置(全局单行)
CREATE TABLE ops.fia_intercept_config (
  id                  UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id              UUID REFERENCES ops.sys_org(id),
  intercept_mode      VARCHAR(16) NOT NULL,
  multi_trigger_mode  VARCHAR(16) NOT NULL,
  sla_hours           NUMERIC(4,1) NOT NULL DEFAULT 2,
  escalate_fail_count INT NOT NULL DEFAULT 3
);

-- 电子签名配置(全局单行)
CREATE TABLE ops.fia_sign_config (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID REFERENCES ops.sys_org(id),
  sign_methods     VARCHAR(64)[] NOT NULL,
  sign_nodes       VARCHAR(16) NOT NULL,
  sign_granularity VARCHAR(16) NOT NULL,
  lock_after_fail  INT NOT NULL DEFAULT 3,
  lock_minutes     INT NOT NULL DEFAULT 5
);
