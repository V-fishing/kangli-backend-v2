-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [售后 (CS)]
-- ----------------------------------------------------------------------------
-- 板块 : 售后 (CS)
-- 内容 : 2 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: cs_work_order; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.cs_work_order (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    order_no character varying(32) NOT NULL,
    customer_name character varying(120) NOT NULL,
    customer_contact character varying(120),
    wo_type character varying(16) NOT NULL,
    priority character varying(16) DEFAULT 'NORMAL'::character varying NOT NULL,
    product_name character varying(160),
    fault_desc text,
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    owner_id uuid,
    owner_name character varying(80),
    assign_at timestamp without time zone,
    handle_detail text,
    handle_at timestamp without time zone,
    close_at timestamp without time zone,
    satisfaction integer,
    satisfaction_comment character varying(400),
    expect_time timestamp without time zone,
    address character varying(240),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: cs_work_order cs_work_order_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.cs_work_order
    ADD CONSTRAINT cs_work_order_pkey PRIMARY KEY (id);


--
-- Name: idx_cs_work_order_org_created; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_work_order_org_created ON ops.cs_work_order USING btree (org_id, created_at DESC) WHERE (is_deleted = false);


--
-- Name: idx_cs_work_order_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_work_order_org_status ON ops.cs_work_order USING btree (org_id, status) WHERE (is_deleted = false);


--
-- Name: uk_cs_work_order_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uk_cs_work_order_no ON ops.cs_work_order USING btree (org_id, order_no) WHERE (is_deleted = false);


--
-- Name: cs_work_order cs_work_order_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: cs_feedback; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.cs_feedback (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    customer_name character varying(120) NOT NULL,
    customer_contact character varying(120),
    fb_type character varying(16) NOT NULL,
    content text NOT NULL,
    related_wo_no character varying(32),
    status character varying(16) DEFAULT 'OPEN'::character varying NOT NULL,
    handle_detail text,
    handle_at timestamp without time zone,
    owner_name character varying(80),
    satisfaction integer,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    cause character varying(32),
    related_ncm_id uuid,
    related_8d_id uuid,
    related_capa_id uuid,
    related_ca_id uuid
);


--
-- Name: cs_feedback cs_feedback_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.cs_feedback
    ADD CONSTRAINT cs_feedback_pkey PRIMARY KEY (id);


--
-- Name: idx_cs_feedback_8d; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_8d ON ops.cs_feedback USING btree (related_8d_id) WHERE ((is_deleted = false) AND (related_8d_id IS NOT NULL));


--
-- Name: idx_cs_feedback_ca; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_ca ON ops.cs_feedback USING btree (related_ca_id) WHERE ((is_deleted = false) AND (related_ca_id IS NOT NULL));


--
-- Name: idx_cs_feedback_capa; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_capa ON ops.cs_feedback USING btree (related_capa_id) WHERE ((is_deleted = false) AND (related_capa_id IS NOT NULL));


--
-- Name: idx_cs_feedback_cause; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_cause ON ops.cs_feedback USING btree (cause) WHERE ((is_deleted = false) AND (cause IS NOT NULL));


--
-- Name: idx_cs_feedback_ncm; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_ncm ON ops.cs_feedback USING btree (related_ncm_id) WHERE ((is_deleted = false) AND (related_ncm_id IS NOT NULL));


--
-- Name: idx_cs_feedback_org_created; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_org_created ON ops.cs_feedback USING btree (org_id, created_at DESC) WHERE (is_deleted = false);


--
-- Name: idx_cs_feedback_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_cs_feedback_org_status ON ops.cs_feedback USING btree (org_id, status) WHERE (is_deleted = false);


--
-- Name: cs_feedback cs_feedback_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



