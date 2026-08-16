-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SPC 统计过程 (SPC)]
-- ----------------------------------------------------------------------------
-- 板块 : SPC 统计过程 (SPC)
-- 内容 : 18 张表 (CREATE TABLE / 索引 / 非外键约束)
-- 外键 : 见 V017__schema_constraints.sql (所有表建完后统一追加)
-- 依赖 : V001 地基函数
-- 生成 : pg_dump -t 逐表导出, FK 约束剥离到 V017。
-- ============================================================================

--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_sample_task; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_sample_task (
    id uuid NOT NULL,
    org_id uuid,
    wo_no character varying(64) NOT NULL,
    part_no character varying(64) NOT NULL,
    proc_name character varying(64),
    product_name character varying(128),
    param_id uuid NOT NULL,
    target_count integer DEFAULT 1 NOT NULL,
    current_count integer DEFAULT 0 NOT NULL,
    status character varying(16) DEFAULT '采集中'::character varying NOT NULL,
    cpk numeric(6,4),
    released boolean DEFAULT false NOT NULL,
    alarm_flag boolean DEFAULT false NOT NULL,
    created_by character varying(64),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    trigger_type character varying(64),
    category character varying(32),
    supplier_id character varying(64),
    supplier_name character varying(128),
    is_urgent boolean DEFAULT false NOT NULL,
    remark character varying(512)
);


--
-- Name: spc_sample_task spc_sample_task_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_sample_task
    ADD CONSTRAINT spc_sample_task_pkey PRIMARY KEY (id);


--
-- Name: idx_spctask_category; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_category ON ops.spc_sample_task USING btree (category);


--
-- Name: idx_spctask_deleted; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_deleted ON ops.spc_sample_task USING btree (is_deleted);


--
-- Name: idx_spctask_org; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_org ON ops.spc_sample_task USING btree (org_id);


--
-- Name: idx_spctask_param; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_param ON ops.spc_sample_task USING btree (param_id);


--
-- Name: idx_spctask_supplier; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_supplier ON ops.spc_sample_task USING btree (supplier_id);


--
-- Name: idx_spctask_trigger; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_trigger ON ops.spc_sample_task USING btree (trigger_type);


--
-- Name: idx_spctask_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spctask_wo ON ops.spc_sample_task USING btree (wo_no);


--
-- Name: spc_sample_task fk_spctask_param; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)



--
-- Name: spc_subgroup; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_subgroup (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    subgroup_no integer NOT NULL,
    subgroup_time timestamp with time zone NOT NULL,
    shift character varying(8),
    n integer NOT NULL,
    xbar numeric(12,4),
    range_r numeric(12,4),
    judge character varying(8) DEFAULT '正常'::character varying NOT NULL,
    is_outlier boolean DEFAULT false NOT NULL,
    data_source character varying(8) DEFAULT 'manual'::character varying NOT NULL,
    operator_id uuid,
    wo_no character varying(32),
    batch_no character varying(32),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    outlier_rule character varying(32),
    task_id character varying(36),
    product_code character varying(100),
    stage character varying(8) DEFAULT 'ROUTINE'::character varying NOT NULL,
    sample_task_id uuid,
    std_dev numeric(18,6),
    nonconforming integer,
    inspect_n integer,
    defect_count integer
)
PARTITION BY RANGE (subgroup_time);


--
-- Name: TABLE spc_subgroup; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_subgroup IS 'SPC 子组数据(时序,按月分区)';


--
-- Name: COLUMN spc_subgroup.shift; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_subgroup.shift IS '班次(已废弃,保留兼容历史数据)';


--
-- Name: COLUMN spc_subgroup.outlier_rule; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_subgroup.outlier_rule IS '命中判异规则编号(①-⑧)';


--
-- Name: COLUMN spc_subgroup.task_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_subgroup.task_id IS '关联FIA任务ID(fia_task.id)';


--
-- Name: COLUMN spc_subgroup.product_code; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_subgroup.product_code IS '产品料号(来自FIA任务)';


--
-- Name: COLUMN spc_subgroup.stage; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_subgroup.stage IS 'SPC 数据阶段: FIRST=首件能力验证(点状/一次性,由FIA任务联动产生); ROUTINE=量产过程监控(线状/持续,由来料/追溯页量产抽样入口产生)';


--
-- Name: spc_subgroup spc_subgroup_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_subgroup
    ADD CONSTRAINT spc_subgroup_pkey PRIMARY KEY (id, subgroup_time);


--
-- Name: idx_spc_sub_param; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_sub_param ON ONLY ops.spc_subgroup USING btree (org_id, param_id, subgroup_time DESC);


--
-- Name: idx_spc_sub_stage; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_sub_stage ON ONLY ops.spc_subgroup USING btree (org_id, param_id, stage, subgroup_time DESC);


--
-- Name: idx_subgroup_sample_task; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_subgroup_sample_task ON ONLY ops.spc_subgroup USING btree (sample_task_id);


--
-- Name: spc_subgroup spc_subgroup_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_subgroup spc_subgroup_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_subgroup_default; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_subgroup_default (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    subgroup_no integer NOT NULL,
    subgroup_time timestamp with time zone NOT NULL,
    shift character varying(8),
    n integer NOT NULL,
    xbar numeric(12,4),
    range_r numeric(12,4),
    judge character varying(8) DEFAULT '正常'::character varying NOT NULL,
    is_outlier boolean DEFAULT false NOT NULL,
    data_source character varying(8) DEFAULT 'manual'::character varying NOT NULL,
    operator_id uuid,
    wo_no character varying(32),
    batch_no character varying(32),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    outlier_rule character varying(32),
    task_id character varying(36),
    product_code character varying(100),
    stage character varying(8) DEFAULT 'ROUTINE'::character varying NOT NULL,
    sample_task_id uuid,
    std_dev numeric(18,6),
    nonconforming integer,
    inspect_n integer,
    defect_count integer
);


--
-- Name: spc_subgroup_default; Type: TABLE ATTACH; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_subgroup ATTACH PARTITION ops.spc_subgroup_default DEFAULT;


--
-- Name: spc_subgroup_default spc_subgroup_default_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_org_id_param_id_stage_subgroup_time_idx; Type: INDEX; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_org_id_param_id_subgroup_time_idx; Type: INDEX; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_sample_task_id_idx; Type: INDEX; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_org_id_param_id_stage_subgroup_time_idx; Type: INDEX ATTACH; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_org_id_param_id_subgroup_time_idx; Type: INDEX ATTACH; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_pkey; Type: INDEX ATTACH; Schema: ops; Owner: -
--




--
-- Name: spc_subgroup_default_sample_task_id_idx; Type: INDEX ATTACH; Schema: ops; Owner: -
--




--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_param; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_param (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_name character varying(64) NOT NULL,
    proc_name character varying(32) NOT NULL,
    unit character varying(16) NOT NULL,
    spec_lower numeric(12,4),
    spec_upper numeric(12,4),
    spec_text character varying(32) NOT NULL,
    target_value numeric(12,4),
    subgroup_size integer DEFAULT 5 NOT NULL,
    collect_freq character varying(32) NOT NULL,
    chart_type character varying(8),
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    sigma_method character varying(16),
    sigma_k numeric(4,2),
    cpk_period character varying(8),
    supplier_id uuid,
    fia_std_item_id uuid,
    spec_standard_id uuid,
    process_id uuid,
    chartable boolean DEFAULT true NOT NULL,
    src_item_id uuid,
    param_source character varying(32) DEFAULT 'MANUAL'::character varying NOT NULL,
    chart_candidates character varying(64),
    src_wo_no character varying(64),
    src_batch_no character varying(64),
    data_type character varying(16),
    CONSTRAINT spc_param_subgroup_size_check CHECK (((subgroup_size >= 1) AND (subgroup_size <= 25)))
);


--
-- Name: TABLE spc_param; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_param IS 'SPC 参数配置(工序-参数 CRUD)';


--
-- Name: COLUMN spc_param.sigma_method; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.sigma_method IS 'CPK 标准差算法:within/overall';


--
-- Name: COLUMN spc_param.sigma_k; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.sigma_k IS '控制限/能力指数 σ 倍数,默认 3';


--
-- Name: COLUMN spc_param.cpk_period; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.cpk_period IS 'CPK 自动滚动更新周期:批次/日/周';


--
-- Name: COLUMN spc_param.supplier_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.supplier_id IS '关联供应商:该工序参数归属的供应商,用于 CPK→供应商质量分联动';


--
-- Name: COLUMN spc_param.fia_std_item_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.fia_std_item_id IS '关联的FIA检验标准项ID(可空);关联后规格字段从标准项的stdValue+tolerance派生';


--
-- Name: COLUMN spc_param.spec_standard_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.spec_standard_id IS '关联的 SPC 标准线ID(可空);选择后 specLower/specUpper/targetValue/unit 从标准线自动填充';


--
-- Name: COLUMN spc_param.process_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.process_id IS '所属工序ID(父级分组,可空=未分类);原 proc_name 保留为工位/说明';


--
-- Name: COLUMN spc_param.chartable; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.chartable IS '是否可制图:specLower/specUpper/targetValue 全空则 false';


--
-- Name: COLUMN spc_param.src_item_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.src_item_id IS '来源FIA任务检验项ID(可空);由FIA任务一键生成SPC参数时写入,用于去重';


--
-- Name: COLUMN spc_param.chart_candidates; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param.chart_candidates IS '带入时的推荐控制图全集(逗号分隔),编辑弹窗据此限定 chartType 可选范围;空则用全量';


--
-- Name: spc_param spc_param_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_param
    ADD CONSTRAINT spc_param_pkey PRIMARY KEY (id);


--
-- Name: idx_spc_param_fia_item; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_param_fia_item ON ops.spc_param USING btree (fia_std_item_id);


--
-- Name: idx_spc_param_process; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_param_process ON ops.spc_param USING btree (process_id);


--
-- Name: idx_spc_param_spec_standard; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_param_spec_standard ON ops.spc_param USING btree (spec_standard_id);


--
-- Name: idx_spc_param_src_item; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_param_src_item ON ops.spc_param USING btree (src_item_id);


--
-- Name: idx_spcparam_source; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spcparam_source ON ops.spc_param USING btree (param_source);


--
-- Name: uq_spc_param_src_item; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uq_spc_param_src_item ON ops.spc_param USING btree (src_item_id) WHERE (src_item_id IS NOT NULL);


--
-- Name: spc_param spc_param_fia_std_item_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param spc_param_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param spc_param_process_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param spc_param_spec_standard_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param spc_param_src_item_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param spc_param_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_control_limit; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_control_limit (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    chart_type character varying(8) NOT NULL,
    baseline_source character varying(64) NOT NULL,
    n_subgroups integer NOT NULL,
    xbar_ucl numeric(12,4) NOT NULL,
    xbar_cl numeric(12,4) NOT NULL,
    xbar_lcl numeric(12,4) NOT NULL,
    r_ucl numeric(12,4),
    r_cl numeric(12,4),
    r_lcl numeric(12,4),
    calc_at timestamp with time zone DEFAULT now() NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    manual boolean
);


--
-- Name: TABLE spc_control_limit; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_control_limit IS 'SPC 控制限(前25子组建基线,动态更新)';


--
-- Name: COLUMN spc_control_limit.manual; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_control_limit.manual IS 'true=人工覆盖,优先于自动计算';


--
-- Name: spc_control_limit spc_control_limit_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_control_limit
    ADD CONSTRAINT spc_control_limit_pkey PRIMARY KEY (id);


--
-- Name: spc_control_limit spc_control_limit_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_control_limit spc_control_limit_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_measurement; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_measurement (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    subgroup_id uuid NOT NULL,
    subgroup_time timestamp with time zone NOT NULL,
    seq integer NOT NULL,
    value numeric(12,4) NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    created_by uuid
);


--
-- Name: spc_measurement spc_measurement_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_measurement
    ADD CONSTRAINT spc_measurement_pkey PRIMARY KEY (id);


--
-- Name: spc_measurement spc_measurement_subgroup_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_measurement
    ADD CONSTRAINT spc_measurement_subgroup_id_seq_key UNIQUE (subgroup_id, seq);


--
-- Name: spc_measurement spc_measurement_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_alarm; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_alarm (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    code character varying(32) NOT NULL,
    param_id uuid NOT NULL,
    param_name character varying(64) NOT NULL,
    current_value numeric(12,4),
    triggered_rule character varying(32) NOT NULL,
    level character varying(8) NOT NULL,
    subgroup_start_no integer,
    subgroup_end_no integer,
    alarm_time timestamp with time zone DEFAULT now() NOT NULL,
    status character varying(16) DEFAULT '待确认'::character varying NOT NULL,
    close_reason text,
    disposition text,
    closed_by uuid,
    closed_at timestamp with time zone,
    suppress_until timestamp with time zone,
    wo_no character varying(32),
    batch_no character varying(32),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE spc_alarm; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_alarm IS 'SPC 告警(WECO 规则,30min 抑制)';


--
-- Name: spc_alarm spc_alarm_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_alarm
    ADD CONSTRAINT spc_alarm_code_key UNIQUE (code);


--
-- Name: spc_alarm spc_alarm_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_alarm
    ADD CONSTRAINT spc_alarm_pkey PRIMARY KEY (id);


--
-- Name: idx_spc_alarm_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_alarm_status ON ops.spc_alarm USING btree (org_id, status, alarm_time DESC);


--
-- Name: spc_alarm spc_alarm_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_alarm spc_alarm_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_import_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_import_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    code character varying(32) NOT NULL,
    param_id uuid NOT NULL,
    param_name character varying(64) NOT NULL,
    file_name character varying(255) NOT NULL,
    file_type character varying(8),
    import_mode character varying(8) NOT NULL,
    record_count integer NOT NULL,
    error_count integer DEFAULT 0 NOT NULL,
    status character varying(16) NOT NULL,
    file_size character varying(16),
    operator_id uuid NOT NULL,
    imported_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: spc_import_log spc_import_log_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_import_log
    ADD CONSTRAINT spc_import_log_code_key UNIQUE (code);


--
-- Name: spc_import_log spc_import_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_import_log
    ADD CONSTRAINT spc_import_log_pkey PRIMARY KEY (id);


--
-- Name: spc_import_log spc_import_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_import_log spc_import_log_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_supplier_capability; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_supplier_capability (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    material_id uuid NOT NULL,
    cpk numeric(6,3),
    level character varying(8),
    period_value character varying(16) NOT NULL
);


--
-- Name: spc_supplier_capability spc_supplier_capability_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_supplier_capability
    ADD CONSTRAINT spc_supplier_capability_pkey PRIMARY KEY (id);


--
-- Name: spc_supplier_capability spc_supplier_capability_supplier_id_material_id_period_valu_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_supplier_capability
    ADD CONSTRAINT spc_supplier_capability_supplier_id_material_id_period_valu_key UNIQUE (supplier_id, material_id, period_value);


--
-- Name: spc_supplier_capability spc_supplier_capability_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    rule_code character varying(4) NOT NULL,
    rule_name character varying(64) NOT NULL,
    level character varying(8) NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    sort_no integer
);


--
-- Name: TABLE spc_rule; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_rule IS 'WECO 判异规则(①-⑧,默认启用①②③⑤)';


--
-- Name: spc_rule spc_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_rule
    ADD CONSTRAINT spc_rule_pkey PRIMARY KEY (id);


--
-- Name: spc_rule spc_rule_rule_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_rule
    ADD CONSTRAINT spc_rule_rule_code_key UNIQUE (rule_code);


--
-- Name: spc_rule spc_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_global_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_global_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    baseline_mode character varying(16) NOT NULL,
    default_subgroup_size integer DEFAULT 5 NOT NULL,
    chart_auto_rules character varying(64)[] NOT NULL,
    cpk_period character varying(8) DEFAULT 'month'::character varying NOT NULL,
    cpk_sufficient numeric(4,2) DEFAULT 1.33 NOT NULL,
    cpk_acceptable numeric(4,2) DEFAULT 1.00 NOT NULL,
    spec_source character varying(16) DEFAULT '检验标准库'::character varying NOT NULL,
    alert_level character varying(16) NOT NULL,
    suppress_minutes integer DEFAULT 30 NOT NULL
);


--
-- Name: spc_global_config spc_global_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_global_config
    ADD CONSTRAINT spc_global_config_pkey PRIMARY KEY (id);


--
-- Name: spc_global_config spc_global_config_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_notify_channel; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_notify_channel (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    channel character varying(16) NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    config_json jsonb
);


--
-- Name: spc_notify_channel spc_notify_channel_channel_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_notify_channel
    ADD CONSTRAINT spc_notify_channel_channel_key UNIQUE (channel);


--
-- Name: spc_notify_channel spc_notify_channel_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_notify_channel
    ADD CONSTRAINT spc_notify_channel_pkey PRIMARY KEY (id);


--
-- Name: spc_notify_channel spc_notify_channel_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_collect_task; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_collect_task (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    collect_freq character varying(32) NOT NULL,
    last_value numeric(12,4),
    last_at timestamp with time zone,
    next_due_at timestamp with time zone,
    status character varying(16) DEFAULT '待采集'::character varying NOT NULL,
    is_planned_downtime boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    collect_mode character varying(16) DEFAULT 'MANUAL'::character varying NOT NULL,
    collector character varying(64),
    due_reminded boolean DEFAULT false NOT NULL
);


--
-- Name: COLUMN spc_collect_task.collect_mode; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_collect_task.collect_mode IS '采集模式: MANUAL(手动录入)/OPC(设备直连)/FILE(文件导入)/MES(MES对接)/AUTO(定时自动采集)';


--
-- Name: COLUMN spc_collect_task.collector; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_collect_task.collector IS '采集任务指定接收人(通知接收者);为空时回退"班组长"';


--
-- Name: COLUMN spc_collect_task.due_reminded; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_collect_task.due_reminded IS '临期提醒是否已发送,避免重复提醒;每次顺延/创建时复位为 false';


--
-- Name: spc_collect_task spc_collect_task_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_collect_task
    ADD CONSTRAINT spc_collect_task_pkey PRIMARY KEY (id);


--
-- Name: spc_collect_task spc_collect_task_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_collect_task spc_collect_task_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_capability; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_capability (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    period_type character varying(8) NOT NULL,
    period_value character varying(16) NOT NULL,
    cpk numeric(6,3),
    ppk numeric(6,3),
    level character varying(32),
    sample_count integer,
    usl numeric(12,4),
    lsl numeric(12,4),
    calc_window_days integer DEFAULT 30 NOT NULL,
    calc_at timestamp with time zone DEFAULT now() NOT NULL,
    cp numeric(10,3),
    pp numeric(10,3),
    calc_note character varying(255)
);


--
-- Name: TABLE spc_capability; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_capability IS '过程能力快照(CPK/PPK,月度趋势)';


--
-- Name: COLUMN spc_capability.cp; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_capability.cp IS '过程潜力指数 Cp = (USL-LSL)/(6σ组内)';


--
-- Name: COLUMN spc_capability.pp; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_capability.pp IS '过程性能指数 Pp = (USL-LSL)/(6σ整体)';


--
-- Name: COLUMN spc_capability.calc_note; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_capability.calc_note IS '能力无法计算的原因诊断(如未配置规格限/样本不足/数据无变异)';


--
-- Name: spc_capability spc_capability_param_id_period_type_period_value_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_capability
    ADD CONSTRAINT spc_capability_param_id_period_type_period_value_key UNIQUE (param_id, period_type, period_value);


--
-- Name: spc_capability spc_capability_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_capability
    ADD CONSTRAINT spc_capability_pkey PRIMARY KEY (id);


--
-- Name: spc_capability spc_capability_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_capability spc_capability_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_notify_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_notify_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    alarm_id uuid NOT NULL,
    channel character varying(16) NOT NULL,
    channel_name character varying(32),
    message text NOT NULL,
    status character varying(16) NOT NULL,
    sent_at timestamp with time zone,
    error text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE spc_notify_record; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_notify_record IS 'SPC 推送通知记录(报警触发,留痕)';


--
-- Name: spc_notify_record spc_notify_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_notify_record
    ADD CONSTRAINT spc_notify_record_pkey PRIMARY KEY (id);


--
-- Name: idx_spc_notify_record_alarm; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_notify_record_alarm ON ops.spc_notify_record USING btree (org_id, alarm_id, created_at DESC);


--
-- Name: spc_notify_record spc_notify_record_alarm_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_notify_record spc_notify_record_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_spec_standard; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_spec_standard (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    material character varying(200) NOT NULL,
    proc_name character varying(200) NOT NULL,
    spec_lower numeric,
    spec_upper numeric,
    target_value numeric,
    unit character varying(32),
    chart_type character varying(32) DEFAULT 'Xbar-R'::character varying,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false,
    version integer DEFAULT 0
);


--
-- Name: TABLE spc_spec_standard; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_spec_standard IS 'SPC 标准线管理:按(组织+物料+工序)定义规格上下限/目标值/单位';


--
-- Name: COLUMN spc_spec_standard.org_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.org_id IS '组织ID(分公司/车间)';


--
-- Name: COLUMN spc_spec_standard.material; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.material IS '物料名称';


--
-- Name: COLUMN spc_spec_standard.proc_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.proc_name IS '工序名称';


--
-- Name: COLUMN spc_spec_standard.spec_lower; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.spec_lower IS '规格下限(LSL)';


--
-- Name: COLUMN spc_spec_standard.spec_upper; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.spec_upper IS '规格上限(USL)';


--
-- Name: COLUMN spc_spec_standard.target_value; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.target_value IS '目标值(设计中心值)';


--
-- Name: COLUMN spc_spec_standard.unit; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.unit IS '单位(mm/g/...)';


--
-- Name: COLUMN spc_spec_standard.chart_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.chart_type IS '默认控制图类型:Xbar-R/Xbar-S/I-MR/P';


--
-- Name: COLUMN spc_spec_standard.version; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_spec_standard.version IS '乐观锁';


--
-- Name: spc_spec_standard spc_spec_standard_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_spec_standard
    ADD CONSTRAINT spc_spec_standard_pkey PRIMARY KEY (id);


--
-- Name: spc_spec_standard uk_spec_standard; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_spec_standard
    ADD CONSTRAINT uk_spec_standard UNIQUE (org_id, material, proc_name);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_process; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_process (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    process_name character varying(64) NOT NULL,
    process_code character varying(32),
    description character varying(255),
    sort_no integer DEFAULT 0,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE spc_process; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_process IS 'SPC 工序主数据(参数的父级分组:装配/焊接/检测/系统...)';


--
-- Name: COLUMN spc_process.org_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.org_id IS '组织ID(分公司/车间)';


--
-- Name: COLUMN spc_process.process_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.process_name IS '工序名称(如 装配/焊接/检测/系统)';


--
-- Name: COLUMN spc_process.process_code; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.process_code IS '工序编码(业务可选)';


--
-- Name: COLUMN spc_process.description; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.description IS '工序说明';


--
-- Name: COLUMN spc_process.sort_no; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.sort_no IS '展示排序,升序';


--
-- Name: COLUMN spc_process.is_active; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_process.is_active IS '是否启用';


--
-- Name: spc_process spc_process_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_process
    ADD CONSTRAINT spc_process_pkey PRIMARY KEY (id);


--
-- Name: spc_process uk_spc_process_org_name; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_process
    ADD CONSTRAINT uk_spc_process_org_name UNIQUE (org_id, process_name);


--
-- Name: spc_process spc_process_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: spc_param_product; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.spc_param_product (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    param_id uuid NOT NULL,
    product_name character varying(128) NOT NULL,
    part_no character varying(64),
    kind character varying(16) DEFAULT 'product'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_spc_pp_kind CHECK (((kind)::text = ANY ((ARRAY['material'::character varying, 'semi'::character varying, 'product'::character varying])::text[])))
);


--
-- Name: TABLE spc_param_product; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.spc_param_product IS 'SPC 参数-产品关联(多对多, kind 区分物料/成品)';


--
-- Name: COLUMN spc_param_product.kind; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.spc_param_product.kind IS 'material=物料(来料首件) / product=成品(产线首件)';


--
-- Name: spc_param_product spc_param_product_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.spc_param_product
    ADD CONSTRAINT spc_param_product_pkey PRIMARY KEY (id);


--
-- Name: idx_spc_pp_org; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_pp_org ON ops.spc_param_product USING btree (org_id);


--
-- Name: idx_spc_pp_param; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_spc_pp_param ON ops.spc_param_product USING btree (param_id);


--
-- Name: uq_spc_pp_param_prod; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uq_spc_pp_param_prod ON ops.spc_param_product USING btree (param_id, product_name, COALESCE(part_no, ''::character varying), kind) WHERE (is_deleted = false);


--
-- Name: spc_param_product spc_param_product_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: spc_param_product spc_param_product_param_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



