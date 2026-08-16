-- V49:审核会签配置表 + 默认配置。
-- 原先会签人员(质量/采购/研发 + 质量一票否决)是写死的;改为按审核类型可配置,
-- 由管理员在「审核人员配置」页面维护。仅"物料变更审核"的质量主管具一票否决权,其他类型无。
CREATE TABLE IF NOT EXISTS ops.sqm_audit_approval_cfg (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id      UUID,
  audit_type  VARCHAR(64) NOT NULL,
  auditors    TEXT NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  VARCHAR(64),
  updated_by  VARCHAR(64),
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  version     INT NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sqm_audit_approval_cfg IS '审核会签配置:按审核类型配置会签人员与否决权';

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000001', '物料变更审核',
       '[{"role":"zhiliangzg","label":"质量主管","veto":true},{"role":"caigou","label":"采购","veto":false},{"role":"yanfa","label":"研发","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='物料变更审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000002', '资质审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='资质审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000003', '年度审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='年度审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000004', '季度审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='季度审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000005', '来料异常审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='来料异常审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000006', '临时审核',
       '[{"role":"zhiliang","label":"质量","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='临时审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000007', '重大来料异常审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='重大来料异常审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000008', '供应商准入审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='供应商准入审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-000000000009', '年度复审',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='年度复审');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000a', '过程审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='过程审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000b', '专项审核',
       '[{"role":"zhiliang","label":"质量","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='专项审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000c', '飞行检查',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='飞行检查');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000d', '初次审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='初次审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000e', '附加审核',
       '[{"role":"zhiliang","label":"质量","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='附加审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT 'f0000000-0000-0000-0000-00000000000f', '重新审核',
       '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='重新审核');
