-- V149: 幂等回填 spc_param.chart_candidates / chart_type 默认控制图类型。
-- 背景: 控制图页按 chart_candidates 渲染(数值型=Xbar-R 即 Xbar 卡+R 卡, 枚举/文本=P 卡)。
--        历史参数可能 chart_candidates 为空/旧值, 导致实际图与配置对不上。
-- 仅回填 NULL/空 的行, 已正确配置的不覆盖(幂等可重复执行)。
-- spc_param 无 value_type 列, 通过 fia_std_item_id JOIN fia_insp_std_item.value_type 判类型;
-- 无法关联标准项的(手动创建等)兜底按数值型 'Xbar-R' 处理(绝大多数 SPC 参数为计量型)。

-- 1) chart_candidates 回填
UPDATE ops.spc_param p
SET chart_candidates = CASE
    WHEN i.value_type IN ('enum', 'text') THEN 'P'
    ELSE 'Xbar-R'
END
FROM ops.fia_insp_std_item i
WHERE p.fia_std_item_id = i.id
  AND (p.chart_candidates IS NULL OR p.chart_candidates = '');

-- 2) 无法关联标准项(NULL fia_std_item_id)的参数, 兜底 Xbar-R
UPDATE ops.spc_param
SET chart_candidates = 'Xbar-R'
WHERE (chart_candidates IS NULL OR chart_candidates = '')
  AND fia_std_item_id IS NULL;

-- 3) chart_type 主图同步回填(取 chart_candidates 第一个, 兼容历史主图字段)
UPDATE ops.spc_param
SET chart_type = CASE
    WHEN chart_candidates LIKE '%,%' THEN split_part(chart_candidates, ',', 1)
    ELSE chart_candidates
END
WHERE (chart_type IS NULL OR chart_type = '')
  AND chart_candidates IS NOT NULL AND chart_candidates <> '';
