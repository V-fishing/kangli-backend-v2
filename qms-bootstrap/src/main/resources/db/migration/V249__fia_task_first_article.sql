-- ============================================================
-- V249 完工检验「先建单、后绑首件」: 持久化完工检验单↔首件 1:1 绑定
-- 1) fia_task 补 first_article_id 列(可空, 存量数据不受影响)
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 完工检验单绑定的首件任务 ID(建单可选绑, 放行前必须非空)
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS first_article_id varchar(64);

COMMENT ON COLUMN ops.fia_task.first_article_id IS '关联首件任务ID(完工检验单↔首件1:1绑定,放行前须非空)';
