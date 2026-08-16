-- 8D 阶段明细的审批人字段原定义为 UUID,但实体(Qms8dStageDetail.approvedBy)与前后端
-- 均按审批人姓名/工号字符串处理,故改为 VARCHAR(幂等,列已为 VARCHAR 时再执行无影响)。
ALTER TABLE ops.qms_8d_stage_detail ALTER COLUMN approved_by TYPE VARCHAR(64) USING COALESCE(approved_by::text, '');
