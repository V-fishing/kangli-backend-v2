-- V56: 8D 阶段审核配置表 + 默认种子(D3/D5/D7 需审核人签名)
CREATE TABLE IF NOT EXISTS ops.qms_8d_approval_config (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        VARCHAR(64) NOT NULL,
  stage_code    VARCHAR(16) NOT NULL,
  need_approval BOOLEAN NOT NULL DEFAULT FALSE,
  signer        VARCHAR(128),
  sort_order    INT NOT NULL DEFAULT 0,
  UNIQUE (org_id, stage_code)
);

-- 默认: D3/D5/D7 需要审核(质量经理签名)——可在“8D审核配置”页按需调整
INSERT INTO ops.qms_8d_approval_config (org_id, stage_code, need_approval, sort_order)
SELECT o.id, s.stage, TRUE, s.ord
FROM ops.sys_org o
CROSS JOIN (VALUES ('D3', 3), ('D5', 5), ('D7', 7)) AS s(stage, ord)
ON CONFLICT (org_id, stage_code) DO NOTHING;
