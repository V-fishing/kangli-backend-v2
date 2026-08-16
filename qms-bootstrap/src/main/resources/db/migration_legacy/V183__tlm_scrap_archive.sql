-- ============================================================
-- V183 工装报废归档表(强约束关联: 报废审批通过即归档, 留存全生命周期)。
-- 与现有归档表(fia/sqm_audit/qms_8d/patl)同范式: UUID 主键 + org_id + 审计字段 + 报告哈希 + 留存到期。
-- 全部以运行库范式为准(UUID + org_id + ops. schema)。
-- ============================================================

CREATE TABLE IF NOT EXISTS ops.tlm_scrap_archive (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          UUID NOT NULL REFERENCES ops.sys_org(id),
  archive_no      VARCHAR(32) NOT NULL UNIQUE,        -- 归档号 TLM-SCA-<time>
  scrap_id        UUID NOT NULL,                       -- 关联 tlm_scrap.id
  tool_id         UUID NOT NULL,                       -- 关联工装
  scrap_no        VARCHAR(32),                         -- 报废单号
  tool_no         VARCHAR(32),                         -- 工装编号(留痕, 防工装删除后失联)
  tool_name       VARCHAR(128),                        -- 工装名称(留痕)
  scrap_method    VARCHAR(16),                         -- DESTROY/RETURN
  reason          VARCHAR(512),                        -- 报废原因
  archive_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
  retention_until DATE,                                -- 留存到期(默认归档日+10年)
  report_hash     VARCHAR(128),
  pdf_ref         VARCHAR(256),                        -- 归档报告路径(placeholder 兼容)
  status          VARCHAR(16) NOT NULL DEFAULT '已归档',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID,
  is_deleted      BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_tlm_scrap_archive_org ON ops.tlm_scrap_archive(org_id);
