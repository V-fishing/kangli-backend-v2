-- FMEA 风险项：措施实施后重评 S/O/D 三值，用于详情展示重评明细
-- 与 rpn_after 配套：闭环时若重评 S/O/D 三项同时填写，则同时落库三值 + rpn_after；否则均为 NULL(未重评)。
ALTER TABLE ops.qms_fmea_risk ADD COLUMN IF NOT EXISTS reseval_severity smallint;
ALTER TABLE ops.qms_fmea_risk ADD COLUMN IF NOT EXISTS reseval_occurrence smallint;
ALTER TABLE ops.qms_fmea_risk ADD COLUMN IF NOT EXISTS reseval_detection smallint;

COMMENT ON COLUMN ops.qms_fmea_risk.reseval_severity IS '措施实施后重评严重度 S(1-10),NULL 表示未重评';
COMMENT ON COLUMN ops.qms_fmea_risk.reseval_occurrence IS '措施实施后重评频度 O(1-10),NULL 表示未重评';
COMMENT ON COLUMN ops.qms_fmea_risk.reseval_detection IS '措施实施后重评探测度 D(1-10),NULL 表示未重评';
