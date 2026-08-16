-- 抽样批次任务表 + 子组关联抽样任务列(SPC 量产抽样采集载体)
-- 与首件(FIA 任务带入)平行:抽样任务手动创建后,在其下列表跳转采集多批子组。

-- 抽样批次任务:工单×料号×工序三元组归集,标准自动带出 SPC 参数
-- id / param_id 均为 uuid(与 spc_param.id 一致),sample_task_id 同为 uuid(与 spc_sample_task.id 一致)
CREATE TABLE IF NOT EXISTS ops.spc_sample_task (
    id              UUID PRIMARY KEY,
    org_id          UUID,
    wo_no           VARCHAR(64) NOT NULL,
    part_no         VARCHAR(64) NOT NULL,
    proc_name       VARCHAR(64),
    product_name    VARCHAR(128),
    param_id        UUID NOT NULL,
    target_count    INTEGER     NOT NULL DEFAULT 1,
    current_count   INTEGER     NOT NULL DEFAULT 0,
    status          VARCHAR(16) NOT NULL DEFAULT '采集中',
    cpk             NUMERIC(6,4),
    released        BOOLEAN     NOT NULL DEFAULT FALSE,
    alarm_flag      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by      VARCHAR(32),
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_spctask_param FOREIGN KEY (param_id) REFERENCES ops.spc_param (id)
);
CREATE INDEX IF NOT EXISTS idx_spctask_org ON ops.spc_sample_task (org_id);
CREATE INDEX IF NOT EXISTS idx_spctask_wo ON ops.spc_sample_task (wo_no);
CREATE INDEX IF NOT EXISTS idx_spctask_param ON ops.spc_sample_task (param_id);

-- spc_subgroup 增加抽样任务关联列(首件仍用 task_id;抽样用 sample_task_id)
ALTER TABLE ops.spc_subgroup ADD COLUMN IF NOT EXISTS sample_task_id UUID;
CREATE INDEX IF NOT EXISTS idx_subgroup_sample_task ON ops.spc_subgroup (sample_task_id);
