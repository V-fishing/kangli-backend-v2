-- 拆分混挂计数参数「外观」(id=72a365cc, chart_candidates='P,NP,U'):
-- 该参数同时挂了 P/NP(不合格品数) 与 U(缺陷数) 两类口径,但实际子组录入口径不一致:
--   子组1、2 只填了 nonconforming(不合格品数) -> 只进 P/NP 图;
--   子组3 只填了 defect_count(缺陷数) -> 只进 U 图。
-- 导致「计数型过程水平」卡片内 p̄/PPM(不合格品口径) 与 ū/DPU(缺陷口径) 样本对不齐、统计口径混乱。
--
-- 拆分策略:
--   1) 原参数收敛为纯 P/NP: chart_candidates='P,NP'(chart_type 本就是 P, data_type 保持 ATTRIBUTE)。
--      子组1、2(nonconforming 数据) 已归属原参数,无需改动。
--   2) 新建纯 C/U 参数「外观（缺陷数）」(固定 UUID, 便于幂等与子组归属),
--      复制原参数的组织/工序/规格/σ 等上下文, chart_candidates='C,U', chart_type='C';
--      param_source 清空(脱钩 FIA 首件联动,避免 FIA 重复建档), 仅作人工维护的统计口径。
--   3) 将原参数下「缺陷数型」子组(nonconforming IS NULL AND defect_count IS NOT NULL) 改挂到新 C/U 参数。
--
-- 幂等:
--   * 原参数收敛用 IS DISTINCT FROM, 重复执行 UPDATE 0 行。
--   * 新参数用固定 UUID + WHERE NOT EXISTS, 只插入一次。
--   * 子组迁移仅匹配「仍属原参数且为缺陷数型」的行, 已迁移行 param_id 已变不再命中。

-- 1) 原参数收敛为 P/NP
UPDATE ops.spc_param
SET chart_candidates = 'P,NP',
    updated_at = NOW()
WHERE id = '72a365cc-54e5-53e2-b9b6-a68f0ad0f482'
  AND (chart_candidates IS DISTINCT FROM 'P,NP');

-- 2) 新建 C/U 参数(复制原参数上下文, 仅改图类型与名称/来源)
INSERT INTO ops.spc_param (
    id, org_id, param_name, proc_name, data_type, chart_candidates, chart_type,
    spec_upper, spec_lower, spec_text, target_value, subgroup_size, collect_freq,
    sigma_method, sigma_k, unit, is_active, created_at, created_by, updated_at, updated_by,
    is_deleted, version, chartable, param_source, src_wo_no, src_batch_no, process_id,
    cpk_period, supplier_id, fia_std_item_id, spec_standard_id, src_item_id
)
SELECT
    '72a365cc-aa01-53e2-b9b6-aa00cc000001'::uuid,
    p.org_id,
    '外观（缺陷数）',
    p.proc_name,
    'ATTRIBUTE',
    'C,U',
    'C',
    p.spec_upper, p.spec_lower, p.spec_text, p.target_value, p.subgroup_size, p.collect_freq,
    p.sigma_method, p.sigma_k, p.unit,
    p.is_active,
    NOW(), p.created_by, NOW(), p.created_by,
    p.is_deleted, 0,
    p.chartable,
    'MANUAL',                  -- 脱钩 FIA 首件联动(避免重复建档), 且满足 NOT NULL 约束
    NULL, NULL, p.process_id, -- src_wo_no/src_batch_no 清空: 不再溯源原 FIA 首件, 避免首件溯源键冲突
    p.cpk_period, p.supplier_id, p.fia_std_item_id, p.spec_standard_id, NULL  -- src_item_id 清空: 避开 uq_spc_param_src_item 唯一约束
FROM ops.spc_param p
WHERE p.id = '72a365cc-54e5-53e2-b9b6-a68f0ad0f482'
  AND NOT EXISTS (
    SELECT 1 FROM ops.spc_param x
    WHERE x.id = '72a365cc-aa01-53e2-b9b6-aa00cc000001'::uuid
  );

-- 3) 缺陷数型子组改挂到新 C/U 参数
UPDATE ops.spc_subgroup
SET param_id = '72a365cc-aa01-53e2-b9b6-aa00cc000001'::uuid
WHERE param_id = '72a365cc-54e5-53e2-b9b6-a68f0ad0f482'
  AND nonconforming IS NULL
  AND defect_count IS NOT NULL;
