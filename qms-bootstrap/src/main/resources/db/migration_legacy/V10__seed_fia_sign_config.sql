-- V10: 种子 FIA 签名配置(梅州/深圳 默认 {password}/两级/整单/lock 3·5min)
INSERT INTO ops.fia_sign_config (org_id, sign_methods, sign_nodes, sign_granularity, lock_after_fail, lock_minutes)
SELECT id, ARRAY['password'], '两级', '整单签名', 3, 5
FROM ops.sys_org
WHERE org_code IN ('MZ','SZ')
ON CONFLICT (org_id) DO NOTHING;
