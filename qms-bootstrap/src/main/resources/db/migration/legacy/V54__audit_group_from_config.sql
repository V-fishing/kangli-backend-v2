-- V54: V53 用兜底值(质量经理,采购主管)回填了空审核组,但「审核人员配置」(会签配置,V49 写入)
-- 已为各审核类型定义了默认会签人。此处把被错误回填的计划改回配置值,使列表「审核组」栏
-- 与详情页会签链(同样源自配置)完全一致。仅修正 V53 写入的兜底值,不影响其它数据。
UPDATE ops.sqm_audit_plan
SET auditor_team = '质量,采购,SQE'
WHERE audit_type = '年度复审' AND auditor_team = '质量经理,采购主管';

UPDATE ops.sqm_audit_plan
SET auditor_team = '质量'
WHERE audit_type = '临时审核' AND auditor_team = '质量经理,采购主管';
