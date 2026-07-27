-- 加严检验 lot_id 实际承载业务批号(如前端生成 "LOT-<ts>"),原 UUID 类型无法存储,
-- 导致录入结果提交时报 "invalid input syntax for type uuid"。
-- 调整为 VARCHAR(64) 以容纳业务批号字符串(加严检验在变更审批后执行,批次号即来料批标识)。
ALTER TABLE ops.sqm_change_strict_inspect
    ALTER COLUMN lot_id TYPE VARCHAR(64) USING lot_id::VARCHAR(64);
