-- 8D 整改报告归档(15 年留存,无审计字段)
CREATE TABLE ops.qms_8d_archived_report (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  archive_no      VARCHAR(32) NOT NULL UNIQUE,
  report_id       UUID NOT NULL REFERENCES ops.qms_8d_report(id),
  d8_no           VARCHAR(32) NOT NULL,
  archive_date    DATE NOT NULL,
  status          VARCHAR(16) NOT NULL DEFAULT '已归档',
  pdf_ref         VARCHAR(255) NOT NULL,
  report_hash     CHAR(64) NOT NULL,
  retention_until DATE NOT NULL
);
COMMENT ON TABLE ops.qms_8d_archived_report IS '8D 整改报告归档(15 年留存)';
CREATE INDEX idx_8d_archive_report ON ops.qms_8d_archived_report(org_id, report_id);
CREATE INDEX idx_8d_archive_date ON ops.qms_8d_archived_report(org_id, archive_date);
