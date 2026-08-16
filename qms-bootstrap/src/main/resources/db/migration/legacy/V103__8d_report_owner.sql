-- 8D 报告主表新增"负责人"字段,列表页直接展示责任人,无需额外 join 阶段明细。
ALTER TABLE ops.qms_8d_report ADD COLUMN IF NOT EXISTS owner_user_name VARCHAR(64);

-- 历史数据兜底:若主表 owner_user_name 为空,但 D1 阶段已写入 owner,则回填到主表。
UPDATE ops.qms_8d_report r
SET owner_user_name = s.owner
FROM ops.qms_8d_stage_detail s
WHERE r.owner_user_name IS NULL
  AND s.stage_code = 'D1'
  AND s.owner IS NOT NULL
  AND s.d8_id = r.id;

COMMENT ON COLUMN ops.qms_8d_report.owner_user_name IS '8D 报告负责人(姓名)';
