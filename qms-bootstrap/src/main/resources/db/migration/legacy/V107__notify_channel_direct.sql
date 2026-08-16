-- V107: 通知渠道扩展 - 点对点(direct)渠道
-- 1) ops.notify_channel 增加 channel_type(webhook=群机器人 / direct=点对点) 与 config_json(渠道凭据 JSON)
-- 2) 幂等插入 4 个点对点渠道种子: 钉钉应用消息 / 企业微信应用消息 / 邮件 / 短信
-- 说明: 存量行 channel_type 默认 'webhook', 兼容既有群机器人渠道; config_json 中 secret 字段在接口返回时脱敏。
-- 说明: sys_user.email/phone 列已在 V01 建表时存在, 无需 ALTER。

ALTER TABLE ops.notify_channel
  ADD COLUMN IF NOT EXISTS channel_type VARCHAR(16) NOT NULL DEFAULT 'webhook';
ALTER TABLE ops.notify_channel
  ADD COLUMN IF NOT EXISTS config_json TEXT;
COMMENT ON COLUMN ops.notify_channel.channel_type IS '渠道类型: webhook=群机器人 / direct=点对点';
COMMENT ON COLUMN ops.notify_channel.config_json IS '渠道凭据 JSON(secret 字段返回时脱敏)';

-- 存量 webhook 渠道置为 webhook 类型(幂等)
UPDATE ops.notify_channel SET channel_type = 'webhook' WHERE channel_type IS NULL OR channel_type = '';

-- ============ 点对点渠道种子(幂等) ============
INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark, channel_type, config_json)
SELECT '钉钉应用消息', '', false, '普通', '钉钉工作通知(应用消息, 点对点)',
       'direct', '{"type":"dingtalk","appKey":"","appSecret":"","agentId":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='钉钉应用消息');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark, channel_type, config_json)
SELECT '企业微信应用消息', '', false, '普通', '企业微信应用消息(点对点)',
       'direct', '{"type":"wecom","corpId":"","agentId":"","secret":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='企业微信应用消息');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark, channel_type, config_json)
SELECT '邮件', '', false, '普通', '邮件(SMTP, 点对点)',
       'direct', '{"type":"mail","host":"","port":465,"username":"","password":"","from":"","ssl":true}'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='邮件');

INSERT INTO ops.notify_channel (channel, webhook_url, is_enabled, level, remark, channel_type, config_json)
SELECT '短信', '', false, '普通', '短信(预留适配器, 未配置服务商时发送记录失败)',
       'direct', '{"type":"sms","provider":"","appKey":"","appSecret":"","signName":"","templateCode":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.notify_channel WHERE channel='短信');
