-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 异常与整改 (SQM-ABNORMAL)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 异常与整改 (SQM-ABNORMAL)
-- 内容 : 10 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: sqm_rectify_notice; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_rectify_notice (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    notice_no character varying(32) NOT NULL,
    biz_source character varying(16) NOT NULL,
    abnormal_id uuid,
    supplier_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    content text NOT NULL,
    deadline date NOT NULL,
    notice_date date NOT NULL,
    channels character varying(32) NOT NULL,
    sent_flag boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_rectify_notice sqm_rectify_notice_notice_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_rectify_notice
    ADD CONSTRAINT sqm_rectify_notice_notice_no_key UNIQUE (notice_no);


--
-- Name: sqm_rectify_notice sqm_rectify_notice_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_rectify_notice
    ADD CONSTRAINT sqm_rectify_notice_pkey PRIMARY KEY (id);


--
-- Name: sqm_rectify_notice sqm_rectify_notice_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_rectify_notice sqm_rectify_notice_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_rectify_notice sqm_rectify_notice_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_sqe_verification; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_sqe_verification (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    verification_id character varying(32) NOT NULL,
    abnormal_id uuid NOT NULL,
    sqe_id uuid NOT NULL,
    verify_result character varying(8) NOT NULL,
    verify_comment text,
    verify_date date NOT NULL,
    qualified_batch_count integer,
    esign_id uuid
);


--
-- Name: sqm_sqe_verification sqm_sqe_verification_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_sqe_verification
    ADD CONSTRAINT sqm_sqe_verification_pkey PRIMARY KEY (id);


--
-- Name: sqm_sqe_verification sqm_sqe_verification_verification_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_sqe_verification
    ADD CONSTRAINT sqm_sqe_verification_verification_id_key UNIQUE (verification_id);


--
-- Name: sqm_sqe_verification sqm_sqe_verification_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_sqe_verification sqm_sqe_verification_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_rectify_batch_verify; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_rectify_batch_verify (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    abnormal_id uuid NOT NULL,
    batch_no character varying(32) NOT NULL,
    inspect_result character varying(8) NOT NULL,
    inspect_date date,
    seq integer NOT NULL,
    total_seq integer DEFAULT 3 NOT NULL,
    restored_flag boolean DEFAULT false NOT NULL
);


--
-- Name: sqm_rectify_batch_verify sqm_rectify_batch_verify_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_rectify_batch_verify
    ADD CONSTRAINT sqm_rectify_batch_verify_pkey PRIMARY KEY (id);


--
-- Name: sqm_rectify_batch_verify sqm_rectify_batch_verify_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_rectify_batch_verify sqm_rectify_batch_verify_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_abnormal_trigger_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_abnormal_trigger_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    level character varying(8) NOT NULL,
    threshold integer NOT NULL,
    auto_flag boolean DEFAULT true NOT NULL
);


--
-- Name: sqm_abnormal_trigger_rule sqm_abnormal_trigger_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_abnormal_trigger_rule
    ADD CONSTRAINT sqm_abnormal_trigger_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_abnormal_trigger_rule sqm_abnormal_trigger_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_rectify_sla_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_rectify_sla_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    stage character varying(16) NOT NULL,
    days integer NOT NULL,
    notify_role character varying(32) NOT NULL
);


--
-- Name: sqm_rectify_sla_rule sqm_rectify_sla_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_rectify_sla_rule
    ADD CONSTRAINT sqm_rectify_sla_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_rectify_sla_rule sqm_rectify_sla_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_repeat_problem_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_repeat_problem_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    window_days integer DEFAULT 30 NOT NULL,
    threshold integer DEFAULT 2 NOT NULL,
    action character varying(32) NOT NULL
);


--
-- Name: sqm_repeat_problem_rule sqm_repeat_problem_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_repeat_problem_rule
    ADD CONSTRAINT sqm_repeat_problem_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_repeat_problem_rule sqm_repeat_problem_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_escalation_action_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_escalation_action_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    repeat_count integer NOT NULL,
    action character varying(32) NOT NULL
);


--
-- Name: sqm_escalation_action_rule sqm_escalation_action_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_escalation_action_rule
    ADD CONSTRAINT sqm_escalation_action_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_escalation_action_rule sqm_escalation_action_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_abnormal_measure; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_abnormal_measure (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    abnormal_id uuid NOT NULL,
    seq integer DEFAULT 0 NOT NULL,
    content text NOT NULL,
    operator character varying(64),
    complete_date date,
    status character varying(16) DEFAULT '待完成'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_abnormal_measure; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_abnormal_measure IS '来料异常整改措施记录';


--
-- Name: sqm_abnormal_measure sqm_abnormal_measure_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_abnormal_measure
    ADD CONSTRAINT sqm_abnormal_measure_pkey PRIMARY KEY (id);


--
-- Name: idx_abm_abnormal; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_abm_abnormal ON ops.sqm_abnormal_measure USING btree (org_id, abnormal_id);


--
-- Name: sqm_abnormal_measure sqm_abnormal_measure_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_abnormal_measure sqm_abnormal_measure_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_abnormal_batch_verify; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_abnormal_batch_verify (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    abnormal_id uuid NOT NULL,
    batch_no character varying(32) NOT NULL,
    result character varying(16) DEFAULT '待验证'::character varying NOT NULL,
    verify_date date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_abnormal_batch_verify; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_abnormal_batch_verify IS '来料异常三批验证记录';


--
-- Name: sqm_abnormal_batch_verify sqm_abnormal_batch_verify_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_abnormal_batch_verify
    ADD CONSTRAINT sqm_abnormal_batch_verify_pkey PRIMARY KEY (id);


--
-- Name: idx_abv_abnormal; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_abv_abnormal ON ops.sqm_abnormal_batch_verify USING btree (org_id, abnormal_id);


--
-- Name: sqm_abnormal_batch_verify sqm_abnormal_batch_verify_abnormal_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_abnormal_batch_verify sqm_abnormal_batch_verify_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_abnormal_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_abnormal_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    severe_min_qty integer DEFAULT 3 NOT NULL,
    general_accum_days integer DEFAULT 30 NOT NULL,
    general_accum_qty integer DEFAULT 3 NOT NULL,
    remark character varying(255),
    updated_at timestamp with time zone DEFAULT now()
);


--
-- Name: TABLE sqm_abnormal_rule; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_abnormal_rule IS '来料异常严重度判定规则(org_id IS NULL 为全局规则)';


--
-- Name: COLUMN sqm_abnormal_rule.severe_min_qty; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_abnormal_rule.severe_min_qty IS '严重判定阈值:不良数>=该值判严重';


--
-- Name: COLUMN sqm_abnormal_rule.general_accum_days; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_abnormal_rule.general_accum_days IS '一般不良累计窗口天数:窗口内累计件数达到阈值触发8D';


--
-- Name: COLUMN sqm_abnormal_rule.general_accum_qty; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_abnormal_rule.general_accum_qty IS '一般不良累计件数阈值:窗口内累计>=该值触发8D';


--
-- Name: sqm_abnormal_rule sqm_abnormal_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_abnormal_rule
    ADD CONSTRAINT sqm_abnormal_rule_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--



