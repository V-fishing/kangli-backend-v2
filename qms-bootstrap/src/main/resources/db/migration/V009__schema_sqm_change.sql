-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 变更 (SQM-CHANGE)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 变更 (SQM-CHANGE)
-- 内容 : 8 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: sqm_change_strict_inspect; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_strict_inspect (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    strict_no character varying(32) NOT NULL,
    change_id uuid NOT NULL,
    lot_id character varying(64),
    inspect_type character varying(16) DEFAULT '加严'::character varying NOT NULL,
    aql_level character varying(16) NOT NULL,
    result character varying(16) DEFAULT '待检'::character varying NOT NULL,
    inspect_date date,
    seq integer NOT NULL,
    total_seq integer DEFAULT 3 NOT NULL,
    restored boolean DEFAULT false NOT NULL
);


--
-- Name: TABLE sqm_change_strict_inspect; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_change_strict_inspect IS '加严检验跟踪(连续3批合格恢复/不合格回滚)';


--
-- Name: sqm_change_strict_inspect sqm_change_strict_inspect_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_strict_inspect
    ADD CONSTRAINT sqm_change_strict_inspect_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_strict_inspect sqm_change_strict_inspect_strict_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_strict_inspect
    ADD CONSTRAINT sqm_change_strict_inspect_strict_no_key UNIQUE (strict_no);


--
-- Name: sqm_change_strict_inspect sqm_change_strict_inspect_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_strict_inspect sqm_change_strict_inspect_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_doc; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_doc (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_id uuid NOT NULL,
    doc_type character varying(16) NOT NULL,
    submitted boolean DEFAULT false NOT NULL,
    file_path jsonb,
    submitted_by character varying(64),
    submitted_date timestamp with time zone
);


--
-- Name: sqm_change_doc sqm_change_doc_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_doc
    ADD CONSTRAINT sqm_change_doc_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_doc sqm_change_doc_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_doc sqm_change_doc_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_approval; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_approval (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_id uuid NOT NULL,
    approval_role character varying(16) NOT NULL,
    role_label character varying(32) NOT NULL,
    status character varying(16) DEFAULT 'pending'::character varying NOT NULL,
    operator character varying(64),
    operate_date timestamp with time zone,
    opinion text,
    has_veto boolean DEFAULT false NOT NULL,
    seq_order integer NOT NULL,
    esign_id uuid
);


--
-- Name: TABLE sqm_change_approval; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_change_approval IS '变更会签(质量/采购/研发并行 + 质量一票否决 + 试产串行)';


--
-- Name: sqm_change_approval sqm_change_approval_change_id_approval_role_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_approval
    ADD CONSTRAINT sqm_change_approval_change_id_approval_role_key UNIQUE (change_id, approval_role);


--
-- Name: sqm_change_approval sqm_change_approval_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_approval
    ADD CONSTRAINT sqm_change_approval_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_approval sqm_change_approval_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_approval sqm_change_approval_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_workflow_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_workflow_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_id uuid NOT NULL,
    step character varying(32) NOT NULL,
    operator character varying(64),
    operate_date timestamp with time zone,
    action_desc character varying(255),
    status character varying(16) NOT NULL
);


--
-- Name: sqm_change_workflow_log sqm_change_workflow_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_workflow_log
    ADD CONSTRAINT sqm_change_workflow_log_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_workflow_log sqm_change_workflow_log_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_workflow_log sqm_change_workflow_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_impact; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_impact (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_id uuid NOT NULL,
    affected_products jsonb,
    switch_date date,
    trial_qty integer,
    strict_plan text
);


--
-- Name: sqm_change_impact sqm_change_impact_change_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_impact
    ADD CONSTRAINT sqm_change_impact_change_id_key UNIQUE (change_id);


--
-- Name: sqm_change_impact sqm_change_impact_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_impact
    ADD CONSTRAINT sqm_change_impact_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_impact sqm_change_impact_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_impact sqm_change_impact_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_order; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_order (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_no character varying(32) NOT NULL,
    title character varying(255) NOT NULL,
    supplier_id uuid NOT NULL,
    part_no character varying(64) NOT NULL,
    change_type character varying(16) NOT NULL,
    reason text,
    applicant character varying(64),
    apply_date date NOT NULL,
    urgency character varying(8) NOT NULL,
    strict_flag boolean DEFAULT false NOT NULL,
    risk_pre_mark character varying(8),
    source character varying(16) NOT NULL,
    receive_frozen boolean DEFAULT false NOT NULL,
    status character varying(16) DEFAULT '待申请'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    verify_report text,
    risk_file text
);


--
-- Name: TABLE sqm_change_order; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_change_order IS '物料变更单(冻结收货,会签+一票否决)';


--
-- Name: COLUMN sqm_change_order.verify_report; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_change_order.verify_report IS '验证报告附件路径(相对 logs/files)';


--
-- Name: COLUMN sqm_change_order.risk_file; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_change_order.risk_file IS '风险评估附件路径(相对 logs/files)';


--
-- Name: sqm_change_order sqm_change_order_change_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_order
    ADD CONSTRAINT sqm_change_order_change_no_key UNIQUE (change_no);


--
-- Name: sqm_change_order sqm_change_order_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_order
    ADD CONSTRAINT sqm_change_order_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_order sqm_change_order_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_order sqm_change_order_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_sop_notice; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_sop_notice (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    change_id uuid NOT NULL,
    sop_file character varying(255) NOT NULL,
    version character varying(32) NOT NULL,
    update_content text,
    publish_date date,
    status character varying(16) DEFAULT '待更新'::character varying NOT NULL
);


--
-- Name: sqm_change_sop_notice sqm_change_sop_notice_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_sop_notice
    ADD CONSTRAINT sqm_change_sop_notice_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_sop_notice sqm_change_sop_notice_change_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_change_sop_notice sqm_change_sop_notice_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_change_risk_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_change_risk_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    change_type character varying(16) NOT NULL,
    risk_level character varying(8) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_change_risk_rule sqm_change_risk_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_change_risk_rule
    ADD CONSTRAINT sqm_change_risk_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_change_risk_rule sqm_change_risk_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



