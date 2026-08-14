-- V140: spc_subgroup 增加 stage(阶段)维度,区分首件能力验证(FIRST)与量产过程监控(ROUTINE)
-- 背景:原先首件联动(FIA->SPC)与量产抽样数据混存于同一张子组表,无阶段语义区分,
-- 用户无法分辨"首件一次性能力验证点"与"量产持续监控点"。引入 stage 维度后在
-- 录入/控制图/看板三层贯穿,形成两条独立流程。
--
-- 备注:spc_subgroup 为 RANGE 分区表(按 subgroup_time)。PostgreSQL 12+ 对
-- ADD COLUMN ... DEFAULT 'ROUTINE' NOT NULL 采用"快速加列"(重写元数据、不重写全表),
-- 无长时间锁风险。历史数据天然归为 ROUTINE(此前无首件标记)。

ALTER TABLE ops.spc_subgroup
  ADD COLUMN IF NOT EXISTS stage VARCHAR(8) NOT NULL DEFAULT 'ROUTINE';

COMMENT ON COLUMN ops.spc_subgroup.stage IS
  'SPC 数据阶段: FIRST=首件能力验证(点状/一次性,由FIA任务联动产生); ROUTINE=量产过程监控(线状/持续,由来料/追溯页量产抽样入口产生)';

-- 阶段维度查询索引:按 组织+参数+阶段+时间 过滤控制图/看板
CREATE INDEX IF NOT EXISTS idx_spc_sub_stage
  ON ops.spc_subgroup (org_id, param_id, stage, subgroup_time DESC);

-- 同步新增 measurement 子表阶段冗余列(仅供按阶段聚合告警/能力时快速关联,可选)
-- spc_measurement 通过 subgroup_id 关联 spc_subgroup,无需单独 stage 列。
