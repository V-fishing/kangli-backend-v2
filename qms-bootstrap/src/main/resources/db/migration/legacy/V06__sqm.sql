-- V06: SQM 供应商质量(38 表,收尾 stage-1)
-- 含 4 张二期表(sqm_supplier_cert/performance/escalation/share),建空表备用。
-- sqm_supplier_cert 双 version 冲突:证书版本改名 cert_version,保留 version INTEGER 乐观锁。
-- org_id:业务/子表/主数据 NOT NULL;配置规则 NULLABLE。
-- 跨模块 FK:sqm_incoming_abnormal.d8_id -> qms_8d_report(id)、capa_id -> qms_capa(id)(V02 已建)。

-- ============ 供应商 ============
CREATE TABLE ops.sqm_supplier (
  id               UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id           UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_no      VARCHAR(32) NOT NULL UNIQUE,
  supplier_code    VARCHAR(16) NOT NULL UNIQUE,
  name             VARCHAR(255) NOT NULL,
  credit_code      VARCHAR(32) NOT NULL UNIQUE,
  category         VARCHAR(32) NOT NULL,
  level            CHAR(1),
  status           VARCHAR(16) NOT NULL DEFAULT '合格',
  score            NUMERIC(5,2),
  contact_person   VARCHAR(64),
  contact_phone    VARCHAR(32),
  address          VARCHAR(255),
  certs            JSONB,
  last_audit_date  DATE,
  next_audit_date  DATE,
  observe_flag     BOOLEAN NOT NULL DEFAULT false,
  sole_source_flag BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_supplier IS '供应商档案(主数据)';

-- 供应商资质(二期)
CREATE TABLE ops.sqm_supplier_cert (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_id  UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  cert_type    VARCHAR(32) NOT NULL,
  cert_name    VARCHAR(128) NOT NULL,
  cert_no      VARCHAR(64),
  issue_date   DATE,
  expiry_date  DATE NOT NULL,
  file_url     VARCHAR(512) NOT NULL,
  file_hash    CHAR(64) NOT NULL,
  cert_version INTEGER NOT NULL,               -- 证书版本(原 doc version,避让乐观锁)
  status       VARCHAR(16) NOT NULL,
  warn_level   VARCHAR(8),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 供应商绩效(二期)
CREATE TABLE ops.sqm_supplier_performance (
  id                   UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id               UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_id          UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  period               CHAR(7) NOT NULL,
  score                NUMERIC(5,2) NOT NULL,
  delivery_score       NUMERIC(5,2),
  quality_score        NUMERIC(5,2),
  service_score        NUMERIC(5,2),
  incoming_pass_rate   NUMERIC(5,2),
  defect_rate          NUMERIC(6,3),
  rectify_timely_rate  NUMERIC(5,2),
  delivery_timely_rate NUMERIC(5,2),
  compliance_rate      NUMERIC(5,2),
  level                CHAR(1),
  observe_flag         BOOLEAN NOT NULL DEFAULT false,
  data_missing_flag    BOOLEAN NOT NULL DEFAULT false,
  UNIQUE(supplier_id, period)
);

-- 供应商升级管理(二期)
CREATE TABLE ops.sqm_supplier_escalation (
  id                      UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id                  UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_id             UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  current_level           CHAR(1) NOT NULL,
  quality_issue_count_6m  INT NOT NULL,
  repeat_problem_count    INT,
  suggested_action        TEXT NOT NULL,
  escalation_status       VARCHAR(16) NOT NULL,
  escalation_action       VARCHAR(32),
  notice_sent_flag        BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 供应商份额(二期)
CREATE TABLE ops.sqm_supplier_share (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  supplier_id   UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  part_no       VARCHAR(64) NOT NULL,
  share_ratio   NUMERIC(5,2) NOT NULL,
  effective_date DATE NOT NULL,
  change_reason TEXT,
  prev_ratio    NUMERIC(5,2),
  linked_level  CHAR(1)
);

-- ============ 审核 ============
CREATE TABLE ops.sqm_audit_plan (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  plan_no      VARCHAR(32) NOT NULL UNIQUE,
  supplier_id  UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  audit_type   VARCHAR(16) NOT NULL,
  plan_date    DATE NOT NULL,
  audit_lead   VARCHAR(64),
  auditor_team TEXT,
  scope        VARCHAR(255),
  risk_level   VARCHAR(8),
  actual_date  DATE,
  status       VARCHAR(16) NOT NULL DEFAULT '计划中',
  record_id    UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_audit_plan IS '供应商审核计划';

CREATE TABLE ops.sqm_audit_record (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  record_no    VARCHAR(32) NOT NULL UNIQUE,
  plan_id      UUID REFERENCES ops.sqm_audit_plan(id),
  supplier_id  UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  audit_type   VARCHAR(16) NOT NULL,
  audit_date   DATE NOT NULL,
  audit_lead   VARCHAR(64),
  auditor_team TEXT,
  result       VARCHAR(16) NOT NULL,
  score        NUMERIC(5,2),
  nc_count     INT NOT NULL DEFAULT 0,
  conclusion   TEXT,
  status       VARCHAR(16) NOT NULL DEFAULT '执行中',
  archive_id   UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE ops.sqm_audit_checklist_item (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID NOT NULL REFERENCES ops.sys_org(id),
  record_id   UUID NOT NULL REFERENCES ops.sqm_audit_record(id),
  seq         INT NOT NULL,
  clause      VARCHAR(32),
  item_name   VARCHAR(128) NOT NULL,
  result      VARCHAR(16) NOT NULL,
  evidence    TEXT,
  nc_id       UUID,
  UNIQUE(record_id, seq)
);

CREATE TABLE ops.sqm_audit_photo (
  id                 UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id             UUID NOT NULL REFERENCES ops.sys_org(id),
  record_id          UUID NOT NULL REFERENCES ops.sqm_audit_record(id),
  checklist_item_id  UUID,
  file_path          VARCHAR(512) NOT NULL,
  file_name          VARCHAR(255),
  file_hash          CHAR(64),
  watermark_time     TIMESTAMPTZ NOT NULL,
  watermark_location VARCHAR(255) NOT NULL,
  shoot_by           VARCHAR(64),
  shoot_time         TIMESTAMPTZ
);
COMMENT ON TABLE ops.sqm_audit_photo IS '审核现场照片(时间+GPS 水印)';

CREATE TABLE ops.sqm_audit_nc (
  id                UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id            UUID NOT NULL REFERENCES ops.sys_org(id),
  nc_no             VARCHAR(32) NOT NULL UNIQUE,
  record_id         UUID NOT NULL REFERENCES ops.sqm_audit_record(id),
  supplier_id       UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  clause            VARCHAR(32),
  description       TEXT NOT NULL,
  level             VARCHAR(16) NOT NULL,
  status            VARCHAR(16) NOT NULL DEFAULT '整改中',
  responsible       VARCHAR(64),
  deadline          DATE,
  rectify_measure   TEXT,
  rectify_attachment JSONB,
  rectify_date      TIMESTAMPTZ,
  need_site_review  BOOLEAN NOT NULL DEFAULT false,
  verify_result     VARCHAR(16),
  verify_comment    TEXT,
  verify_date       TIMESTAMPTZ,
  verify_by         VARCHAR(64),
  verified_batches  INT,
  close_date        TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_audit_nc IS '审核不符合项(分级整改,严重项现场复核+连续3批闭环)';

CREATE TABLE ops.sqm_audit_report_archive (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  archive_no      VARCHAR(32) NOT NULL UNIQUE,
  record_id       UUID NOT NULL REFERENCES ops.sqm_audit_record(id),
  plan_id         UUID,
  supplier_id     UUID NOT NULL,
  report_file_path VARCHAR(512) NOT NULL,
  report_hash     CHAR(64) NOT NULL,
  assembled_at    TIMESTAMPTZ NOT NULL,
  archive_date    TIMESTAMPTZ NOT NULL,
  retention_until DATE NOT NULL
);
COMMENT ON TABLE ops.sqm_audit_report_archive IS '审核报告归档(15年,受控文件水印)';

-- 审核频次规则(配置)
CREATE TABLE ops.sqm_audit_freq_rule (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID REFERENCES ops.sys_org(id),
  risk_level    VARCHAR(8) NOT NULL,
  level         CHAR(1),
  freq_per_year SMALLINT NOT NULL,
  audit_type    VARCHAR(16) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- ============ 变更 ============
CREATE TABLE ops.sqm_change_order (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  change_no      VARCHAR(32) NOT NULL UNIQUE,
  title          VARCHAR(255) NOT NULL,
  supplier_id    UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  part_no        VARCHAR(64) NOT NULL,
  change_type    VARCHAR(16) NOT NULL,
  reason         TEXT,
  applicant      VARCHAR(64),
  apply_date     DATE NOT NULL,
  urgency        VARCHAR(8) NOT NULL,
  strict_flag    BOOLEAN NOT NULL DEFAULT false,
  risk_pre_mark  VARCHAR(8),
  source         VARCHAR(16) NOT NULL,
  receive_frozen BOOLEAN NOT NULL DEFAULT false,
  status         VARCHAR(16) NOT NULL DEFAULT '待申请',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_change_order IS '物料变更单(冻结收货,会签+一票否决)';

CREATE TABLE ops.sqm_change_doc (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  change_id     UUID NOT NULL REFERENCES ops.sqm_change_order(id),
  doc_type      VARCHAR(16) NOT NULL,
  submitted     BOOLEAN NOT NULL DEFAULT false,
  file_path     JSONB,
  submitted_by  VARCHAR(64),
  submitted_date TIMESTAMPTZ
);

CREATE TABLE ops.sqm_change_approval (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  change_id     UUID NOT NULL REFERENCES ops.sqm_change_order(id),
  approval_role VARCHAR(16) NOT NULL,
  role_label    VARCHAR(32) NOT NULL,
  status        VARCHAR(16) NOT NULL DEFAULT 'pending',
  operator      VARCHAR(64),
  operate_date  TIMESTAMPTZ,
  opinion       TEXT,
  has_veto      BOOLEAN NOT NULL DEFAULT false,
  seq_order     INT NOT NULL,
  esign_id      UUID,
  UNIQUE(change_id, approval_role)
);
COMMENT ON TABLE ops.sqm_change_approval IS '变更会签(质量/采购/研发并行 + 质量一票否决 + 试产串行)';

CREATE TABLE ops.sqm_change_workflow_log (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  change_id    UUID NOT NULL REFERENCES ops.sqm_change_order(id),
  step         VARCHAR(32) NOT NULL,
  operator     VARCHAR(64),
  operate_date TIMESTAMPTZ,
  action_desc  VARCHAR(255),
  status       VARCHAR(16) NOT NULL
);

CREATE TABLE ops.sqm_change_impact (
  id                UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id            UUID NOT NULL REFERENCES ops.sys_org(id),
  change_id         UUID NOT NULL UNIQUE REFERENCES ops.sqm_change_order(id),
  affected_products JSONB,
  switch_date       DATE,
  trial_qty         INT,
  strict_plan       TEXT
);

CREATE TABLE ops.sqm_change_strict_inspect (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  strict_no    VARCHAR(32) NOT NULL UNIQUE,
  change_id    UUID NOT NULL REFERENCES ops.sqm_change_order(id),
  lot_id       UUID NOT NULL,
  inspect_type VARCHAR(16) NOT NULL DEFAULT '加严',
  aql_level    VARCHAR(16) NOT NULL,
  result       VARCHAR(16) NOT NULL DEFAULT '待检',
  inspect_date DATE,
  seq          INT NOT NULL,
  total_seq    INT NOT NULL DEFAULT 3,
  restored     BOOLEAN NOT NULL DEFAULT false
);
COMMENT ON TABLE ops.sqm_change_strict_inspect IS '加严检验跟踪(连续3批合格恢复/不合格回滚)';

CREATE TABLE ops.sqm_change_sop_notice (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  change_id      UUID NOT NULL REFERENCES ops.sqm_change_order(id),
  sop_file       VARCHAR(255) NOT NULL,
  version        VARCHAR(32) NOT NULL,          -- SOP 版本
  update_content TEXT,
  publish_date   DATE,
  status         VARCHAR(16) NOT NULL DEFAULT '待更新'
);

-- 变更风险规则(配置)
CREATE TABLE ops.sqm_change_risk_rule (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID REFERENCES ops.sys_org(id),
  change_type  VARCHAR(16) NOT NULL,
  risk_level   VARCHAR(8) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- ============ 追溯 ============
CREATE TABLE ops.sqm_incoming_lot (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  lot_no         VARCHAR(32) NOT NULL UNIQUE,
  supplier_id    UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  part_no        VARCHAR(64) NOT NULL,
  part_name      VARCHAR(128) NOT NULL,
  qty            NUMERIC(14,2) NOT NULL,
  unit           VARCHAR(16),
  incoming_date  DATE NOT NULL,
  inspect_result VARCHAR(16) NOT NULL,
  inspect_type   VARCHAR(16) NOT NULL,
  iqc_pass       BOOLEAN NOT NULL DEFAULT false,
  po_no          VARCHAR(32),
  change_id      UUID REFERENCES ops.sqm_change_order(id),
  is_key_part    BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_lot_supplier ON ops.sqm_incoming_lot(org_id, supplier_id, incoming_date DESC);
COMMENT ON TABLE ops.sqm_incoming_lot IS '来料批次(追溯根)';

-- 追溯节点(自引用树)
CREATE TABLE ops.sqm_trace_node (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  root_lot_id   UUID NOT NULL REFERENCES ops.sqm_incoming_lot(id),
  parent_node_id UUID REFERENCES ops.sqm_trace_node(id),
  node_type     VARCHAR(16) NOT NULL,
  node_name     VARCHAR(255) NOT NULL,
  batch_no      VARCHAR(64),
  qty           NUMERIC(14,2),
  unit          VARCHAR(16),
  node_date     DATE,
  supplier_id   UUID REFERENCES ops.sqm_supplier(id),
  remark        TEXT,
  tree_level    INT NOT NULL DEFAULT 0,
  is_valid      VARCHAR(8) NOT NULL DEFAULT '是',
  invalid_by    VARCHAR(64),
  invalid_time  TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_trace_parent ON ops.sqm_trace_node(org_id, parent_node_id);
CREATE INDEX idx_trace_root   ON ops.sqm_trace_node(org_id, root_lot_id);
COMMENT ON TABLE ops.sqm_trace_node IS '追溯节点(自引用树,WITH RECURSIVE 正/逆向)';

CREATE TABLE ops.sqm_trace_raw_detail (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  node_id         UUID NOT NULL UNIQUE REFERENCES ops.sqm_trace_node(id),
  category        VARCHAR(32),
  wo_no           VARCHAR(32),
  product_barcode VARCHAR(64),
  product_part_no VARCHAR(64),
  product_name    VARCHAR(128),
  wo_qty          NUMERIC(14,2),
  material_barcode VARCHAR(64),
  material_code   VARCHAR(64),
  material_name   VARCHAR(128),
  spec_model      VARCHAR(128),
  scanner         VARCHAR(64),
  scan_time       TIMESTAMPTZ,
  process_code    VARCHAR(32),
  process_name    VARCHAR(64)
);

CREATE TABLE ops.sqm_trace_product_detail (
  id                 UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id             UUID NOT NULL REFERENCES ops.sys_org(id),
  node_id            UUID NOT NULL UNIQUE REFERENCES ops.sqm_trace_node(id),
  is_urgent          VARCHAR(8),
  qc_review          VARCHAR(16),
  mg_approve         VARCHAR(16),
  inspect_result     VARCHAR(16),
  report_no          VARCHAR(32),
  inspect_order_no   VARCHAR(32),
  production_order_no VARCHAR(32),
  material_code      VARCHAR(64),
  product_name       VARCHAR(128),
  model_spec         VARCHAR(128),
  batch_no           VARCHAR(64),
  production_date    DATE,
  expiry_date        DATE,
  inspect_qty        NUMERIC(14,2),
  inspect_count      NUMERIC(14,2),
  pass_qty           NUMERIC(14,2),
  fail_qty           NUMERIC(14,2),
  unit               VARCHAR(16),
  inspector          VARCHAR(64),
  category           VARCHAR(32),
  qc_reviewer        VARCHAR(64),
  qc_review_time     TIMESTAMPTZ,
  mg_approver        VARCHAR(64),
  mg_approve_time    TIMESTAMPTZ,
  drug_reg_no        VARCHAR(64),
  perf_inspect_method VARCHAR(64),
  perf_batch_no      VARCHAR(64),
  customer           VARCHAR(128),
  customer_code      VARCHAR(64),
  customer_order_no  VARCHAR(64),
  ship_date          DATE,
  tracking_no        VARCHAR(64),
  ship_address       VARCHAR(255)
);

CREATE TABLE ops.sqm_trace_customer_detail (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  node_id         UUID NOT NULL UNIQUE REFERENCES ops.sqm_trace_node(id),
  customer_name   VARCHAR(128) NOT NULL,
  customer_code   VARCHAR(64),
  customer_order_no VARCHAR(64),
  ship_date       DATE,
  tracking_no     VARCHAR(64),
  ship_address    VARCHAR(255),
  contact_person  VARCHAR(64),
  contact_phone   VARCHAR(32),
  qty             NUMERIC(14,2),
  unit            VARCHAR(16)
);

-- 关键件 SN
CREATE TABLE ops.sqm_key_part_sn (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  sn_code      VARCHAR(64) NOT NULL UNIQUE,
  lot_id       UUID NOT NULL REFERENCES ops.sqm_incoming_lot(id),
  part_no      VARCHAR(64) NOT NULL,
  part_name    VARCHAR(128),
  supplier_id  UUID REFERENCES ops.sqm_supplier(id),
  status       VARCHAR(16) NOT NULL,
  line         VARCHAR(32),
  wo_no        VARCHAR(32),
  scan_time    TIMESTAMPTZ
);
CREATE INDEX idx_sn_wo ON ops.sqm_key_part_sn(org_id, wo_no);
COMMENT ON TABLE ops.sqm_key_part_sn IS '关键件 SN(逐件追溯,非关键件走树状)';

-- ============ 异常整改 ============
CREATE TABLE ops.sqm_incoming_abnormal (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  abnormal_no     VARCHAR(32) NOT NULL UNIQUE,
  lot_id          VARCHAR(32) NOT NULL,
  supplier_id     UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  part_no         VARCHAR(64) NOT NULL,
  part_name       VARCHAR(128) NOT NULL,
  description     TEXT NOT NULL,
  qty             INTEGER NOT NULL,
  level           VARCHAR(8) NOT NULL,
  occur_date      DATE NOT NULL,
  handler_id      UUID,
  status          VARCHAR(16) NOT NULL DEFAULT '待处理',
  disposal        VARCHAR(16),
  disposal_remark TEXT,
  d8_id           UUID REFERENCES ops.qms_8d_report(id),
  capa_id         UUID REFERENCES ops.qms_capa(id),
  rectify_type    VARCHAR(8),
  overdue_days    INTEGER NOT NULL DEFAULT 0,
  close_date      DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_incoming_abnormal IS '来料异常整改单(严重1件/一般>=3件触发)';

-- 整改通知(NCM + SQM 共用)
CREATE TABLE ops.sqm_rectify_notice (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  notice_no    VARCHAR(32) NOT NULL UNIQUE,
  biz_source   VARCHAR(16) NOT NULL,
  abnormal_id  UUID REFERENCES ops.sqm_incoming_abnormal(id),
  supplier_id  UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  title        VARCHAR(255) NOT NULL,
  content      TEXT NOT NULL,
  deadline     DATE NOT NULL,
  notice_date  DATE NOT NULL,
  channels     VARCHAR(32) NOT NULL,
  sent_flag    BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 供应商整改措施
CREATE TABLE ops.sqm_supplier_measure (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  measure_id      VARCHAR(32) NOT NULL UNIQUE,
  abnormal_id     UUID NOT NULL REFERENCES ops.sqm_incoming_abnormal(id),
  supplier_id     UUID NOT NULL REFERENCES ops.sqm_supplier(id),
  content         TEXT NOT NULL,
  rootcause       TEXT NOT NULL,
  prevention      TEXT NOT NULL,
  deadline        DATE NOT NULL,
  owner           VARCHAR(64) NOT NULL,
  submit_date     DATE NOT NULL,
  evidence_files  JSONB,
  esign_id        UUID
);

-- SQE 验证记录
CREATE TABLE ops.sqm_sqe_verification (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID NOT NULL REFERENCES ops.sys_org(id),
  verification_id VARCHAR(32) NOT NULL UNIQUE,
  abnormal_id    UUID NOT NULL REFERENCES ops.sqm_incoming_abnormal(id),
  sqe_id         UUID NOT NULL,
  verify_result  VARCHAR(8) NOT NULL,
  verify_comment TEXT,
  verify_date    DATE NOT NULL,
  qualified_batch_count INTEGER,
  esign_id       UUID
);

-- 闭环批次验证(连续3批合格)
CREATE TABLE ops.sqm_rectify_batch_verify (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  abnormal_id  UUID NOT NULL REFERENCES ops.sqm_incoming_abnormal(id),
  batch_no     VARCHAR(32) NOT NULL,
  inspect_result VARCHAR(8) NOT NULL,
  inspect_date DATE,
  seq          INTEGER NOT NULL,
  total_seq    INTEGER NOT NULL DEFAULT 3,
  restored_flag BOOLEAN NOT NULL DEFAULT false
);

-- 异常触发规则(配置)
CREATE TABLE ops.sqm_abnormal_trigger_rule (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id     UUID REFERENCES ops.sys_org(id),
  level      VARCHAR(8) NOT NULL,
  threshold  INT NOT NULL,
  auto_flag  BOOLEAN NOT NULL DEFAULT true
);

-- 整改 SLA 规则(配置)
CREATE TABLE ops.sqm_rectify_sla_rule (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID REFERENCES ops.sys_org(id),
  stage         VARCHAR(16) NOT NULL,
  days          INT NOT NULL,
  notify_role   VARCHAR(32) NOT NULL
);

-- 重复问题规则(配置)
CREATE TABLE ops.sqm_repeat_problem_rule (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID REFERENCES ops.sys_org(id),
  window_days INT NOT NULL DEFAULT 30,
  threshold   INT NOT NULL DEFAULT 2,
  action      VARCHAR(32) NOT NULL
);

-- 升级递进动作规则(配置)
CREATE TABLE ops.sqm_escalation_action_rule (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID REFERENCES ops.sys_org(id),
  repeat_count INT NOT NULL,
  action       VARCHAR(32) NOT NULL
);

-- 供应商评级规则(配置)
CREATE TABLE ops.sqm_supplier_grade_rule (
  id                 UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id             UUID REFERENCES ops.sys_org(id),
  score_min          NUMERIC(5,2) NOT NULL,
  score_max          NUMERIC(5,2) NOT NULL,
  level              CHAR(1) NOT NULL,
  observe_first_year BOOLEAN NOT NULL DEFAULT true
);

-- ============ 主数据 ============
CREATE TABLE ops.sqm_material (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  part_no      VARCHAR(64) NOT NULL UNIQUE,
  part_name    VARCHAR(128) NOT NULL,
  spec_model   VARCHAR(128),
  category     VARCHAR(32),
  is_key_part  BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE ops.sqm_customer (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  customer_code VARCHAR(32) NOT NULL UNIQUE,
  customer_name VARCHAR(128) NOT NULL,
  contact_person VARCHAR(64),
  contact_phone  VARCHAR(32),
  address       VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
