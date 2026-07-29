-- V73: 检验标准项新增"合格值"字段(枚举型自动判定用)
-- 判定规则: 数值型用 std_value + tolerance; 枚举型用 enum_values(候选项) + pass_values(合格值)。

ALTER TABLE ops.fia_insp_std_item ADD COLUMN IF NOT EXISTS pass_values VARCHAR(128);

COMMENT ON COLUMN ops.fia_insp_std_item.pass_values IS '合格值(逗号分隔);枚举型实测命中即判合格,否则不合格';
