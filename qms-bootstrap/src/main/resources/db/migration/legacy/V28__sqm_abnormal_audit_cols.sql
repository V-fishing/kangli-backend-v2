-- V28: 补齐来料异常整改两张表的 BaseEntity 审计列
-- 原因: V21 建表时遗漏 created_by/updated_by/is_deleted/version(及 batch_verify 的 updated_at),
--       导致 MyBatis-Plus 按实体查询时 SELECT/WHERE 引用不存在的列 -> SQL 报错 -> 详情接口 500。

-- 1. 整改措施记录表补列
ALTER TABLE ops.sqm_abnormal_measure
  ADD COLUMN IF NOT EXISTS created_by UUID,
  ADD COLUMN IF NOT EXISTS updated_by UUID,
  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS version    INT     NOT NULL DEFAULT 0;

-- 2. 三批验证记录表补列(含 updated_at)
ALTER TABLE ops.sqm_abnormal_batch_verify
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS created_by UUID,
  ADD COLUMN IF NOT EXISTS updated_by UUID,
  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS version    INT     NOT NULL DEFAULT 0;
