-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 核心-来料/供应商/物料 (SQM-CORE)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 核心-来料/供应商/物料 (SQM-CORE)
-- 内容 : 13 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: sqm_supplier; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_no character varying(32) NOT NULL,
    supplier_code character varying(16) NOT NULL,
    name character varying(255) NOT NULL,
    credit_code character varying(32) NOT NULL,
    category character varying(32) NOT NULL,
    level character(1),
    status character varying(16) DEFAULT '合格'::character varying NOT NULL,
    score numeric(5,2),
    contact_person character varying(64),
    contact_phone character varying(32),
    address character varying(255),
    certs jsonb,
    last_audit_date date,
    next_audit_date date,
    observe_flag boolean DEFAULT false NOT NULL,
    sole_source_flag boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    ven_code character varying(32)
);


--
-- Name: TABLE sqm_supplier; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_supplier IS '供应商档案(主数据)';


--
-- Name: COLUMN sqm_supplier.ven_code; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_supplier.ven_code IS 'MES 供应商编号(如 VEN00417)';


--
-- Name: sqm_supplier sqm_supplier_credit_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier
    ADD CONSTRAINT sqm_supplier_credit_code_key UNIQUE (credit_code);


--
-- Name: sqm_supplier sqm_supplier_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier
    ADD CONSTRAINT sqm_supplier_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier sqm_supplier_supplier_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier
    ADD CONSTRAINT sqm_supplier_supplier_code_key UNIQUE (supplier_code);


--
-- Name: sqm_supplier sqm_supplier_supplier_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier
    ADD CONSTRAINT sqm_supplier_supplier_no_key UNIQUE (supplier_no);


--
-- Name: idx_supplier_ven; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_supplier_ven ON ops.sqm_supplier USING btree (ven_code);


--
-- Name: sqm_supplier sqm_supplier_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_cert; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_cert (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    cert_type character varying(32) NOT NULL,
    cert_name character varying(128) NOT NULL,
    cert_no character varying(64),
    issue_date date,
    expiry_date date NOT NULL,
    file_url character varying(512) NOT NULL,
    file_hash character(64) NOT NULL,
    cert_version integer NOT NULL,
    status character varying(16) NOT NULL,
    warn_level character varying(8),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_supplier_cert sqm_supplier_cert_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_cert
    ADD CONSTRAINT sqm_supplier_cert_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_cert sqm_supplier_cert_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_cert sqm_supplier_cert_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_performance; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_performance (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    period character(7) NOT NULL,
    score numeric(5,2) NOT NULL,
    delivery_score numeric(5,2),
    quality_score numeric(5,2),
    service_score numeric(5,2),
    incoming_pass_rate numeric(5,2),
    defect_rate numeric(6,3),
    rectify_timely_rate numeric(5,2),
    delivery_timely_rate numeric(5,2),
    compliance_rate numeric(5,2),
    level character(1),
    observe_flag boolean DEFAULT false NOT NULL,
    data_missing_flag boolean DEFAULT false NOT NULL
);


--
-- Name: sqm_supplier_performance sqm_supplier_performance_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_performance
    ADD CONSTRAINT sqm_supplier_performance_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_performance sqm_supplier_performance_supplier_id_period_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_performance
    ADD CONSTRAINT sqm_supplier_performance_supplier_id_period_key UNIQUE (supplier_id, period);


--
-- Name: sqm_supplier_performance sqm_supplier_performance_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_performance sqm_supplier_performance_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_escalation; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_escalation (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    current_level character(1) NOT NULL,
    quality_issue_count_6m integer NOT NULL,
    repeat_problem_count integer,
    suggested_action text NOT NULL,
    escalation_status character varying(16) NOT NULL,
    escalation_action character varying(32),
    notice_sent_flag boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_supplier_escalation sqm_supplier_escalation_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_escalation
    ADD CONSTRAINT sqm_supplier_escalation_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_escalation sqm_supplier_escalation_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_escalation sqm_supplier_escalation_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_share; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_share (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    part_no character varying(64) NOT NULL,
    share_ratio numeric(5,2) NOT NULL,
    effective_date date NOT NULL,
    change_reason text,
    prev_ratio numeric(5,2),
    linked_level character(1)
);


--
-- Name: sqm_supplier_share sqm_supplier_share_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_share
    ADD CONSTRAINT sqm_supplier_share_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_share sqm_supplier_share_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_share sqm_supplier_share_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_key_part_sn; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_key_part_sn (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    sn_code character varying(64) NOT NULL,
    lot_id uuid NOT NULL,
    part_no character varying(64) NOT NULL,
    part_name character varying(128),
    supplier_id uuid,
    status character varying(16) NOT NULL,
    line character varying(32),
    wo_no character varying(32),
    scan_time timestamp with time zone
);


--
-- Name: TABLE sqm_key_part_sn; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_key_part_sn IS '关键件 SN(逐件追溯,非关键件走树状)';


--
-- Name: sqm_key_part_sn sqm_key_part_sn_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_key_part_sn
    ADD CONSTRAINT sqm_key_part_sn_pkey PRIMARY KEY (id);


--
-- Name: sqm_key_part_sn sqm_key_part_sn_sn_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_key_part_sn
    ADD CONSTRAINT sqm_key_part_sn_sn_code_key UNIQUE (sn_code);


--
-- Name: idx_sn_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sn_wo ON ops.sqm_key_part_sn USING btree (org_id, wo_no);


--
-- Name: sqm_key_part_sn sqm_key_part_sn_lot_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_key_part_sn sqm_key_part_sn_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_key_part_sn sqm_key_part_sn_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_incoming_lot; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_incoming_lot (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    lot_no character varying(32) NOT NULL,
    supplier_id uuid,
    part_no character varying(64) NOT NULL,
    part_name character varying(128) NOT NULL,
    qty numeric(14,2) NOT NULL,
    unit character varying(16),
    incoming_date date NOT NULL,
    inspect_result character varying(16) NOT NULL,
    inspect_type character varying(16) NOT NULL,
    iqc_pass boolean DEFAULT false NOT NULL,
    po_no character varying(32),
    change_id uuid,
    is_key_part boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    used_qty numeric(14,2) DEFAULT 0,
    ven_code character varying(32),
    ext_json jsonb,
    material_barcode character varying(128),
    supplier_name text,
    due_date date,
    material_batch_no character varying(128)
);


--
-- Name: TABLE sqm_incoming_lot; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_incoming_lot IS '来料批次(追溯根)';


--
-- Name: COLUMN sqm_incoming_lot.supplier_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.supplier_id IS '供应商(来料批次;FIA 产线首件 FACTORY 来源无供应商时为 NULL)';


--
-- Name: COLUMN sqm_incoming_lot.used_qty; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.used_qty IS '已投料数量(追溯投料累加,防超领)';


--
-- Name: COLUMN sqm_incoming_lot.ven_code; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.ven_code IS 'MES 供应商编号(来料检验记录上的 VEN 编号)';


--
-- Name: COLUMN sqm_incoming_lot.ext_json; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.ext_json IS 'MES 来料检验原始行(JSONB 留存, 用于追溯/建树关联 material_barcode)';


--
-- Name: COLUMN sqm_incoming_lot.material_barcode; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.material_barcode IS '来料条码(MES material_inspection.material_barcode 映射, 用于关键件追溯关联)';


--
-- Name: COLUMN sqm_incoming_lot.supplier_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.supplier_name IS '来料供应商名称(MES material_inspection.supplier_name 直接映射, 覆盖旧快照)';


--
-- Name: COLUMN sqm_incoming_lot.due_date; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.due_date IS '计划到货日期(交付及时率计算依据;历史数据回填=实际到货日)';


--
-- Name: COLUMN sqm_incoming_lot.material_batch_no; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_lot.material_batch_no IS '物料批次号(MES material_inspection.material_batch_no 映射, 总表批次号列展示/搜索用)';


--
-- Name: sqm_incoming_lot sqm_incoming_lot_lot_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_incoming_lot
    ADD CONSTRAINT sqm_incoming_lot_lot_no_key UNIQUE (lot_no);


--
-- Name: sqm_incoming_lot sqm_incoming_lot_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_incoming_lot
    ADD CONSTRAINT sqm_incoming_lot_pkey PRIMARY KEY (id);


--
-- Name: idx_lot_org_created; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_org_created ON ops.sqm_incoming_lot USING btree (org_id, created_at DESC);


--
-- Name: idx_lot_org_lotno; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_org_lotno ON ops.sqm_incoming_lot USING btree (org_id, lot_no);


--
-- Name: idx_lot_part_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_part_no ON ops.sqm_incoming_lot USING btree (org_id, part_no);


--
-- Name: idx_lot_supplier; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_supplier ON ops.sqm_incoming_lot USING btree (org_id, supplier_id, incoming_date DESC);


--
-- Name: idx_lot_supplier_name; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_supplier_name ON ops.sqm_incoming_lot USING btree (org_id, supplier_name);


--
-- Name: idx_lot_supplier_part_iqc; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_supplier_part_iqc ON ops.sqm_incoming_lot USING btree (org_id, supplier_id, part_no, iqc_pass);


--
-- Name: idx_lot_trgm_part; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_trgm_part ON ops.sqm_incoming_lot USING gin (part_no ops.gin_trgm_ops);


--
-- Name: idx_lot_trgm_sup; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_lot_trgm_sup ON ops.sqm_incoming_lot USING gin (supplier_name ops.gin_trgm_ops);


--
-- Name: sqm_incoming_lot sqm_incoming_lot_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_incoming_lot sqm_incoming_lot_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_incoming_lot sqm_incoming_lot_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_incoming_abnormal; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_incoming_abnormal (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    abnormal_no character varying(32) NOT NULL,
    lot_id character varying(32) NOT NULL,
    supplier_id uuid NOT NULL,
    part_no character varying(64) NOT NULL,
    part_name character varying(128) NOT NULL,
    description text NOT NULL,
    qty integer NOT NULL,
    level character varying(8) NOT NULL,
    occur_date date NOT NULL,
    handler_id uuid,
    status character varying(16) DEFAULT '待处理'::character varying NOT NULL,
    disposal character varying(16),
    disposal_remark text,
    d8_id uuid,
    capa_id uuid,
    rectify_type character varying(8),
    overdue_days integer DEFAULT 0 NOT NULL,
    close_date date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    notice_date date,
    notice_content text,
    plan_date date,
    extension_approved boolean DEFAULT false,
    extension_date date,
    verify_result character varying(16),
    verify_comment text,
    verify_date timestamp with time zone,
    verify_by character varying(64),
    return_reason text,
    close_auditor character varying(64),
    incoming_qty integer,
    batch_no character varying(64),
    defect_qty integer
);


--
-- Name: TABLE sqm_incoming_abnormal; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_incoming_abnormal IS '来料异常整改单(严重1件/一般>=3件触发)';


--
-- Name: COLUMN sqm_incoming_abnormal.defect_qty; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_incoming_abnormal.defect_qty IS '不良数(来料异常)';


--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_abnormal_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_abnormal_no_key UNIQUE (abnormal_no);


--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_incoming_abnormal
    ADD CONSTRAINT sqm_incoming_abnormal_pkey PRIMARY KEY (id);


--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_capa_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_incoming_abnormal sqm_incoming_abnormal_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_measure; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_measure (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    measure_id character varying(32) NOT NULL,
    abnormal_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    content text NOT NULL,
    rootcause text NOT NULL,
    prevention text NOT NULL,
    deadline date NOT NULL,
    owner character varying(64) NOT NULL,
    submit_date date NOT NULL,
    evidence_files jsonb,
    esign_id uuid
);


--
-- Name: sqm_supplier_measure sqm_supplier_measure_measure_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_measure
    ADD CONSTRAINT sqm_supplier_measure_measure_id_key UNIQUE (measure_id);


--
-- Name: sqm_supplier_measure sqm_supplier_measure_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_measure
    ADD CONSTRAINT sqm_supplier_measure_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_measure sqm_supplier_measure_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_measure sqm_supplier_measure_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_supplier_measure sqm_supplier_measure_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_supplier_grade_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_supplier_grade_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    score_min numeric(5,2) NOT NULL,
    score_max numeric(5,2) NOT NULL,
    level character(1) NOT NULL,
    observe_first_year boolean DEFAULT true NOT NULL
);


--
-- Name: sqm_supplier_grade_rule sqm_supplier_grade_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_supplier_grade_rule
    ADD CONSTRAINT sqm_supplier_grade_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_supplier_grade_rule sqm_supplier_grade_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_material; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_material (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    part_no character varying(64) NOT NULL,
    part_name character varying(128) NOT NULL,
    spec_model character varying(128),
    category character varying(32),
    is_key_part boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_material sqm_material_part_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_material
    ADD CONSTRAINT sqm_material_part_no_key UNIQUE (part_no);


--
-- Name: sqm_material sqm_material_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_material
    ADD CONSTRAINT sqm_material_pkey PRIMARY KEY (id);


--
-- Name: sqm_material sqm_material_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_customer; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_customer (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    customer_code character varying(32) NOT NULL,
    customer_name character varying(128) NOT NULL,
    contact_person character varying(64),
    contact_phone character varying(32),
    address character varying(255),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_customer sqm_customer_customer_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_customer
    ADD CONSTRAINT sqm_customer_customer_code_key UNIQUE (customer_code);


--
-- Name: sqm_customer sqm_customer_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_customer
    ADD CONSTRAINT sqm_customer_pkey PRIMARY KEY (id);


--
-- Name: sqm_customer sqm_customer_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_iqc_inspect_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_iqc_inspect_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    lot_id uuid,
    material_code character varying(64),
    material_name character varying(128),
    inspect_result character varying(16) DEFAULT 'PASS'::character varying,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone,
    updated_by uuid,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_iqc_inspect_record; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_iqc_inspect_record IS 'IQC 来料检验记录(历史手工表,全新库由 V91 幂等补建,兼容清理守卫引用)';


--
-- Name: sqm_iqc_inspect_record sqm_iqc_inspect_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_iqc_inspect_record
    ADD CONSTRAINT sqm_iqc_inspect_record_pkey PRIMARY KEY (id);


--
-- Name: idx_sqm_iqc_inspect_record_lot; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_iqc_inspect_record_lot ON ops.sqm_iqc_inspect_record USING btree (lot_id) WHERE (is_deleted = false);


--
-- PostgreSQL database dump complete
--



