-- V75: fia_task 增加检验项统计与合格率字段
-- 录入环节(enterResults)与归档/放行环节统一刷新,供任务详情与看板展示单任务合格率。

ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS item_total INT DEFAULT 0;
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS pass_count INT DEFAULT 0;
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS fail_count INT DEFAULT 0;
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS pass_rate NUMERIC(5,4) DEFAULT 0;

COMMENT ON COLUMN ops.fia_task.item_total IS '检验项总数';
COMMENT ON COLUMN ops.fia_task.pass_count IS '合格数';
COMMENT ON COLUMN ops.fia_task.fail_count IS '不合格数';
COMMENT ON COLUMN ops.fia_task.pass_rate IS '合格率(比例 0~1,=合格数/检验项总数)';
