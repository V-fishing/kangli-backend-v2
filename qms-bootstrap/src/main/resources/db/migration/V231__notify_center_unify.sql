-- ============================================================================
-- 康立 QMS 数据库结构增量 - 通知中心统一收件网关
-- ----------------------------------------------------------------------------
-- 板块 : 通知 (NOTIFY)
-- 内容 : 让「通知中心」覆盖每一条通知(站内信 + 外发渠道)。
--        1) ops.notify_message 增加 notification_id: 指向 ops.sys_notification.id,
--           使「投递明细」挂到「站内信主记录」, 一对一/一对多关系清晰。
--        2) ops.sys_notification 增加 channel 列: 记录该通知的首选/主渠道,
--           便于通知中心按渠道聚合展示。
-- 说明 : 软引用(不加 FK 约束)——手动外发场景可能没有 sys_notification 主记录,
--        历史数据 notification_id 为空也属正常。幂等可重复执行。
-- 依赖 : V016 (notify_message / sys_notification 已存在)
-- ============================================================================

-- 1) notify_message.notification_id (指向 sys_notification.id)
ALTER TABLE ops.notify_message
    ADD COLUMN IF NOT EXISTS notification_id uuid;

COMMENT ON COLUMN ops.notify_message.notification_id IS
    '关联站内信主记录 ops.sys_notification.id; 站内弹窗/外发明细均挂到对应主记录, 使通知中心覆盖每一条通知';

CREATE INDEX IF NOT EXISTS idx_notify_message_notif
    ON ops.notify_message USING btree (notification_id);

-- 2) sys_notification.channel (主渠道/首选渠道)
ALTER TABLE ops.sys_notification
    ADD COLUMN IF NOT EXISTS channel character varying(32);

COMMENT ON COLUMN ops.sys_notification.channel IS
    '通知首选渠道(如 站内弹窗 / 钉钉应用消息); 通知中心聚合投递明细时作为主渠道展示';
