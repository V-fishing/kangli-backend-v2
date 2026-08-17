-- 通知中心聚合需要「可读业务单据号」(biz_no)。
-- 1) sys_notification 收件箱主表补 biz_no 列(此前仅 notify_message 有)。
-- 2) 历史站内弹窗投递明细(inbox)的 biz_no 此前被兜底写成业务主键 UUID(无可读意义),
--    统一清空, 使通知中心回退展示为「—」, 与新数据(调用方传入真实单号)保持一致。
ALTER TABLE ops.sys_notification ADD COLUMN IF NOT EXISTS biz_no character varying(64);

UPDATE ops.notify_message
SET biz_no = NULL
WHERE channel_type = 'inbox'
  AND biz_no IS NOT NULL
  AND biz_no NOT SIMILAR TO '%[A-Za-z]{2,}-%';  -- 仅清掉「纯 UUID/无前缀」的兜底值, 保留已有真实单号
