-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [体系/8D/CAPA/FMEA (QMS)]
-- ----------------------------------------------------------------------------
-- 板块 : 体系/8D/CAPA/FMEA (QMS)
-- 内容 : 19 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: qms_capa_action; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_capa_action (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    capa_id uuid NOT NULL,
    seq smallint NOT NULL,
    action_text text NOT NULL,
    done boolean DEFAULT false NOT NULL,
    complete_date date
);


--
-- Name: TABLE qms_capa_action; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_capa_action IS 'CAPA 措施明细';


--
-- Name: qms_capa_action qms_capa_action_capa_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_capa_action
    ADD CONSTRAINT qms_capa_action_capa_id_seq_key UNIQUE (capa_id, seq);


--
-- Name: qms_capa_action qms_capa_action_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_capa_action
    ADD CONSTRAINT qms_capa_action_pkey PRIMARY KEY (id);


--
-- Name: qms_capa_action qms_capa_action_capa_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_capa_action qms_capa_action_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_capa_trigger_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_capa_trigger_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    condition character varying(32) NOT NULL,
    auto_or_manual character varying(16) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE qms_capa_trigger_rule; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_capa_trigger_rule IS 'CAPA 触发规则(配置)';


--
-- Name: qms_capa_trigger_rule qms_capa_trigger_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_capa_trigger_rule
    ADD CONSTRAINT qms_capa_trigger_rule_pkey PRIMARY KEY (id);


--
-- Name: qms_capa_trigger_rule qms_capa_trigger_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_fmea_risk_track; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_fmea_risk_track (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    risk_id uuid NOT NULL,
    from_status character varying(16),
    to_status character varying(16) NOT NULL,
    operator character varying(64) NOT NULL,
    operate_time timestamp with time zone DEFAULT now() NOT NULL,
    action_note text,
    evidence text,
    esign_id uuid
);


--
-- Name: TABLE qms_fmea_risk_track; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_fmea_risk_track IS 'FMEA 高风险闭环跟踪日志';


--
-- Name: qms_fmea_risk_track qms_fmea_risk_track_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_fmea_risk_track
    ADD CONSTRAINT qms_fmea_risk_track_pkey PRIMARY KEY (id);


--
-- Name: qms_fmea_risk_track qms_fmea_risk_track_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_fmea_risk_track qms_fmea_risk_track_risk_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_fmea_template; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_fmea_template (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    template_no character varying(32) NOT NULL,
    fmea_type character varying(8) NOT NULL,
    items jsonb NOT NULL,
    rpn_threshold jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE qms_fmea_template; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_fmea_template IS 'FMEA 评估模板(配置主数据)';


--
-- Name: qms_fmea_template qms_fmea_template_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_fmea_template
    ADD CONSTRAINT qms_fmea_template_pkey PRIMARY KEY (id);


--
-- Name: qms_fmea_template qms_fmea_template_template_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_fmea_template
    ADD CONSTRAINT qms_fmea_template_template_no_key UNIQUE (template_no);


--
-- Name: qms_fmea_template qms_fmea_template_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_fmea_risk; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_fmea_risk (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    risk_no character varying(32) NOT NULL,
    fmea_type character varying(8) NOT NULL,
    product character varying(128) NOT NULL,
    process character varying(128) NOT NULL,
    failure_mode character varying(255) NOT NULL,
    severity_s smallint NOT NULL,
    occurrence_o smallint NOT NULL,
    detection_d smallint NOT NULL,
    rpn smallint NOT NULL,
    risk_level character varying(8) NOT NULL,
    high_risk_flag boolean DEFAULT false NOT NULL,
    status character varying(16) DEFAULT '待闭环'::character varying NOT NULL,
    action text,
    owner character varying(64),
    target_date date,
    evidence text,
    close_date date,
    change_order_id character varying(64),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    source_type character varying(32),
    source_id character varying(64),
    failure_effect text,
    failure_cause text,
    current_prevent_ctrl text,
    current_detect_ctrl text,
    suggest_measure text,
    owner_dept character varying(128),
    owner_dept_code character varying(32),
    note text,
    owner_user_id uuid,
    CONSTRAINT qms_fmea_risk_detection_d_check CHECK (((detection_d >= 1) AND (detection_d <= 10))),
    CONSTRAINT qms_fmea_risk_occurrence_o_check CHECK (((occurrence_o >= 1) AND (occurrence_o <= 10))),
    CONSTRAINT qms_fmea_risk_severity_s_check CHECK (((severity_s >= 1) AND (severity_s <= 10)))
);


--
-- Name: TABLE qms_fmea_risk; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_fmea_risk IS 'FMEA 高风险项(跨模块共享,RPN>=100 入清单)';


--
-- Name: COLUMN qms_fmea_risk.source_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_fmea_risk.source_type IS '自动触发来源类型(INCOMING_ABNORMAL/NCM_DEFECT),手工创建为 NULL';


--
-- Name: COLUMN qms_fmea_risk.source_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_fmea_risk.source_id IS '自动触发来源业务主键(异常/缺陷 id),手工创建为 NULL';


--
-- Name: qms_fmea_risk qms_fmea_risk_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_fmea_risk
    ADD CONSTRAINT qms_fmea_risk_pkey PRIMARY KEY (id);


--
-- Name: qms_fmea_risk qms_fmea_risk_risk_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_fmea_risk
    ADD CONSTRAINT qms_fmea_risk_risk_no_key UNIQUE (risk_no);


--
-- Name: idx_fmea_rpn; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fmea_rpn ON ops.qms_fmea_risk USING btree (org_id, rpn DESC);


--
-- Name: idx_fmea_source; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_fmea_source ON ops.qms_fmea_risk USING btree (source_type, source_id) WHERE (source_type IS NOT NULL);


--
-- Name: qms_fmea_risk qms_fmea_risk_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_esign_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_esign_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    biz_type character varying(32) NOT NULL,
    biz_id character varying(64) NOT NULL,
    sign_role character varying(16) NOT NULL,
    signer_user_id uuid NOT NULL,
    signer_username character varying(64) NOT NULL,
    sign_method character varying(16) NOT NULL,
    sign_time timestamp with time zone DEFAULT now() NOT NULL,
    client_ip character varying(64),
    content_hash character(64) NOT NULL,
    prev_hash character(64),
    chain_hash character(64) NOT NULL,
    status character varying(16) DEFAULT '成功'::character varying NOT NULL,
    fail_count integer DEFAULT 0 NOT NULL,
    is_rejected boolean DEFAULT false NOT NULL,
    remark text
);


--
-- Name: TABLE qms_esign_log; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_esign_log IS '电子签名哈希链日志(Part 11,不可篡改)';


--
-- Name: qms_esign_log qms_esign_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_esign_log
    ADD CONSTRAINT qms_esign_log_pkey PRIMARY KEY (id);


--
-- Name: idx_esign_biz; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_esign_biz ON ops.qms_esign_log USING btree (biz_type, biz_id);


--
-- Name: uk_esign_chain; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uk_esign_chain ON ops.qms_esign_log USING btree (chain_hash);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: qms_8d_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    d8_no character varying(32) NOT NULL,
    source character varying(16) NOT NULL,
    source_ref_id character varying(36),
    issue text NOT NULL,
    severity character varying(8) NOT NULL,
    current_stage character varying(4) DEFAULT 'D1'::character varying NOT NULL,
    status character varying(16) DEFAULT '进行中'::character varying NOT NULL,
    flow_type character varying(16),
    team character varying(512),
    capa_triggered boolean DEFAULT false NOT NULL,
    close_date date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    owner_user_name character varying(64),
    capa_id character varying(64),
    owner_user_id character varying(36)
);


--
-- Name: TABLE qms_8d_report; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_report IS '8D 整改报告(跨模块共享)';


--
-- Name: COLUMN qms_8d_report.owner_user_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_8d_report.owner_user_name IS '8D 报告负责人(姓名)';


--
-- Name: COLUMN qms_8d_report.capa_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_8d_report.capa_id IS '关联 CAPA 主键(36字符 UUID, 双向追溯)';


--
-- Name: COLUMN qms_8d_report.owner_user_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_8d_report.owner_user_id IS '8D 报告负责人用户 ID(对应 ops.sys_user.id)';


--
-- Name: qms_8d_report qms_8d_report_d8_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_report
    ADD CONSTRAINT qms_8d_report_d8_no_key UNIQUE (d8_no);


--
-- Name: qms_8d_report qms_8d_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_report
    ADD CONSTRAINT qms_8d_report_pkey PRIMARY KEY (id);


--
-- Name: idx_8d_source; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_8d_source ON ops.qms_8d_report USING btree (org_id, source, source_ref_id);


--
-- Name: qms_8d_report qms_8d_report_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_8d_stage_history; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_stage_history (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    d8_id uuid NOT NULL,
    stage_code character varying(4) NOT NULL,
    operation character varying(64) NOT NULL,
    operator character varying(64) NOT NULL,
    operated_at timestamp with time zone DEFAULT now() NOT NULL,
    cost_hours character varying(16),
    note text
);


--
-- Name: TABLE qms_8d_stage_history; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_stage_history IS '8D 阶段变更历史(全量留痕)';


--
-- Name: qms_8d_stage_history qms_8d_stage_history_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_stage_history
    ADD CONSTRAINT qms_8d_stage_history_pkey PRIMARY KEY (id);


--
-- Name: idx_8d_hist; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_8d_hist ON ops.qms_8d_stage_history USING btree (org_id, d8_id);


--
-- Name: qms_8d_stage_history qms_8d_stage_history_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_8d_stage_history qms_8d_stage_history_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_8d_fishbone; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_fishbone (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    d8_id uuid NOT NULL,
    problem text NOT NULL,
    category character varying(4) NOT NULL,
    cause_text text NOT NULL,
    sort_order smallint
);


--
-- Name: TABLE qms_8d_fishbone; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_fishbone IS '8D 鱼骨图 5M1E 根因';


--
-- Name: qms_8d_fishbone qms_8d_fishbone_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_fishbone
    ADD CONSTRAINT qms_8d_fishbone_pkey PRIMARY KEY (id);


--
-- Name: qms_8d_fishbone qms_8d_fishbone_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_8d_fishbone qms_8d_fishbone_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_8d_stage_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_stage_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    stage_code character varying(4) NOT NULL,
    stage_name character varying(32) NOT NULL,
    sort_order smallint NOT NULL,
    need_approval boolean DEFAULT false NOT NULL,
    sla_duration interval,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE qms_8d_stage_config; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_stage_config IS '8D 阶段配置(主数据)';


--
-- Name: qms_8d_stage_config qms_8d_stage_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_stage_config
    ADD CONSTRAINT qms_8d_stage_config_pkey PRIMARY KEY (id);


--
-- Name: qms_8d_stage_config qms_8d_stage_config_stage_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_stage_config
    ADD CONSTRAINT qms_8d_stage_config_stage_code_key UNIQUE (stage_code);


--
-- Name: qms_8d_stage_config qms_8d_stage_config_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_capa; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_capa (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    capa_no character varying(32) NOT NULL,
    d8_id uuid,
    abnormal_id character varying(64),
    issue text NOT NULL,
    trigger_stage character varying(4),
    trigger_type character varying(16) NOT NULL,
    trigger_condition character varying(32),
    capa_type character varying(16) NOT NULL,
    rootcause text,
    action_plan text,
    owner character varying(64) NOT NULL,
    due_date date NOT NULL,
    progress smallint DEFAULT 0 NOT NULL,
    status character varying(16) DEFAULT '待启动'::character varying NOT NULL,
    esign_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    source_ref_id character varying(64),
    source_type character varying(32),
    init_comment text,
    init_approved_by character varying(100),
    init_approved_at timestamp without time zone,
    owner_user_id character varying(36)
);


--
-- Name: TABLE qms_capa; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_capa IS 'CAPA 纠正与预防措施(跨模块共享)';


--
-- Name: COLUMN qms_capa.source_ref_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_capa.source_ref_id IS '通用来源单据 ID(内审不符合项/不良记录等)';


--
-- Name: COLUMN qms_capa.source_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_capa.source_type IS '来源类型(审核不符合项/不良记录/8D/来料异常)';


--
-- Name: COLUMN qms_capa.owner_user_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_capa.owner_user_id IS 'CAPA 负责人用户 ID(对应 ops.sys_user.id)';


--
-- Name: qms_capa qms_capa_capa_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_capa
    ADD CONSTRAINT qms_capa_capa_no_key UNIQUE (capa_no);


--
-- Name: qms_capa qms_capa_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_capa
    ADD CONSTRAINT qms_capa_pkey PRIMARY KEY (id);


--
-- Name: idx_capa_d8; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_capa_d8 ON ops.qms_capa USING btree (org_id, d8_id);


--
-- Name: idx_capa_source; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_capa_source ON ops.qms_capa USING btree (org_id, source_type, source_ref_id);


--
-- Name: qms_capa qms_capa_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_capa qms_capa_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_quality_goal; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_quality_goal (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    goal_name character varying(120) NOT NULL,
    goal_type character varying(32) NOT NULL,
    period character varying(32) NOT NULL,
    target_value numeric(12,4) NOT NULL,
    actual_value numeric(12,4) DEFAULT 0 NOT NULL,
    unit character varying(16) DEFAULT '%'::character varying NOT NULL,
    owner character varying(80),
    deadline timestamp without time zone,
    remark character varying(400),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: qms_quality_goal qms_quality_goal_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_quality_goal
    ADD CONSTRAINT qms_quality_goal_pkey PRIMARY KEY (id);


--
-- Name: idx_qms_goal_org_period; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_goal_org_period ON ops.qms_quality_goal USING btree (org_id, period) WHERE (is_deleted = false);


--
-- Name: qms_quality_goal qms_quality_goal_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_internal_audit; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_internal_audit (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    audit_no character varying(32) NOT NULL,
    audit_name character varying(160) NOT NULL,
    audit_scope character varying(240),
    plan_date timestamp without time zone,
    auditor character varying(80),
    status character varying(16) DEFAULT 'PLANNED'::character varying NOT NULL,
    remark character varying(400),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: qms_internal_audit qms_internal_audit_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_internal_audit
    ADD CONSTRAINT qms_internal_audit_pkey PRIMARY KEY (id);


--
-- Name: idx_qms_audit_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_audit_org_status ON ops.qms_internal_audit USING btree (org_id, status) WHERE (is_deleted = false);


--
-- Name: uk_qms_audit_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uk_qms_audit_no ON ops.qms_internal_audit USING btree (org_id, audit_no) WHERE (is_deleted = false);


--
-- Name: qms_internal_audit qms_internal_audit_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_audit_nc; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_audit_nc (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    audit_id uuid,
    nc_no character varying(32) NOT NULL,
    nc_desc text NOT NULL,
    clause character varying(80),
    severity character varying(16) DEFAULT 'MINOR'::character varying NOT NULL,
    status character varying(16) DEFAULT 'OPEN'::character varying NOT NULL,
    owner character varying(80),
    due_date timestamp without time zone,
    corrective text,
    verify_result character varying(400),
    closed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: qms_audit_nc qms_audit_nc_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_audit_nc
    ADD CONSTRAINT qms_audit_nc_pkey PRIMARY KEY (id);


--
-- Name: idx_qms_nc_audit; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_nc_audit ON ops.qms_audit_nc USING btree (audit_id) WHERE (is_deleted = false);


--
-- Name: idx_qms_nc_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_nc_org_status ON ops.qms_audit_nc USING btree (org_id, status) WHERE (is_deleted = false);


--
-- Name: qms_audit_nc qms_audit_nc_audit_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_audit_nc qms_audit_nc_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_8d_stage_detail; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_stage_detail (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    d8_id uuid NOT NULL,
    stage_code character varying(4) NOT NULL,
    content text,
    team_members character varying(512),
    owner character varying(64),
    plan_date date,
    actual_cost_hours numeric(10,2),
    approval_status character varying(16),
    approved_by character varying(64),
    approved_at timestamp with time zone,
    approval_comment text,
    esign_id uuid,
    evidence_files character varying(512),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE qms_8d_stage_detail; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_stage_detail IS '8D 各阶段内容/审批/SLA/证据明细';


--
-- Name: qms_8d_stage_detail qms_8d_stage_detail_d8_id_stage_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_stage_detail
    ADD CONSTRAINT qms_8d_stage_detail_d8_id_stage_code_key UNIQUE (d8_id, stage_code);


--
-- Name: qms_8d_stage_detail qms_8d_stage_detail_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_stage_detail
    ADD CONSTRAINT qms_8d_stage_detail_pkey PRIMARY KEY (id);


--
-- Name: qms_8d_stage_detail qms_8d_stage_detail_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_8d_stage_detail qms_8d_stage_detail_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_8d_approval_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_approval_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id character varying(64) NOT NULL,
    stage_code character varying(16) NOT NULL,
    need_approval boolean DEFAULT false NOT NULL,
    signer character varying(128),
    sort_order integer DEFAULT 0 NOT NULL
);


--
-- Name: qms_8d_approval_config qms_8d_approval_config_org_id_stage_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_approval_config
    ADD CONSTRAINT qms_8d_approval_config_org_id_stage_code_key UNIQUE (org_id, stage_code);


--
-- Name: qms_8d_approval_config qms_8d_approval_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_approval_config
    ADD CONSTRAINT qms_8d_approval_config_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: qms_8d_archived_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_8d_archived_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    archive_no character varying(32) NOT NULL,
    report_id uuid NOT NULL,
    d8_no character varying(32) NOT NULL,
    archive_date date NOT NULL,
    status character varying(16) DEFAULT '已归档'::character varying NOT NULL,
    pdf_ref character varying(255) NOT NULL,
    report_hash character(64) NOT NULL,
    retention_until date NOT NULL
);


--
-- Name: TABLE qms_8d_archived_report; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.qms_8d_archived_report IS '8D 整改报告归档(15 年留存)';


--
-- Name: qms_8d_archived_report qms_8d_archived_report_archive_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_archived_report
    ADD CONSTRAINT qms_8d_archived_report_archive_no_key UNIQUE (archive_no);


--
-- Name: qms_8d_archived_report qms_8d_archived_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_8d_archived_report
    ADD CONSTRAINT qms_8d_archived_report_pkey PRIMARY KEY (id);


--
-- Name: idx_8d_archive_date; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_8d_archive_date ON ops.qms_8d_archived_report USING btree (org_id, archive_date);


--
-- Name: idx_8d_archive_report; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_8d_archive_report ON ops.qms_8d_archived_report USING btree (org_id, report_id);


--
-- Name: qms_8d_archived_report qms_8d_archived_report_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: qms_8d_archived_report qms_8d_archived_report_report_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_adverse_event; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_adverse_event (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    event_no character varying(32) NOT NULL,
    event_type character varying(32) NOT NULL,
    occur_stage character varying(80),
    severity character varying(16) DEFAULT 'GENERAL'::character varying NOT NULL,
    occur_at timestamp without time zone,
    report_at timestamp without time zone,
    root_cause text,
    handle_desc text,
    handle_timeliness character varying(16),
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    remark character varying(400),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    owner character varying(80)
);


--
-- Name: qms_adverse_event qms_adverse_event_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_adverse_event
    ADD CONSTRAINT qms_adverse_event_pkey PRIMARY KEY (id);


--
-- Name: idx_qms_adverse_org_type; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_adverse_org_type ON ops.qms_adverse_event USING btree (org_id, event_type) WHERE (is_deleted = false);


--
-- Name: uk_qms_adverse_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uk_qms_adverse_no ON ops.qms_adverse_event USING btree (org_id, event_no) WHERE (is_deleted = false);


--
-- Name: qms_adverse_event qms_adverse_event_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: qms_assign_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.qms_assign_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    defect_id uuid,
    defect_no character varying(64),
    biz_type character varying(16) NOT NULL,
    biz_id character varying(64),
    biz_no character varying(64),
    assignee_user_id uuid,
    assignee_user_name character varying(64),
    assignee_role_code character varying(64),
    assignee_role_name character varying(64),
    notify_channels character varying(128),
    assigner_id uuid,
    remark text,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    version integer DEFAULT 0 NOT NULL,
    action character varying(20)
);


--
-- Name: COLUMN qms_assign_record.action; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.qms_assign_record.action IS '指派动作: assign=首次指派, reassign=改派';


--
-- Name: qms_assign_record qms_assign_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.qms_assign_record
    ADD CONSTRAINT qms_assign_record_pkey PRIMARY KEY (id);


--
-- Name: idx_qms_assign_record_defect; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_assign_record_defect ON ops.qms_assign_record USING btree (defect_id, is_deleted);


--
-- Name: idx_qms_assign_record_user; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_qms_assign_record_user ON ops.qms_assign_record USING btree (assignee_user_id, is_deleted);


--
-- PostgreSQL database dump complete
--



