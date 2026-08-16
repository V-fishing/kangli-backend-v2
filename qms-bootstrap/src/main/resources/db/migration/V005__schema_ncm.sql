-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [NCM 不合格/8D (NCM)]
-- ----------------------------------------------------------------------------
-- 板块 : NCM 不合格/8D (NCM)
-- 内容 : 11 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: ncm_defect_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_defect_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    defect_no character varying(32) NOT NULL,
    wo_no character varying(32) NOT NULL,
    process_code character varying(16) NOT NULL,
    defect_dict_code character varying(16) NOT NULL,
    severity character varying(8) NOT NULL,
    defect_count integer NOT NULL,
    batch_total integer,
    defect_rate numeric(6,3),
    device_code character varying(32),
    batch_no character varying(32),
    product_model character varying(64),
    operator_id uuid NOT NULL,
    source character varying(8) DEFAULT '手动'::character varying NOT NULL,
    device_payload jsonb,
    occurred_at timestamp with time zone DEFAULT now() NOT NULL,
    remark text,
    disposition character varying(16),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    stage character varying(16) DEFAULT '来料不良'::character varying NOT NULL,
    d8_no character varying(50),
    capa_no character varying(50),
    ca_no character varying(50),
    tool_id uuid,
    tool_no character varying(64)
);


--
-- Name: TABLE ncm_defect_record; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.ncm_defect_record IS '不良记录(6维绑定:类型/工序/设备/批次/时间/产品)';


--
-- Name: COLUMN ncm_defect_record.stage; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.ncm_defect_record.stage IS '不良阶段: 来料不良/半成品不良/成品不良/首件不良';


--
-- Name: ncm_defect_record ncm_defect_record_defect_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_record
    ADD CONSTRAINT ncm_defect_record_defect_no_key UNIQUE (defect_no);


--
-- Name: ncm_defect_record ncm_defect_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_record
    ADD CONSTRAINT ncm_defect_record_pkey PRIMARY KEY (id);


--
-- Name: idx_ncm_defect_multi; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_ncm_defect_multi ON ops.ncm_defect_record USING btree (org_id, defect_dict_code, process_code, device_code, batch_no, occurred_at DESC);


--
-- Name: idx_ncm_defect_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_ncm_defect_tool ON ops.ncm_defect_record USING btree (tool_id) WHERE ((is_deleted = false) AND (tool_id IS NOT NULL));


--
-- Name: idx_ncm_defect_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_ncm_defect_wo ON ops.ncm_defect_record USING btree (org_id, wo_no);


--
-- Name: ncm_defect_record ncm_defect_record_defect_dict_code_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: ncm_defect_record ncm_defect_record_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_defect_dict; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_defect_dict (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    code character varying(16) NOT NULL,
    name character varying(64) NOT NULL,
    category character varying(16) NOT NULL,
    level character varying(8) NOT NULL,
    status character varying(8) DEFAULT '启用'::character varying NOT NULL,
    reference_count integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE ncm_defect_dict; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.ncm_defect_dict IS '不良字典(主数据,停用不可复用)';


--
-- Name: ncm_defect_dict ncm_defect_dict_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_dict
    ADD CONSTRAINT ncm_defect_dict_code_key UNIQUE (code);


--
-- Name: ncm_defect_dict ncm_defect_dict_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_dict
    ADD CONSTRAINT ncm_defect_dict_pkey PRIMARY KEY (id);


--
-- Name: ncm_defect_dict ncm_defect_dict_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_realtime_snapshot; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_realtime_snapshot (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    snapshot_time timestamp with time zone NOT NULL,
    shift character varying(8) NOT NULL,
    defect_count integer NOT NULL,
    defect_rate numeric(6,3) NOT NULL,
    top_types jsonb,
    process_heatmap jsonb,
    data_freshness character varying(8) NOT NULL
);


--
-- Name: ncm_realtime_snapshot ncm_realtime_snapshot_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_realtime_snapshot
    ADD CONSTRAINT ncm_realtime_snapshot_pkey PRIMARY KEY (id);


--
-- Name: ncm_realtime_snapshot ncm_realtime_snapshot_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_filter_scheme; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_filter_scheme (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    scheme_name character varying(64) NOT NULL,
    owner_id uuid NOT NULL,
    filter_json jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: ncm_filter_scheme ncm_filter_scheme_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_filter_scheme
    ADD CONSTRAINT ncm_filter_scheme_pkey PRIMARY KEY (id);


--
-- Name: ncm_filter_scheme ncm_filter_scheme_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_bi_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_bi_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    report_no character varying(32) NOT NULL,
    report_type character varying(32) NOT NULL,
    period character varying(16) NOT NULL,
    generated_at timestamp with time zone DEFAULT now() NOT NULL,
    file_url character varying(255),
    status character varying(16) NOT NULL
);


--
-- Name: ncm_bi_report ncm_bi_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_bi_report
    ADD CONSTRAINT ncm_bi_report_pkey PRIMARY KEY (id);


--
-- Name: ncm_bi_report ncm_bi_report_report_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_bi_report
    ADD CONSTRAINT ncm_bi_report_report_no_key UNIQUE (report_no);


--
-- Name: ncm_bi_report ncm_bi_report_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_daily_report_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_daily_report_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    push_time time without time zone NOT NULL,
    receivers jsonb NOT NULL,
    enabled boolean DEFAULT true NOT NULL
);


--
-- Name: ncm_daily_report_config ncm_daily_report_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_daily_report_config
    ADD CONSTRAINT ncm_daily_report_config_pkey PRIMARY KEY (id);


--
-- Name: ncm_daily_report_config ncm_daily_report_config_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_alert_escalation; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_alert_escalation (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    level smallint NOT NULL,
    timeout_minutes integer NOT NULL,
    notify_role character varying(16) NOT NULL,
    off_hours_delay boolean DEFAULT true NOT NULL
);


--
-- Name: TABLE ncm_alert_escalation; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.ncm_alert_escalation IS 'ESC 三级升级(30min->班组长/60min->主管/120min->经理)';


--
-- Name: ncm_alert_escalation ncm_alert_escalation_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_alert_escalation
    ADD CONSTRAINT ncm_alert_escalation_pkey PRIMARY KEY (id);


--
-- Name: ncm_alert_escalation ncm_alert_escalation_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_trend_alert; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_trend_alert (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    alert_type character varying(16) NOT NULL,
    dimension character varying(32) NOT NULL,
    notify_role character varying(16) NOT NULL,
    triggered_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: ncm_trend_alert ncm_trend_alert_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_trend_alert
    ADD CONSTRAINT ncm_trend_alert_pkey PRIMARY KEY (id);


--
-- Name: ncm_trend_alert ncm_trend_alert_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_corrective_action; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_corrective_action (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    ca_no character varying(32) NOT NULL,
    defect_no character varying(32),
    issue text NOT NULL,
    owner character varying(64) NOT NULL,
    due_date date NOT NULL,
    status character varying(16) DEFAULT '待启动'::character varying NOT NULL,
    progress smallint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    owner_user_id character varying(36),
    source_ref_id character varying(64),
    source_type character varying(32)
);


--
-- Name: COLUMN ncm_corrective_action.owner_user_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.ncm_corrective_action.owner_user_id IS '纠正措施责任人用户 ID(对应 ops.sys_user.id)';


--
-- Name: ncm_corrective_action ncm_corrective_action_ca_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_corrective_action
    ADD CONSTRAINT ncm_corrective_action_ca_no_key UNIQUE (ca_no);


--
-- Name: ncm_corrective_action ncm_corrective_action_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_corrective_action
    ADD CONSTRAINT ncm_corrective_action_pkey PRIMARY KEY (id);


--
-- Name: idx_ncm_ca_src; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_ncm_ca_src ON ops.ncm_corrective_action USING btree (source_ref_id) WHERE (source_ref_id IS NOT NULL);


--
-- Name: ncm_corrective_action ncm_corrective_action_defect_no_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: ncm_corrective_action ncm_corrective_action_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: ncm_defect_trend_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_defect_trend_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    product_model character varying(80),
    granularity character varying(16) NOT NULL,
    period_value character varying(32) NOT NULL,
    summary_json jsonb,
    rule_snapshot jsonb,
    generated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64)
);


--
-- Name: ncm_defect_trend_report ncm_defect_trend_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_trend_report
    ADD CONSTRAINT ncm_defect_trend_report_pkey PRIMARY KEY (id);


--
-- Name: idx_trend_report_lookup; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trend_report_lookup ON ops.ncm_defect_trend_report USING btree (org_id, product_model, granularity, period_value) WHERE (is_deleted = false);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: ncm_defect_trend_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.ncm_defect_trend_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    consecutive_days integer DEFAULT 3 NOT NULL,
    use_mean_plus_2sigma boolean DEFAULT true NOT NULL,
    sigma_multiplier numeric(4,2) DEFAULT 2.0 NOT NULL,
    baseline_days integer DEFAULT 30 NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    created_by character varying(64),
    updated_by character varying(64)
);


--
-- Name: ncm_defect_trend_rule ncm_defect_trend_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.ncm_defect_trend_rule
    ADD CONSTRAINT ncm_defect_trend_rule_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--



