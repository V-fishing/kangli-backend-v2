-- 物料变更会签节点增加「指定审批人」:逗号串存 userIds(OR 语义任一可签),用于审批门禁。
-- 门禁:节点绑定了 approver_id 时,当前登录人必须命中其一;为空(历史数据)退化为「有权限者均可签」。
-- 幂等:列已存在则跳过。
ALTER TABLE ops.sqm_change_approval ADD COLUMN IF NOT EXISTS approver_id character varying(1024);
