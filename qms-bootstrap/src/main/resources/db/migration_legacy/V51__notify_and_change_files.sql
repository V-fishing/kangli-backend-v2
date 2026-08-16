-- 全平台站内消息通知中心 + 物料变更评估资料附件字段
CREATE TABLE IF NOT EXISTS ops.sys_notification (
    id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    org_id      UUID,
    user_id     VARCHAR(64) NOT NULL,
    user_name   VARCHAR(64),
    title       VARCHAR(255) NOT NULL,
    content     TEXT,
    biz_type    VARCHAR(32),
    biz_id      VARCHAR(64),
    link        VARCHAR(255),
    is_read     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_sys_notification_user ON ops.sys_notification (user_id, is_read);

ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS verify_report TEXT;
ALTER TABLE ops.sqm_change_order ADD COLUMN IF NOT EXISTS risk_file TEXT;
COMMENT ON COLUMN ops.sqm_change_order.verify_report IS '验证报告附件路径(相对 logs/files)';
COMMENT ON COLUMN ops.sqm_change_order.risk_file IS '风险评估附件路径(相对 logs/files)';
