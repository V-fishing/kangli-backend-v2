-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [FIA 检验 (FIA)]
-- ----------------------------------------------------------------------------
-- 板块 : FIA 检验 (FIA)
-- 内容 : 12 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: fia_insp_std; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_insp_std (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    code character varying(32) NOT NULL,
    material character varying(128) NOT NULL,
    proc_name character varying(32) NOT NULL,
    aql character varying(16),
    inspect_level character varying(4),
    sample_plan character varying(16),
    ctq_text character varying(255),
    std_version character varying(16) NOT NULL,
    status character varying(16) DEFAULT '草稿'::character varying NOT NULL,
    prev_version_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    part_no character varying(64),
    supplier_id uuid,
    category character varying(16),
    std_name character varying(128),
    is_default boolean DEFAULT false NOT NULL,
    spc_process_id uuid
);


--
-- Name: TABLE fia_insp_std; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_insp_std IS '检验标准库(主数据,物料+工序按公司唯一生效版本)';


--
-- Name: COLUMN fia_insp_std.part_no; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std.part_no IS '物料编码(来料批次驱动匹配键)';


--
-- Name: COLUMN fia_insp_std.supplier_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std.supplier_id IS '供应商ID(来料批次驱动匹配键)';


--
-- Name: COLUMN fia_insp_std.category; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std.category IS '品类: material=物料 / semi=半成品 / product=成品';


--
-- Name: COLUMN fia_insp_std.std_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std.std_name IS '标准名称';


--
-- Name: COLUMN fia_insp_std.is_default; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std.is_default IS '是否默认标准';


--
-- Name: fia_insp_std fia_insp_std_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_code_key UNIQUE (code);


--
-- Name: fia_insp_std fia_insp_std_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_std
    ADD CONSTRAINT fia_insp_std_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_insp_std_part_sup_proc; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX idx_fia_insp_std_part_sup_proc ON ops.fia_insp_std USING btree (org_id, part_no, supplier_id, proc_name) WHERE ((is_deleted = false) AND ((status)::text = '生效'::text) AND (part_no IS NOT NULL));


--
-- Name: fia_insp_std fia_insp_std_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_std fia_insp_std_prev_version_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_std fia_insp_std_spc_process_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_std fia_insp_std_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_insp_std_item; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_insp_std_item (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    std_id uuid NOT NULL,
    seq integer NOT NULL,
    item_name character varying(128) NOT NULL,
    is_ctq boolean DEFAULT false NOT NULL,
    std_value character varying(32),
    tolerance character varying(32),
    unit character varying(16),
    value_type character varying(8),
    enum_values character varying(128),
    is_deleted boolean DEFAULT false NOT NULL,
    pass_values character varying(128),
    upper_limit numeric,
    lower_limit numeric,
    item_type character varying(16),
    chart_types character varying(64)
);


--
-- Name: COLUMN fia_insp_std_item.pass_values; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std_item.pass_values IS '合格值(逗号分隔);枚举型实测命中即判合格,否则不合格';


--
-- Name: COLUMN fia_insp_std_item.upper_limit; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std_item.upper_limit IS '规格上限(数值型自动判定用)';


--
-- Name: COLUMN fia_insp_std_item.lower_limit; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std_item.lower_limit IS '规格下限(数值型自动判定用)';


--
-- Name: COLUMN fia_insp_std_item.item_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std_item.item_type IS '项目类型(numeric/enum/text)';


--
-- Name: COLUMN fia_insp_std_item.chart_types; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_std_item.chart_types IS '推荐控制图类型集合(逗号分隔): 数值→Xbar-R(可加Xbar-S/I-MR), 枚举/文本→P(可加NP/C/U)';


--
-- Name: fia_insp_std_item fia_insp_std_item_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_std_item
    ADD CONSTRAINT fia_insp_std_item_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_insp_std_item_std_id; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_insp_std_item_std_id ON ops.fia_insp_std_item USING btree (std_id);


--
-- Name: uq_fia_insp_std_item_std_seq; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uq_fia_insp_std_item_std_seq ON ops.fia_insp_std_item USING btree (std_id, seq) WHERE (is_deleted = false);


--
-- Name: fia_insp_std_item fia_insp_std_item_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_std_item fia_insp_std_item_std_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_trigger_type; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_trigger_type (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    name character varying(32) NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    description character varying(255),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: fia_trigger_type fia_trigger_type_name_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_trigger_type
    ADD CONSTRAINT fia_trigger_type_name_key UNIQUE (name);


--
-- Name: fia_trigger_type fia_trigger_type_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_trigger_type
    ADD CONSTRAINT fia_trigger_type_pkey PRIMARY KEY (id);


--
-- Name: fia_trigger_type fia_trigger_type_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_insp_item; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_insp_item (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    task_id uuid NOT NULL,
    seq integer NOT NULL,
    item_name character varying(128) NOT NULL,
    is_ctq boolean DEFAULT false NOT NULL,
    std_value character varying(32),
    tolerance character varying(32),
    unit character varying(16),
    measured_value character varying(32),
    judge character varying(16),
    std_item_id uuid
);


--
-- Name: TABLE fia_insp_item; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_insp_item IS '检验项目录入(CTQ 超差整单不合格)';


--
-- Name: fia_insp_item fia_insp_item_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_item
    ADD CONSTRAINT fia_insp_item_pkey PRIMARY KEY (id);


--
-- Name: fia_insp_item fia_insp_item_task_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_item
    ADD CONSTRAINT fia_insp_item_task_id_seq_key UNIQUE (task_id, seq);


--
-- Name: fia_insp_item fia_insp_item_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_item fia_insp_item_std_item_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_item fia_insp_item_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_task; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_task (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    code character varying(32) NOT NULL,
    wo_no character varying(32) NOT NULL,
    line_name character varying(64) NOT NULL,
    product_name character varying(128) NOT NULL,
    proc_name character varying(32) NOT NULL,
    trigger_type character varying(32) NOT NULL,
    std_id uuid NOT NULL,
    std_version character varying(16) NOT NULL,
    aql character varying(16),
    sample_size integer,
    sample_count integer,
    batch_no character varying(32),
    status character varying(16) DEFAULT '待检'::character varying NOT NULL,
    overall_judge character varying(16),
    inspector_id uuid,
    is_urgent boolean DEFAULT false NOT NULL,
    sla_due_at timestamp with time zone,
    is_overdue boolean DEFAULT false NOT NULL,
    disposition character varying(16),
    remark text,
    submitted_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    reviewer_id uuid,
    reviewed_at timestamp with time zone,
    approver_id uuid,
    approved_at timestamp with time zone,
    part_no character varying(64),
    supplier_id uuid,
    lot_id uuid,
    source character varying(16) DEFAULT 'FACTORY'::character varying,
    item_total integer DEFAULT 0,
    pass_count integer DEFAULT 0,
    fail_count integer DEFAULT 0,
    pass_rate numeric(5,4) DEFAULT 0,
    category character varying(16),
    tool_id uuid,
    CONSTRAINT chk_fia_task_category CHECK (((category IS NULL) OR ((category)::text = ANY ((ARRAY['material'::character varying, 'semi'::character varying, 'product'::character varying])::text[]))))
);


--
-- Name: TABLE fia_task; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_task IS '首件检验任务(校验单)';


--
-- Name: COLUMN fia_task.reviewer_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.reviewer_id IS '复核人(双签名第二签)';


--
-- Name: COLUMN fia_task.reviewed_at; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.reviewed_at IS '复核签名时间';


--
-- Name: COLUMN fia_task.approver_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.approver_id IS '批准人(三级签名第三签)';


--
-- Name: COLUMN fia_task.approved_at; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.approved_at IS '批准签名时间';


--
-- Name: COLUMN fia_task.lot_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.lot_id IS '来源来料批次ID';


--
-- Name: COLUMN fia_task.source; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.source IS '来源: FACTORY(产线首件) / SUPPLIER(供应商来料首件)';


--
-- Name: COLUMN fia_task.item_total; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.item_total IS '检验项总数';


--
-- Name: COLUMN fia_task.pass_count; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.pass_count IS '合格数';


--
-- Name: COLUMN fia_task.fail_count; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.fail_count IS '不合格数';


--
-- Name: COLUMN fia_task.pass_rate; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.pass_rate IS '合格率(比例 0~1,=合格数/检验项总数)';


--
-- Name: COLUMN fia_task.category; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.category IS '任务分类: material=物料 / semi=半成品 / product=成品(可空, 老任务按 source 派生)';


--
-- Name: COLUMN fia_task.tool_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_task.tool_id IS '关联工装 ID(仅 source=TOOLING 时填入，用于不合格回写锁定)';


--
-- Name: fia_task fia_task_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_code_key UNIQUE (code);


--
-- Name: fia_task fia_task_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_task
    ADD CONSTRAINT fia_task_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_task_source; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_task_source ON ops.fia_task USING btree (source) WHERE (is_deleted = false);


--
-- Name: idx_fia_task_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_task_status ON ops.fia_task USING btree (org_id, status, is_overdue);


--
-- Name: idx_fia_task_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_task_wo ON ops.fia_task USING btree (org_id, wo_no);


--
-- Name: fia_task fia_task_lot_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_task fia_task_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_task fia_task_std_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_task fia_task_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_approval; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_approval (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    code character varying(32) NOT NULL,
    approval_type character varying(16) NOT NULL,
    wo_no character varying(32) NOT NULL,
    task_id uuid,
    reason text NOT NULL,
    applicant_id uuid NOT NULL,
    apply_at timestamp with time zone DEFAULT now() NOT NULL,
    status character varying(16) DEFAULT '待审批'::character varying NOT NULL,
    approver_id uuid,
    approve_opinion text,
    approve_at timestamp with time zone,
    esign_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE fia_approval; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_approval IS '首件审批(豁免/紧急放行/让步接收)';


--
-- Name: fia_approval fia_approval_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_approval
    ADD CONSTRAINT fia_approval_code_key UNIQUE (code);


--
-- Name: fia_approval fia_approval_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_approval
    ADD CONSTRAINT fia_approval_pkey PRIMARY KEY (id);


--
-- Name: fia_approval fia_approval_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_approval fia_approval_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_archived_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_archived_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    report_no character varying(32) NOT NULL,
    task_id uuid NOT NULL,
    wo_no character varying(32) NOT NULL,
    archive_date date NOT NULL,
    status character varying(16) DEFAULT '已归档'::character varying NOT NULL,
    pdf_ref character varying(255) NOT NULL,
    report_hash character(64) NOT NULL,
    retention_until date NOT NULL
);


--
-- Name: TABLE fia_archived_report; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_archived_report IS '首件检验归档报告(15 年留存)';


--
-- Name: fia_archived_report fia_archived_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_archived_report
    ADD CONSTRAINT fia_archived_report_pkey PRIMARY KEY (id);


--
-- Name: fia_archived_report fia_archived_report_report_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_archived_report
    ADD CONSTRAINT fia_archived_report_report_no_key UNIQUE (report_no);


--
-- Name: fia_archived_report fia_archived_report_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_archived_report fia_archived_report_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_task_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_task_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    task_id uuid NOT NULL,
    node_seq integer NOT NULL,
    node_name character varying(64) NOT NULL,
    op_time timestamp with time zone DEFAULT now() NOT NULL,
    operator character varying(64) NOT NULL,
    op_type character varying(8) NOT NULL,
    is_done boolean DEFAULT true NOT NULL
);


--
-- Name: fia_task_log fia_task_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_task_log
    ADD CONSTRAINT fia_task_log_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_log_task; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_log_task ON ops.fia_task_log USING btree (org_id, task_id);


--
-- Name: fia_task_log fia_task_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_task_log fia_task_log_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_intercept_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_intercept_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    intercept_mode character varying(16) NOT NULL,
    multi_trigger_mode character varying(16) NOT NULL,
    sla_hours numeric(4,1) DEFAULT 2 NOT NULL,
    escalate_fail_count integer DEFAULT 3 NOT NULL
);


--
-- Name: fia_intercept_config fia_intercept_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_intercept_config
    ADD CONSTRAINT fia_intercept_config_pkey PRIMARY KEY (id);


--
-- Name: fia_intercept_config fia_intercept_config_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_sign_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_sign_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    sign_methods character varying(64)[] NOT NULL,
    sign_nodes character varying(16) NOT NULL,
    sign_granularity character varying(16) NOT NULL,
    lock_after_fail integer DEFAULT 3 NOT NULL,
    lock_minutes integer DEFAULT 5 NOT NULL
);


--
-- Name: fia_sign_config fia_sign_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_sign_config
    ADD CONSTRAINT fia_sign_config_pkey PRIMARY KEY (id);


--
-- Name: fia_sign_config uk_fia_sign_config_org; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_sign_config
    ADD CONSTRAINT uk_fia_sign_config_org UNIQUE (org_id);


--
-- Name: fia_sign_config fia_sign_config_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_wo_lock; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_wo_lock (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    wo_no character varying(64) NOT NULL,
    lock_status character varying(16) NOT NULL,
    lock_reason character varying(64),
    locked_at timestamp with time zone DEFAULT now() NOT NULL,
    wip_hold boolean DEFAULT false NOT NULL,
    unlock_type character varying(16),
    unlocked_at timestamp with time zone,
    approver_id uuid,
    release_reason character varying(255),
    trace_tag character varying(64),
    task_code character varying(64),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE fia_wo_lock; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_wo_lock IS '首件工单锁定记录:锁定(首件未完成/不合格)->解锁(自动/紧急放行),全程留痕';


--
-- Name: fia_wo_lock fia_wo_lock_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_wo_lock
    ADD CONSTRAINT fia_wo_lock_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_wo_lock_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fia_wo_lock_wo ON ops.fia_wo_lock USING btree (org_id, wo_no);


--
-- Name: fia_wo_lock fia_wo_lock_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: fia_insp_plan; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.fia_insp_plan (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    plan_code character varying(32) NOT NULL,
    plan_name character varying(64) NOT NULL,
    material_category character varying(64) NOT NULL,
    proc_name character varying(64) NOT NULL,
    std_id uuid,
    aql numeric(5,2) DEFAULT 1.0,
    sample_level character varying(8) DEFAULT 'II'::character varying,
    sample_plan character varying(8) DEFAULT '单次'::character varying,
    severity character varying(8) DEFAULT '一般'::character varying,
    is_active boolean DEFAULT true NOT NULL,
    sort_order integer DEFAULT 0,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    supplier_id uuid
);


--
-- Name: TABLE fia_insp_plan; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.fia_insp_plan IS '来料检验计划:物料分类→工序→标准→AQL抽样方案';


--
-- Name: COLUMN fia_insp_plan.aql; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.fia_insp_plan.aql IS 'AQL值(如1.0/0.65/2.5),用于查GB/T2828.1抽样表';


--
-- Name: fia_insp_plan fia_insp_plan_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.fia_insp_plan
    ADD CONSTRAINT fia_insp_plan_pkey PRIMARY KEY (id);


--
-- Name: idx_fia_insp_plan_cat_sup_proc; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX idx_fia_insp_plan_cat_sup_proc ON ops.fia_insp_plan USING btree (org_id, material_category, supplier_id, proc_name) WHERE (is_deleted = false);


--
-- Name: fia_insp_plan fia_insp_plan_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_plan fia_insp_plan_std_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: fia_insp_plan fia_insp_plan_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



