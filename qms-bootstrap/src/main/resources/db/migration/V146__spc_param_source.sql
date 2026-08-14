-- SPC 参数来源隔离:首件 SPC 与 产品抽样 SPC 两视图必须严格隔离。
-- 新增 param_source 列标记参数来源,前端按此过滤,避免抽样任务创建的参数泄漏进首件 SPC。
-- 取值: SAMPLE(抽样任务流程派生/复制) / FIA_FIRST(首件标准库/首件任务生成) / MANUAL(手动新建)
-- 存量回填规则:
--   被抽样任务引用的            -> SAMPLE
--   带 fia_std_item_id 未被引用 -> FIA_FIRST(首件标准库生成)
--   其余(手动新建/null)         -> MANUAL

ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS param_source VARCHAR(32) NOT NULL DEFAULT 'MANUAL';

-- 先标首件标准库来源(带 fia_std_item_id 且未被抽样任务引用)
UPDATE ops.spc_param
SET param_source = 'FIA_FIRST'
WHERE fia_std_item_id IS NOT NULL
  AND id NOT IN (SELECT param_id FROM ops.spc_sample_task WHERE param_id IS NOT NULL);

-- 再标抽样来源(被抽样任务引用)
UPDATE ops.spc_param
SET param_source = 'SAMPLE'
WHERE id IN (SELECT param_id FROM ops.spc_sample_task WHERE param_id IS NOT NULL);

CREATE INDEX IF NOT EXISTS idx_spcparam_source ON ops.spc_param (param_source);
