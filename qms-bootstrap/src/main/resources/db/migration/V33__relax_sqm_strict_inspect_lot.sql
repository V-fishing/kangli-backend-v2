-- 变更审批联动创建加严检验计划时,尚无具体来料批(lot_id)。
-- 原 lot_id NOT NULL 导致联动 insert 失败,且因处于 approve 同一事务内,
-- 会把事务标记为 rollback-only,提交时抛出 UnexpectedRollbackException(前端:"系统异常,请联系管理员")。
-- 计划阶段 lot_id 允许为空。
ALTER TABLE ops.sqm_change_strict_inspect ALTER COLUMN lot_id DROP NOT NULL;
