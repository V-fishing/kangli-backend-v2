-- V162: 工装管理(TLM)主表 — 工装夹具(TOOL) 与 监视测量设备(GAUGE) 合一资产台账。
-- 运行库范式: UUID 主键 + org_id 多分公司隔离 + 审计字段 + ops. schema。
-- GAUGE 专属计量校准列仅对 GAUGE 类使用, TOOL 类留空。
CREATE TABLE IF NOT EXISTS ops.tlm_tooling (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_id        UUID NOT NULL REFERENCES ops.sys_org(id),
  tool_no       VARCHAR(32) NOT NULL,                 -- 一物一码, 如 TL-001 / GAUGE-001
  tool_name     VARCHAR(128) NOT NULL,
  tool_category VARCHAR(8)  NOT NULL DEFAULT 'TOOL',  -- TOOL 工装夹具 / GAUGE 监视测量设备
  tool_type     VARCHAR(32),                           -- 模具/夹具/治具/定位工装(TOOL); 量具/仪器(GAUGE)
  risk_class    VARCHAR(8),                            -- I/II/III/IV 风险等级(仅 GAUGE 适用)
  process_id    UUID REFERENCES ops.spc_process(id),  -- 复用工序主数据
  product_code  VARCHAR(64),                           -- 关联产品(暂冗余, 无 BOM 主数据)
  spec          VARCHAR(256),
  material      VARCHAR(64),
  supplier_id   UUID REFERENCES ops.sqm_supplier(id),
  status        VARCHAR(16) NOT NULL DEFAULT 'IN_USE',-- IN_USE 在用 / DISABLED 停用 / REPAIRING 维修中 / SCRAPPED 报废
  location      VARCHAR(128),
  owner_id      UUID REFERENCES ops.sys_user(id),      -- 领用人/责任人
  admin_id      UUID REFERENCES ops.sys_user(id),      -- 设备管理员(仅 GAUGE)
  software_ver  VARCHAR(32),                           -- 软件版本(仅 GAUGE)
  -- 计量校准(仅 GAUGE)
  precision_val     VARCHAR(32),                       -- 精度
  measure_point     VARCHAR(64),                       -- 测量点
  calib_date        DATE,                              -- 上次校准日期
  calib_due_date    DATE,                              -- 下次校准到期(预警核心)
  calib_cycle       INTEGER,                           -- 校准周期(天)
  -- 寿命
  bind_count    INTEGER NOT NULL DEFAULT 0,            -- 已绑定工单次数(寿命已用)
  design_life   INTEGER,                               -- 寿命上限次数(NULL=不限)
  -- 保养
  next_maint_date DATE,                                -- 下次保养日期(预警)
  maint_cycle   INTEGER,                               -- 保养周期(天)
  cost          NUMERIC(12,2),
  inbound_date  DATE,
  locked        BOOLEAN NOT NULL DEFAULT false,        -- 不合格锁定
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by    UUID,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_by    UUID,
  is_deleted    BOOLEAN NOT NULL DEFAULT false,
  version       INTEGER NOT NULL DEFAULT 0,
  UNIQUE(org_id, tool_no)
);
COMMENT ON TABLE ops.tlm_tooling IS '工装管理主表: 工装夹具与监视测量设备合一台账';
CREATE INDEX IF NOT EXISTS idx_tlm_tooling_org_status   ON ops.tlm_tooling (org_id, status);
CREATE INDEX IF NOT EXISTS idx_tlm_tooling_org_cat     ON ops.tlm_tooling (org_id, tool_category);
CREATE INDEX IF NOT EXISTS idx_tlm_tooling_org_tool_no ON ops.tlm_tooling (org_id, tool_no);
CREATE INDEX IF NOT EXISTS idx_tlm_tooling_org_calib   ON ops.tlm_tooling (org_id, calib_due_date);
CREATE INDEX IF NOT EXISTS idx_tlm_tooling_org_maint   ON ops.tlm_tooling (org_id, next_maint_date);
