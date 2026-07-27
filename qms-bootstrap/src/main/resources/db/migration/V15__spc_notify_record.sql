-- V15: SPC 推送通知记录(报警触发后按启用渠道生成,留痕)
CREATE TABLE ops.spc_notify_record (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  alarm_id     UUID NOT NULL REFERENCES ops.spc_alarm(id),
  channel      VARCHAR(16) NOT NULL,
  channel_name VARCHAR(32),
  message      TEXT NOT NULL,
  status       VARCHAR(16) NOT NULL,
  sent_at      TIMESTAMPTZ,
  error        TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_spc_notify_record_alarm ON ops.spc_notify_record(org_id, alarm_id, created_at DESC);
COMMENT ON TABLE ops.spc_notify_record IS 'SPC 推送通知记录(报警触发,留痕)';
