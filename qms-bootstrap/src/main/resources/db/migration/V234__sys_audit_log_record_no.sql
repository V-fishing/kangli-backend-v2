-- V234__sys_audit_log_record_no.sql
-- 审计日志新增可读业务单号列 record_no, 使前端「业务ID」列优先展示可读单号(如 8D-/DF-/CAPA-),
-- 而非纯 UUID 主键(record_id 仍保留作精确关联键, 不动)。历史数据 record_no 为 NULL, 前端回退显示 record_id。

ALTER TABLE ops.sys_audit_log ADD COLUMN IF NOT EXISTS record_no VARCHAR(64);
COMMENT ON COLUMN ops.sys_audit_log.record_no IS '被操作记录的可读业务单号(8D-/DF-/CAPA- 等), 由 @Auditable(recordNoExpr) 写入, 可为空';

CREATE INDEX IF NOT EXISTS idx_audit_record_no ON ops.sys_audit_log(record_no);
