-- 抽样(SAMPLE)参数回填来源工单号 src_wo_no:
-- 抽样参数由抽样任务创建,但其 src_wo_no 此前未写入,导致控制图页无法按工单聚拢抽样参数。
-- 从关联的抽样任务 spc_sample_task.wo_no 取最新一条回填;已带工单号(含被抽样复用、保留首件工单号)的参数不覆盖。
UPDATE ops.spc_param p
SET src_wo_no = (
    SELECT t.wo_no FROM ops.spc_sample_task t
    WHERE t.param_id = p.id
    ORDER BY t.created_at DESC
    LIMIT 1
)
WHERE p.param_source = 'SAMPLE'
  AND (p.src_wo_no IS NULL OR p.src_wo_no = '')
  AND EXISTS (SELECT 1 FROM ops.spc_sample_task t WHERE t.param_id = p.id);
