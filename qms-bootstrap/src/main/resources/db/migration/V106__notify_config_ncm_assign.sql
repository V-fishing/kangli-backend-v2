-- V106: 统一通知配置 - 不良/来料异常发起指派(ncm_assign)
-- 供 不良管理(NCM)与来料异常(SQM) 发起 8D/CAPA/CA 指派弹窗的只读默认通知渠道
-- (assignCandidates 返回的 channels 即取自该事件的配置, 前端弹窗只读展示并提交)
INSERT INTO ops.notify_config (module, event_code, event_name, role_codes, channels, enabled)
SELECT 'ncm', 'ncm_assign', '不良/来料异常发起指派', 'qmanager,sqe', '站内弹窗', true
WHERE NOT EXISTS (
    SELECT 1 FROM ops.notify_config WHERE module = 'ncm' AND event_code = 'ncm_assign'
);
