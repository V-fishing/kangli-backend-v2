-- V48: 清空上一版以中文作为 approval_role 的会签记录(中文在 WHERE 参数比对中会因编码失配)。
-- 改为 ASCII 角色码(roleLabel 仍保留中文展示),清空后按新的 seedDefaultApprovals 惰性重建。
DELETE FROM ops.sqm_audit_approval;
