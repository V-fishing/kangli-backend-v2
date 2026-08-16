-- V76: 归一化历史 fia_insp_std_item.value_type 中文→英文(numeric/enum/text)
-- 旧数据用 '数值'/'文本'/'枚举'，而前后端判定逻辑按英文 'numeric'/'enum' 匹配，
-- 导致历史标准项既不被判为"可匹配"(覆盖率≈0)，后端录入也不自动判定。
-- 仅为 fia_insp_std_item.value_type 归一(该表存在此列);fia_insp_item 无此物理列,不处理。幂等可重复执行。

UPDATE ops.fia_insp_std_item
SET value_type = CASE value_type
    WHEN '数值' THEN 'numeric'
    WHEN '枚举' THEN 'enum'
    WHEN '文本' THEN 'text'
    ELSE value_type
END
WHERE value_type IN ('数值', '枚举', '文本');
