-- ============================================================
-- V18: 扩容 SPC 标注列(SR-SPC-007/013 数据不足标注)
-- baseline_source 原 varchar(16) 装不下"数据不足(子组N<25,仅供参考)";level 原 varchar(8) 装不下"样本过少,无法计算"。
-- 非破坏性扩容,既有短值不受影响。
-- ============================================================
ALTER TABLE ops.spc_control_limit ALTER COLUMN baseline_source TYPE varchar(64);
ALTER TABLE ops.spc_capability ALTER COLUMN level TYPE varchar(32);
