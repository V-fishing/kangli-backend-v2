-- V23: 来料检验计划 + AQL抽样方案
-- 物料分类 → 工序 → 标准 → 抽样参数,来料入库时自动生成检验任务

CREATE TABLE ops.fia_insp_plan (
    id              uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    org_id          uuid NOT NULL REFERENCES ops.sys_org(id),
    plan_code       varchar(32) NOT NULL,
    plan_name       varchar(64) NOT NULL,
    material_category varchar(64) NOT NULL,  -- 物料分类(密封圈类/弹簧类...)
    proc_name       varchar(64) NOT NULL,     -- 工序名(外观/尺寸/硬度...)
    std_id          uuid REFERENCES ops.fia_insp_std(id),
    aql             numeric(5,2) DEFAULT 1.0,     -- AQL 值
    sample_level    varchar(8) DEFAULT 'II',       -- 检验水平 I/II/III/S1/S2/S3/S4
    sample_plan     varchar(8) DEFAULT '单次',      -- 单次/二次/多次
    severity        varchar(8) DEFAULT '一般',      -- 严重/一般/轻微
    is_active       boolean NOT NULL DEFAULT true,
    sort_order      integer DEFAULT 0,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    created_by      uuid,
    updated_by      uuid,
    is_deleted      boolean NOT NULL DEFAULT false,
    version         integer NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_fia_insp_plan_cat_proc ON ops.fia_insp_plan (org_id, material_category, proc_name) WHERE is_deleted = false;
COMMENT ON TABLE ops.fia_insp_plan IS '来料检验计划:物料分类→工序→标准→AQL抽样方案';
COMMENT ON COLUMN ops.fia_insp_plan.aql IS 'AQL值(如1.0/0.65/2.5),用于查GB/T2828.1抽样表';
