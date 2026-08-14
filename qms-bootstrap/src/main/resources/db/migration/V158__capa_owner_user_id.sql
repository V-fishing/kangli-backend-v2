-- CAPA 主表新增"负责人用户 ID"字段,支撑按当前登录用户聚合"我的任务"(与 V150 8D 对齐)。
-- owner(姓名文本)已在建表时落地,此处补 ID 作为一等公民,后续改派/聚合复用。
ALTER TABLE ops.qms_capa ADD COLUMN IF NOT EXISTS owner_user_id VARCHAR(36);

-- 历史数据兜底(两层回退,优先指派记录,其次姓名反查):
-- 1) 从指派记录回填(发起 CAPA 时已写 qms_assign_record, biz_type='CAPA')。
UPDATE ops.qms_capa c
SET owner_user_id = a.assignee_user_id
FROM ops.qms_assign_record a
WHERE c.owner_user_id IS NULL
  AND a.biz_type = 'CAPA'
  AND a.biz_id = c.id::text
  AND a.assignee_user_id IS NOT NULL;

-- 2) 仅按 owner 姓名反查 sys_user.real_name 兜底(覆盖无指派记录的历史数据)。
UPDATE ops.qms_capa c
SET owner_user_id = u.id::text
FROM ops.sys_user u
WHERE c.owner_user_id IS NULL
  AND c.owner IS NOT NULL
  AND TRIM(c.owner) <> ''
  AND u.real_name = TRIM(c.owner);

COMMENT ON COLUMN ops.qms_capa.owner_user_id IS 'CAPA 负责人用户 ID(对应 ops.sys_user.id)';
