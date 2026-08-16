-- 补齐 ncm_defect_trend_* 与 BaseEntity 审计/乐观锁字段的对齐
-- 背景: V59 建表时漏建 updated_at / created_by / updated_by,
--       而 NcmDefectTrendReport / NcmDefectTrendRule 继承 BaseEntity,
--       MyBatis-Plus 自动全字段查询 → column "updated_at"/"created_by"/"updated_by" does not exist → 500。
-- 使用 IF NOT EXISTS 保证幂等,可安全重复执行。

ALTER TABLE ops.ncm_defect_trend_report
    ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS created_by varchar(64),
    ADD COLUMN IF NOT EXISTS updated_by varchar(64);

ALTER TABLE ops.ncm_defect_trend_rule
    ADD COLUMN IF NOT EXISTS created_by varchar(64),
    ADD COLUMN IF NOT EXISTS updated_by varchar(64);
