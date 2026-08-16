-- V147: 检验标准项新增「控制图类型」推荐集合,首件任务带入 SPC 参数时按类型自动匹配控制图。
-- 背景: 检验项明细已有「类型」(value_type: numeric/enum/text),但缺乏控制图语义,
--        导致 ensureFromFiaTask 写死 Xbar-R,计数型/外观型被错误当成计量型处理。
-- 设计: fia_insp_std_item.chart_types 存逗号分隔的推荐图集合(如 'Xbar-R,I-MR'),标准库一对多;
--        spc_param.chart_candidates 存带入时的推荐全集,供参数页编辑弹窗限定 chartType 可选范围。

-- 1) 检验标准项:推荐控制图集合(可空,前端按 value_type 自动给默认)
ALTER TABLE ops.fia_insp_std_item ADD COLUMN IF NOT EXISTS chart_types VARCHAR(64);
COMMENT ON COLUMN ops.fia_insp_std_item.chart_types
    IS '推荐控制图类型集合(逗号分隔): 数值→Xbar-R(可加Xbar-S/I-MR), 枚举/文本→P(可加NP/C/U)';

-- 2) SPC 参数: 带入时的推荐全集(可空)
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS chart_candidates VARCHAR(64);
COMMENT ON COLUMN ops.spc_param.chart_candidates
    IS '带入时的推荐控制图全集(逗号分隔),编辑弹窗据此限定 chartType 可选范围;空则用全量';

-- 3) 存量幂等回填: 按 value_type 给推荐集合, 避免带入时主图缺省
--    数值→Xbar-R; 枚举/文本→P(不良率趋势)。仅回填为空的行, 已配置的不覆盖。
UPDATE ops.fia_insp_std_item
SET chart_types = CASE
    WHEN value_type = 'enum' THEN 'P'
    WHEN value_type = 'text' THEN 'P'
    ELSE 'Xbar-R'
END
WHERE chart_types IS NULL;
