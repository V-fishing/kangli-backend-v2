-- 8D 团队/成员/证据字段原定义为 JSONB,但实体(Qms8dReport.team / Qms8dStageDetail.teamMembers,evidenceFiles)
-- 与前后端均按逗号分隔字符串处理,统一改为 VARCHAR。
-- PostgreSQL 不允许在 ALTER ... USING 的 transform 表达式里使用子查询,故分两步:
-- 1) 先以 team::text 简单转列类型; 2) 再用 UPDATE 子查询把已有 JSON 数组规整为逗号字符串。
ALTER TABLE ops.qms_8d_report ALTER COLUMN team TYPE VARCHAR(512) USING team::text;
UPDATE ops.qms_8d_report SET team = (
  SELECT string_agg(v, ',') FROM jsonb_array_elements_text(CAST(team AS jsonb)) v
) WHERE team IS NOT NULL AND team LIKE '[%';

ALTER TABLE ops.qms_8d_stage_detail ALTER COLUMN team_members TYPE VARCHAR(512) USING team_members::text;
UPDATE ops.qms_8d_stage_detail SET team_members = (
  SELECT string_agg(v, ',') FROM jsonb_array_elements_text(CAST(team_members AS jsonb)) v
) WHERE team_members IS NOT NULL AND team_members LIKE '[%';

ALTER TABLE ops.qms_8d_stage_detail ALTER COLUMN evidence_files TYPE VARCHAR(512) USING evidence_files::text;
UPDATE ops.qms_8d_stage_detail SET evidence_files = (
  SELECT string_agg(v, ',') FROM jsonb_array_elements_text(CAST(evidence_files AS jsonb)) v
) WHERE evidence_files IS NOT NULL AND evidence_files LIKE '[%';
