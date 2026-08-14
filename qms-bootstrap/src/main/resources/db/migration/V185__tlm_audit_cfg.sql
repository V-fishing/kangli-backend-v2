-- V185: 工装管理(TLM)审核配置种子(跨端协同强约束 C2)。
-- 工装报废 / 工装维修 的默认审批人节点, 供 TlmToolingServiceImpl.resolveScrapApprovers /
-- resolveRepairApprovers 在发起报废 / 送修时读取。缺这两个节点时流程会降级为「无审批人直过」,
-- 导致审批中心收不到工装待办。全部幂等(按 audit_type 去重)。
-- 默认审批人沿用 MZ-质量经理(与已落地的 工装报废审核 节点一致)。
INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT ops.gen_uuid_v7(), '工装报废审核',
       '[{"role":"zhiliang","label":"MZ-质量经理","veto":false,"userId":"a9d66060-9fa3-d966-d945-9a3b1337b2fc","userIds":["a9d66060-9fa3-d966-d945-9a3b1337b2fc"]}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='工装报废审核');

INSERT INTO ops.sqm_audit_approval_cfg (id, audit_type, auditors)
SELECT ops.gen_uuid_v7(), '工装维修审核',
       '[{"role":"zhiliang","label":"MZ-质量经理","veto":false,"userId":"a9d66060-9fa3-d966-d945-9a3b1337b2fc","userIds":["a9d66060-9fa3-d966-d945-9a3b1337b2fc"]}]'
WHERE NOT EXISTS (SELECT 1 FROM ops.sqm_audit_approval_cfg WHERE audit_type='工装维修审核');
