-- 8D 报告主表新增"负责人用户 ID"字段,支撑按当前登录用户聚合"我的任务"。
-- owner_user_name(姓名)已在 V103 落地,此处补 ID 作为一等公民,后续审批链/聚合复用。
ALTER TABLE ops.qms_8d_report ADD COLUMN IF NOT EXISTS owner_user_id VARCHAR(36);

-- 历史数据兜底:从指派记录回填负责人 ID(8D 发起时写入 qms_assign_record.assignee_user_id)。
-- qms_assign_record.biz_id 是 VARCHAR(36),qms_8d_report.id 是 UUID,需显式 cast。
UPDATE ops.qms_8d_report r
SET owner_user_id = a.assignee_user_id
FROM ops.qms_assign_record a
WHERE r.owner_user_id IS NULL
  AND a.biz_type = '8D'
  AND a.biz_id::uuid = r.id
  AND a.assignee_user_id IS NOT NULL;

COMMENT ON COLUMN ops.qms_8d_report.owner_user_id IS '8D 报告负责人用户 ID(对应 ops.sys_user.id)';
