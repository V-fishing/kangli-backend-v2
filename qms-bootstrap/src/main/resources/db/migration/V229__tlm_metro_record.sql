-- ============================================================
-- V229 TLM 计量数据采集记录 tlm_metro_record。
-- 支持计量管理员手动录入计量器具(GAUGE)的实际测量值, 并绑定工单/批次,
-- 留存测量数据追溯(需求: 计量管理下的「计量数据采集」页)。
-- 字段: 器具/工单/批次/计量点位/实测值/单位/标准值/允差上下限/判定/测量时间/操作员。
-- ============================================================

CREATE TABLE IF NOT EXISTS ops.tlm_metro_record (
  id              uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id          uuid,
  tool_id         uuid NOT NULL,
  tool_no         varchar(64),
  tool_name       varchar(128),
  wo_no           varchar(64),                 -- 绑定工单号
  batch_no        varchar(64),                 -- 绑定批次号
  measure_point   varchar(64),                 -- 计量点位(如: 长度/直径/温度)
  measure_value   varchar(64),                 -- 实测值(字符以兼容多单位)
  measure_unit    varchar(16),                 -- 单位(mm/μm/℃/%等)
  standard_value  varchar(64),                 -- 标准值/标称值
  upper_limit     varchar(64),                 -- 允许上限
  lower_limit     varchar(64),                 -- 允许下限
  judged          varchar(16) NOT NULL DEFAULT '合格',  -- 合格/不合格
  measure_time    timestamp,                   -- 实测时间
  operator        varchar(64),                 -- 操作员
  remark          varchar(255),
  is_deleted      boolean NOT NULL DEFAULT false,
  created_at      timestamp DEFAULT now(),
  updated_at      timestamp DEFAULT now(),
  created_by      varchar(36),
  updated_by      varchar(36),
  version         int DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tlm_metro_record_tool ON ops.tlm_metro_record (tool_id);
CREATE INDEX IF NOT EXISTS idx_tlm_metro_record_wo   ON ops.tlm_metro_record (wo_no);
CREATE INDEX IF NOT EXISTS idx_tlm_metro_record_time ON ops.tlm_metro_record (measure_time);
