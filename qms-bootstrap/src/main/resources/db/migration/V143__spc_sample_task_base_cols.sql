-- 补齐 spc_sample_task 表结构,使其符合 BaseEntity 审计/乐观锁约定
-- V142 仅建了 created_at/created_by,缺失 updated_at/updated_by/is_deleted/version,
-- 导致 MyBatis-Plus 自动填充 INSERT 时报 "column updated_at does not exist"。

-- V142 将 created_by 误建为 VARCHAR(32),而审计字段写入的是 UUID(36 字符),
-- 与项目其他 BaseEntity 表一致应为 VARCHAR(64)。此处修正长度避免写入超长报错。
ALTER TABLE ops.spc_sample_task ALTER COLUMN created_by TYPE VARCHAR(64);

ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS updated_at   TIMESTAMP   NOT NULL DEFAULT NOW();
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS updated_by   VARCHAR(64);
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE;
ALTER TABLE ops.spc_sample_task ADD COLUMN IF NOT EXISTS version      INTEGER     NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_spctask_deleted ON ops.spc_sample_task (is_deleted);
