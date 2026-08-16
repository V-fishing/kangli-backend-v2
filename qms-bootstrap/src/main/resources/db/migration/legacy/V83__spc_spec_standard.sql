-- V83: SPC 标准线管理模块
-- 标准线(规格上下限)由 SPC 统一管理,按(组织+物料+工序)唯一确定一条标准线
-- SpcParam 可选择关联标准线,自动填充 spec_lower/spec_upper/target_value/unit
-- 同时可被 FAI 检验标准库引用("从SPC标准线拉取")

CREATE TABLE IF NOT EXISTS ops.spc_spec_standard (
    id          UUID DEFAULT ops.gen_uuid_v7() NOT NULL PRIMARY KEY,
    org_id      UUID NOT NULL,
    material    VARCHAR(200) NOT NULL,
    proc_name   VARCHAR(200) NOT NULL,
    spec_lower  NUMERIC,
    spec_upper  NUMERIC,
    target_value NUMERIC,
    unit        VARCHAR(32),
    chart_type  VARCHAR(32) DEFAULT 'Xbar-R',
    created_at  TIMESTAMPTZ DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    is_deleted  BOOLEAN DEFAULT FALSE,
    version     INT DEFAULT 0,
    CONSTRAINT uk_spec_standard UNIQUE (org_id, material, proc_name)
);

COMMENT ON TABLE  ops.spc_spec_standard IS 'SPC 标准线管理:按(组织+物料+工序)定义规格上下限/目标值/单位';
COMMENT ON COLUMN ops.spc_spec_standard.org_id       IS '组织ID(分公司/车间)';
COMMENT ON COLUMN ops.spc_spec_standard.material     IS '物料名称';
COMMENT ON COLUMN ops.spc_spec_standard.proc_name    IS '工序名称';
COMMENT ON COLUMN ops.spc_spec_standard.spec_lower   IS '规格下限(LSL)';
COMMENT ON COLUMN ops.spc_spec_standard.spec_upper   IS '规格上限(USL)';
COMMENT ON COLUMN ops.spc_spec_standard.target_value IS '目标值(设计中心值)';
COMMENT ON COLUMN ops.spc_spec_standard.unit         IS '单位(mm/g/...)';
COMMENT ON COLUMN ops.spc_spec_standard.chart_type   IS '默认控制图类型:Xbar-R/Xbar-S/I-MR/P';
COMMENT ON COLUMN ops.spc_spec_standard.version      IS '乐观锁';

-- SpcParam 添加标准线关联(可选),选择标准线后自动填充规格字段
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS spec_standard_id UUID REFERENCES ops.spc_spec_standard(id);
COMMENT ON COLUMN ops.spc_param.spec_standard_id IS '关联的 SPC 标准线ID(可空);选择后 specLower/specUpper/targetValue/unit 从标准线自动填充';

CREATE INDEX IF NOT EXISTS idx_spc_param_spec_standard ON ops.spc_param(spec_standard_id);
