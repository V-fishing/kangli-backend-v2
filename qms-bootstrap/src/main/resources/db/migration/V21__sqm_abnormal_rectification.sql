-- V21: 来料异常整改持久化
-- 1) sqm_incoming_abnormal 补齐通知/计划/验证/关闭字段
-- 2) 新建 sqm_abnormal_measure(整改措施记录)
-- 3) 新建 sqm_abnormal_batch_verify(三批验证记录)

-- 1. 补齐 sqm_incoming_abnormal 缺失字段
ALTER TABLE ops.sqm_incoming_abnormal
  ADD COLUMN IF NOT EXISTS notice_date       DATE,
  ADD COLUMN IF NOT EXISTS notice_content    TEXT,
  ADD COLUMN IF NOT EXISTS plan_date         DATE,
  ADD COLUMN IF NOT EXISTS extension_approved BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS extension_date    DATE,
  ADD COLUMN IF NOT EXISTS verify_result     VARCHAR(16),
  ADD COLUMN IF NOT EXISTS verify_comment    TEXT,
  ADD COLUMN IF NOT EXISTS verify_date       TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS verify_by         VARCHAR(64),
  ADD COLUMN IF NOT EXISTS return_reason     TEXT,
  ADD COLUMN IF NOT EXISTS close_auditor     VARCHAR(64);

-- 2. 整改措施记录表(一对一、多对一均可,每次整改存一条)
CREATE TABLE IF NOT EXISTS ops.sqm_abnormal_measure (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  abnormal_id     UUID NOT NULL REFERENCES ops.sqm_incoming_abnormal(id),
  seq             INT NOT NULL DEFAULT 0,
  content         TEXT NOT NULL,
  operator        VARCHAR(64),
  complete_date   DATE,
  status          VARCHAR(16) NOT NULL DEFAULT '待完成',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_abm_abnormal ON ops.sqm_abnormal_measure(org_id, abnormal_id);
COMMENT ON TABLE ops.sqm_abnormal_measure IS '来料异常整改措施记录';

-- 3. 三批验证记录表
CREATE TABLE IF NOT EXISTS ops.sqm_abnormal_batch_verify (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  abnormal_id     UUID NOT NULL REFERENCES ops.sqm_incoming_abnormal(id),
  batch_no        VARCHAR(32) NOT NULL,
  result          VARCHAR(16) NOT NULL DEFAULT '待验证',
  verify_date     DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_abv_abnormal ON ops.sqm_abnormal_batch_verify(org_id, abnormal_id);
COMMENT ON TABLE ops.sqm_abnormal_batch_verify IS '来料异常三批验证记录';
