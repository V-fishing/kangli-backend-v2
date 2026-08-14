-- 纠正措施主表新增"责任人用户 ID"字段,支撑按当前登录用户聚合"我的任务"(与 V150 8D 对齐)。
-- owner(姓名文本)已在建表时落地,此处补 ID 作为一等公民,后续改派/聚合复用。
ALTER TABLE ops.ncm_corrective_action ADD COLUMN IF NOT EXISTS owner_user_id VARCHAR(36);

-- 历史数据兜底(两层回退,优先指派记录,其次姓名反查):
-- 1) 从指派记录回填(发起 CA 时已写 qms_assign_record, biz_type='CA')。
UPDATE ops.ncm_corrective_action ca
SET owner_user_id = a.assignee_user_id
FROM ops.qms_assign_record a
WHERE ca.owner_user_id IS NULL
  AND a.biz_type = 'CA'
  AND a.biz_id = ca.id::text
  AND a.assignee_user_id IS NOT NULL;

-- 2) 仅按 owner 姓名反查 sys_user.real_name 兜底(覆盖无指派记录的历史数据)。
UPDATE ops.ncm_corrective_action ca
SET owner_user_id = u.id::text
FROM ops.sys_user u
WHERE ca.owner_user_id IS NULL
  AND ca.owner IS NOT NULL
  AND TRIM(ca.owner) <> ''
  AND u.real_name = TRIM(ca.owner);

COMMENT ON COLUMN ops.ncm_corrective_action.owner_user_id IS '纠正措施责任人用户 ID(对应 ops.sys_user.id)';
