-- V12: patrol 巡检模块(一期,技术选型§3.3 域1.5)
-- patrol 未在 DB 设计文档中定义,本迁移从零设计(参考 FIA/SPC 模式)

-- ============ 巡检路线(配置) ============
CREATE TABLE ops.patl_route (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  route_code   VARCHAR(32) NOT NULL,
  route_name   VARCHAR(128) NOT NULL,
  proc_name    VARCHAR(32),                 -- 关联工序(注塑/焊接/组装...)
  freq         VARCHAR(32),                 -- 频次: 1次/班/1次/天/1次/周
  status       VARCHAR(8) NOT NULL DEFAULT '启用',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0,
  UNIQUE(org_id, route_code)
);
COMMENT ON TABLE ops.patl_route IS '巡检路线(配置:频次+关联工序)';

-- ============ 巡检点位(配置,属于路线) ============
CREATE TABLE ops.patl_checkpoint (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  route_id     UUID NOT NULL REFERENCES ops.patl_route(id) ON DELETE CASCADE,
  seq          SMALLINT NOT NULL,            -- 顺序
  point_name   VARCHAR(128) NOT NULL,        -- 点位名称(如:注塑机A区/焊接工位3)
  location     VARCHAR(255),                 -- 位置描述
  need_photo   BOOLEAN NOT NULL DEFAULT false, -- 是否必拍照片
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0,
  UNIQUE(route_id, seq)
);
COMMENT ON TABLE ops.patl_checkpoint IS '巡检点位(路线下有序点位)';

-- ============ 点位检查项(配置,属于点位) ============
CREATE TABLE ops.patl_check_item (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  checkpoint_id UUID NOT NULL REFERENCES ops.patl_checkpoint(id) ON DELETE CASCADE,
  seq          SMALLINT NOT NULL,
  item_name    VARCHAR(255) NOT NULL,        -- 检查内容(如:温度是否正常/有无异物)
  check_type   VARCHAR(16) NOT NULL DEFAULT 'enum', -- enum/numeric/text
  std_value    VARCHAR(64),                  -- 标准值/合格值
  enum_values  VARCHAR(255),                 -- 枚举选项(逗号分隔: 正常,异常)
  is_required  BOOLEAN NOT NULL DEFAULT true,
  UNIQUE(checkpoint_id, seq)
);
COMMENT ON TABLE ops.patl_check_item IS '点位检查项(可配:枚举/数值/文本)';

-- ============ 巡检任务(执行) ============
CREATE TABLE ops.patl_task (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  task_no      VARCHAR(32) NOT NULL,
  route_id     UUID NOT NULL REFERENCES ops.patl_route(id),
  shift        VARCHAR(8),                   -- 早班/中班/晚班
  plan_time    TIMESTAMPTZ,                  -- 计划巡检时间
  actual_time  TIMESTAMPTZ,                  -- 实际开始时间
  finish_time  TIMESTAMPTZ,                  -- 完成时间
  inspector_id UUID,                         -- 巡检人
  status       VARCHAR(16) NOT NULL DEFAULT '待巡检',  -- 待巡检/进行中/已完成/超时
  total_points  INTEGER NOT NULL DEFAULT 0,  -- 应检点位数
  done_points   INTEGER NOT NULL DEFAULT 0,  -- 已检点位数
  abnormal_count INTEGER NOT NULL DEFAULT 0, -- 异常数
  remark       TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0,
  UNIQUE(org_id, task_no)
);
COMMENT ON TABLE ops.patl_task IS '巡检任务(按路线生成,含班次+状态)';

-- ============ 巡检记录(每个点位的检查结果) ============
CREATE TABLE ops.patl_record (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  task_id      UUID NOT NULL REFERENCES ops.patl_task(id) ON DELETE CASCADE,
  checkpoint_id UUID NOT NULL REFERENCES ops.patl_checkpoint(id),
  checkpoint_name VARCHAR(128) NOT NULL,     -- 快照
  result       VARCHAR(8) NOT NULL DEFAULT '正常',  -- 正常/异常
  check_time   TIMESTAMPTZ NOT NULL DEFAULT now(),
  operator_id  UUID,
  photo_ref    VARCHAR(512),                 -- MinIO key(如有照片)
  remark       TEXT,
  item_results JSONB                         -- 各检查项结果 [{itemId, itemName, value, result}]
);
COMMENT ON TABLE ops.patl_record IS '巡检记录(点位级,含检查项明细 JSONB)';

-- ============ 巡检异常(从异常记录转 NCM/8D) ============
CREATE TABLE ops.patl_abnormal (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id       UUID NOT NULL REFERENCES ops.sys_org(id),
  task_id      UUID NOT NULL REFERENCES ops.patl_task(id),
  record_id    UUID REFERENCES ops.patl_record(id),
  checkpoint_name VARCHAR(128) NOT NULL,
  description  TEXT NOT NULL,                -- 异常描述
  severity     VARCHAR(8) NOT NULL DEFAULT '一般',  -- 严重/一般
  status       VARCHAR(16) NOT NULL DEFAULT '待处理',  -- 待处理/已转NCM/已关闭
  d8_id        UUID REFERENCES ops.qms_8d_report(id),  -- 转8D
  ncm_record_id UUID,                       -- 转NCM不良记录
  handle_remark TEXT,
  handled_by   UUID,
  handled_at   TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.patl_abnormal IS '巡检异常(可转 NCM/8D)';
