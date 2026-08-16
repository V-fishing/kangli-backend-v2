-- 抽样批次任务表补充业务元信息列,对齐首件任务创建表单字段(触发类型/品类/供应商/加急/备注)
-- 抽样任务支持「同一工单号(产品)下挂多个参数」:param_id 仍单值,多条记录共享 wo_no。
-- 这些列均为可选,存量记录默认 NULL,不影响既有逻辑。

ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS trigger_type  VARCHAR(64);   -- 触发类型(如 量产/换线/客诉)
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS category      VARCHAR(32);   -- 品类 material/semi/product
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS supplier_id   VARCHAR(64);   -- 供应商 UUID(物料类首件必填)
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(128);  -- 供应商名称(冗余,展示用)
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS is_urgent     BOOLEAN NOT NULL DEFAULT FALSE; -- 加急
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS remark        VARCHAR(512);  -- 备注

CREATE INDEX IF NOT EXISTS idx_spctask_trigger ON ops.spc_sample_task (trigger_type);
CREATE INDEX IF NOT EXISTS idx_spctask_category ON ops.spc_sample_task (category);
CREATE INDEX IF NOT EXISTS idx_spctask_supplier ON ops.spc_sample_task (supplier_id);
