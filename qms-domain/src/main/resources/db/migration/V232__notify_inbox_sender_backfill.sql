-- 回填历史「站内弹窗」投递明细的发送人: 旧逻辑 insert() 未写入 sender,
-- 导致通知中心该批站内信显示发送人为「—」。这些站内信由系统流程/业务发起,
-- 无明确自然人 sender, 统一标记为「系统通知」以保持展示完整。
-- 幂等: 仅更新 channel_type='inbox' 且 sender_name 为空的行。
UPDATE ops.notify_message
SET sender_name = '系统通知'
WHERE channel_type = 'inbox'
  AND (sender_name IS NULL OR sender_name = '');
