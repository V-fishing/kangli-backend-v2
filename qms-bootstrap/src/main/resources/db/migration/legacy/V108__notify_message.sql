-- V108: 点对点通知发送记录表
-- 记录每次点对点通知的发送人/接收人/渠道/状态/失败原因/关联单据, 供通知中心展示与追溯。
CREATE TABLE IF NOT EXISTS ops.notify_message (
  id             UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id         UUID,
  sender_id      UUID,
  sender_name    VARCHAR(64),
  receiver_id    UUID,
  receiver_name  VARCHAR(64),
  receiver_type  VARCHAR(16) DEFAULT 'user',      -- user / role(预留)
  channel        VARCHAR(32) NOT NULL,            -- 渠道名, 如 钉钉应用消息 / 邮件
  channel_type   VARCHAR(16) DEFAULT 'direct',    -- 渠道类型: direct / webhook(预留)
  title          VARCHAR(256),
  content        TEXT,
  biz_type       VARCHAR(32),                     -- 8D / CAPA / CA / MANUAL 等
  biz_id         VARCHAR(64),
  biz_no         VARCHAR(64),
  status         VARCHAR(16) NOT NULL DEFAULT '发送中', -- 发送中 / 成功 / 失败
  fail_reason    VARCHAR(512),
  send_time      TIMESTAMPTZ DEFAULT now(),
  created_at     TIMESTAMPTZ DEFAULT now(),
  updated_at     TIMESTAMPTZ DEFAULT now(),
  created_by     UUID,
  updated_by     UUID,
  is_deleted     BOOLEAN NOT NULL DEFAULT false,
  version        INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.notify_message IS '点对点通知发送记录';

CREATE INDEX IF NOT EXISTS idx_notify_message_receiver ON ops.notify_message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_notify_message_create ON ops.notify_message(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notify_message_status ON ops.notify_message(status);
