-- 不良记录发起 8D/CAPA/CA 时的指派处理人记录(含通知方式,可追溯)
CREATE TABLE IF NOT EXISTS ops.qms_assign_record (
  id                 UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id             UUID,
  defect_id          UUID,
  defect_no          VARCHAR(64),
  biz_type           VARCHAR(16)  NOT NULL,   -- 8D / CAPA / CA
  biz_id             VARCHAR(64),
  biz_no             VARCHAR(64),
  assignee_user_id   UUID,
  assignee_user_name VARCHAR(64),
  assignee_role_code VARCHAR(64),
  assignee_role_name VARCHAR(64),
  notify_channels    VARCHAR(128),            -- 逗号分隔渠道名,如: 站内弹窗,钉钉
  assigner_id        UUID,
  remark             TEXT,
  is_deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by         VARCHAR(64),
  updated_by         VARCHAR(64),
  version            INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_qms_assign_record_defect ON ops.qms_assign_record (defect_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_qms_assign_record_user ON ops.qms_assign_record (assignee_user_id, is_deleted);
