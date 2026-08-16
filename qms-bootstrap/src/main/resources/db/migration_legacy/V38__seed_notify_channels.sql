-- V38__seed_notify_channels.sql  
-- 通知渠道种子: 管理员可通过 /api/v1/spc/notify-channels 增删改

INSERT INTO ops.spc_notify_channel (channel, is_enabled, config_json) 
SELECT '站内弹窗', true, '{}'
WHERE NOT EXISTS (SELECT 1 FROM ops.spc_notify_channel WHERE channel='站内弹窗');

INSERT INTO ops.spc_notify_channel (channel, is_enabled, config_json)
SELECT '企业微信', false, '{"webhook":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.spc_notify_channel WHERE channel='企业微信');

INSERT INTO ops.spc_notify_channel (channel, is_enabled, config_json)
SELECT '钉钉', false, '{"webhook":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.spc_notify_channel WHERE channel='钉钉');

INSERT INTO ops.spc_notify_channel (channel, is_enabled, config_json)
SELECT '自定义Webhook', false, '{"webhook":""}'
WHERE NOT EXISTS (SELECT 1 FROM ops.spc_notify_channel WHERE channel='自定义Webhook');
