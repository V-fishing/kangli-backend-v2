-- FMEA 风险项：措施实施后重评 RPN（二次 RPN），用于对比措施有效性
-- rpn_after = 重评 S × 重评 O × 重评 D；为 NULL 表示未重评。
ALTER TABLE ops.qms_fmea_risk ADD COLUMN IF NOT EXISTS rpn_after smallint;

COMMENT ON COLUMN ops.qms_fmea_risk.rpn_after IS '措施实施后重评 RPN(二次 RPN),用于对比措施有效性;NULL 表示未重评';
