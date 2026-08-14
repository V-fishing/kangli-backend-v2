-- V175: 工装版本管理(设计变更/升级记录)履历表。
-- 仅做「留痕」(可追溯), 不回滚历史数据。版本号由后端在写入时按工装自动递增(V1, V2, V3...)。
CREATE TABLE IF NOT EXISTS ops.tlm_tool_version (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_id      UUID NOT NULL REFERENCES ops.tlm_tooling(id),
  version_no   VARCHAR(16) NOT NULL,            -- 后端自动递增: V1, V2, V3 ...
  change_type  VARCHAR(16) NOT NULL DEFAULT 'OTHER', -- DESIGN 设计变更 / UPGRADE 升级 / OTHER 其他
  change_desc  VARCHAR(512),
  changed_by   VARCHAR(64),                     -- 变更人(姓名或工号, 留痕展示)
  changed_at   DATE,                            -- 变更日期
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by   UUID,
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_by   UUID,
  is_deleted   BOOLEAN NOT NULL DEFAULT false,
  version      INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.tlm_tool_version IS '工装版本变更履历: 设计变更/升级记录, 仅留痕可追溯';
CREATE INDEX IF NOT EXISTS idx_tlm_tool_version_org_tool ON ops.tlm_tool_version (org_id, tool_id);
