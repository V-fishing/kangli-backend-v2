-- V37__sys_audit_log.sql
-- 审计日志表: AuditAspect 自动写入, 不依赖各 Service 手动调用

CREATE TABLE IF NOT EXISTS ops.sys_audit_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    module          VARCHAR(32)  NOT NULL,
    action          VARCHAR(16)  NOT NULL,
    method          VARCHAR(128),
    operator_id     VARCHAR(64),
    operator_name   VARCHAR(64)  NOT NULL DEFAULT 'anonymous',
    record_id       VARCHAR(64),
    detail          VARCHAR(512),
    status          VARCHAR(8)   NOT NULL DEFAULT 'SUCCESS',
    error           TEXT,
    cost_ms         INTEGER,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

COMMENT ON TABLE  ops.sys_audit_log IS '操作审计日志(AuditAspect 自动写入)';
COMMENT ON COLUMN ops.sys_audit_log.module        IS '模块: FIA/SPC/NCM/SQM/UOP';
COMMENT ON COLUMN ops.sys_audit_log.action        IS '动作: CREATE/UPDATE/APPROVE/CLOSE/DELETE';
COMMENT ON COLUMN ops.sys_audit_log.operator_name IS '操作人账号(从 JWT 解析)';
COMMENT ON COLUMN ops.sys_audit_log.record_id     IS '被操作记录的 ID(UUID)';
COMMENT ON COLUMN ops.sys_audit_log.cost_ms       IS '耗时(毫秒)';

CREATE INDEX IF NOT EXISTS idx_audit_module  ON ops.sys_audit_log(module);
CREATE INDEX IF NOT EXISTS idx_audit_action  ON ops.sys_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_time    ON ops.sys_audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_record  ON ops.sys_audit_log(record_id);
