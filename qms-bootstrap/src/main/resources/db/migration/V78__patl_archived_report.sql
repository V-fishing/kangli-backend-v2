-- 巡检任务归档报告(15 年留存,无审计字段)
CREATE TABLE ops.patl_archived_report (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  archive_no      VARCHAR(32) NOT NULL UNIQUE,
  task_id         UUID NOT NULL REFERENCES ops.patl_task(id),
  task_no         VARCHAR(32) NOT NULL,
  route_id        UUID,
  archive_date    DATE NOT NULL,
  status          VARCHAR(16) NOT NULL DEFAULT '已归档',
  pdf_ref         VARCHAR(255) NOT NULL,
  report_hash     CHAR(64) NOT NULL,
  retention_until DATE NOT NULL
);
COMMENT ON TABLE ops.patl_archived_report IS '巡检任务归档报告(15 年留存)';
CREATE INDEX idx_patl_archive_task ON ops.patl_archived_report(org_id, task_id);
CREATE INDEX idx_patl_archive_date ON ops.patl_archived_report(org_id, archive_date);
