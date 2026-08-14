-- V110: 通知配置支持"指定具体接收人"
-- ops.notify_config 增加 receiver_ids(逗号分隔用户ID, 与 role_codes 并存取并集)
-- 说明: 角色仍按原逻辑解析全部成员, 具体接收人精确到个人; 站内弹窗与点对点渠道均按最终接收人发送。
ALTER TABLE ops.notify_config ADD COLUMN IF NOT EXISTS receiver_ids TEXT;
COMMENT ON COLUMN ops.notify_config.receiver_ids IS '具体接收人用户ID(逗号分隔, 可空); 与 role_codes 并存取并集';
