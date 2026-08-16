-- V142 将 created_by/updated_by 误建为 VARCHAR(32),而审计自动填充写入的是
-- 操作人 UUID(36 字符),与项目其他 BaseEntity 表(均为 VARCHAR(64))不一致导致
-- INSERT 报 "value too long for type character varying(32)"。V143 仅补齐了其他基类列,
-- 此处修正这两列长度至 VARCHAR(64),与规范保持一致。

ALTER TABLE ops.spc_sample_task ALTER COLUMN created_by TYPE VARCHAR(64);
ALTER TABLE ops.spc_sample_task ALTER COLUMN updated_by TYPE VARCHAR(64);
