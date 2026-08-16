-- V53: 回填历史审核组(auditor_team)为空的计划(主要为年度复审/临时审核),
-- 使列表「审核组」栏不再为空,并与详情会签链的兜底人员(质量经理,采购主管)保持一致。
UPDATE ops.sqm_audit_plan
SET auditor_team = '质量经理,采购主管'
WHERE auditor_team IS NULL OR auditor_team = '';
