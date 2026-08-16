-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [工装/计量 (TLM)]
-- ----------------------------------------------------------------------------
-- 板块 : 工装/计量 (TLM)
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
-- Name: tlm_repair; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_repair (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    repair_no character varying(32) NOT NULL,
    fault_desc character varying(512),
    measure character varying(512),
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    approver_id uuid,
    verify_task_id character varying(64),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    fault_type character varying(32)
);


--
-- Name: TABLE tlm_repair; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_repair IS '工装维修工单';


--
-- Name: COLUMN tlm_repair.fault_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_repair.fault_type IS '故障类型: 磨损/变形/断裂/精度超差/电气故障/其他';


--
-- Name: tlm_repair tlm_repair_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_repair
    ADD CONSTRAINT tlm_repair_pkey PRIMARY KEY (id);


--
-- Name: tlm_repair tlm_repair_repair_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_repair
    ADD CONSTRAINT tlm_repair_repair_no_key UNIQUE (repair_no);


--
-- Name: idx_tlm_repair_org_approver; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_repair_org_approver ON ops.tlm_repair USING btree (org_id, approver_id);


--
-- Name: idx_tlm_repair_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_repair_org_status ON ops.tlm_repair USING btree (org_id, status);


--
-- Name: idx_tlm_repair_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_repair_org_tool ON ops.tlm_repair USING btree (org_id, tool_id);


--
-- Name: tlm_repair tlm_repair_approver_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_repair tlm_repair_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_repair tlm_repair_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_scrap; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_scrap (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    scrap_no character varying(32) NOT NULL,
    scrap_method character varying(16),
    reason character varying(512),
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    approver_id uuid,
    approval_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE tlm_scrap; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_scrap IS '工装报废单(接统一审批中心)';


--
-- Name: tlm_scrap tlm_scrap_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_scrap
    ADD CONSTRAINT tlm_scrap_pkey PRIMARY KEY (id);


--
-- Name: tlm_scrap tlm_scrap_scrap_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_scrap
    ADD CONSTRAINT tlm_scrap_scrap_no_key UNIQUE (scrap_no);


--
-- Name: idx_tlm_scrap_org_approver; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_scrap_org_approver ON ops.tlm_scrap USING btree (org_id, approver_id);


--
-- Name: idx_tlm_scrap_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_scrap_org_status ON ops.tlm_scrap USING btree (org_id, status);


--
-- Name: idx_tlm_scrap_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_scrap_org_tool ON ops.tlm_scrap USING btree (org_id, tool_id);


--
-- Name: tlm_scrap tlm_scrap_approver_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_scrap tlm_scrap_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_scrap tlm_scrap_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_maint_plan; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_maint_plan (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    plan_no character varying(32) NOT NULL,
    cycle_type character varying(16),
    next_date date NOT NULL,
    responsible_id uuid,
    remark character varying(256),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(64)
);


--
-- Name: TABLE tlm_maint_plan; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_maint_plan IS '工装保养计划';


--
-- Name: tlm_maint_plan tlm_maint_plan_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_maint_plan
    ADD CONSTRAINT tlm_maint_plan_pkey PRIMARY KEY (id);


--
-- Name: tlm_maint_plan tlm_maint_plan_plan_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_maint_plan
    ADD CONSTRAINT tlm_maint_plan_plan_no_key UNIQUE (plan_no);


--
-- Name: idx_tlm_maint_plan_org_next; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_maint_plan_org_next ON ops.tlm_maint_plan USING btree (org_id, next_date);


--
-- Name: idx_tlm_maint_plan_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_maint_plan_org_tool ON ops.tlm_maint_plan USING btree (org_id, tool_id);


--
-- Name: tlm_maint_plan tlm_maint_plan_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_maint_plan tlm_maint_plan_responsible_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_maint_plan tlm_maint_plan_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_maint_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_maint_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    plan_id uuid,
    tool_id uuid NOT NULL,
    maint_date date NOT NULL,
    result character varying(256),
    responsible_id uuid,
    attachment character varying(256),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(64),
    version integer DEFAULT 0
);


--
-- Name: TABLE tlm_maint_record; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_maint_record IS '工装保养记录';


--
-- Name: tlm_maint_record tlm_maint_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_maint_record
    ADD CONSTRAINT tlm_maint_record_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_maint_record_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_maint_record_org_tool ON ops.tlm_maint_record USING btree (org_id, tool_id);


--
-- Name: tlm_maint_record tlm_maint_record_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_maint_record tlm_maint_record_plan_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_maint_record tlm_maint_record_responsible_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_maint_record tlm_maint_record_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_tool_product; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_tool_product (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    product_code character varying(64) NOT NULL,
    product_name character varying(128),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(64),
    version integer DEFAULT 0,
    kind character varying(16),
    spec_model character varying(256)
);


--
-- Name: TABLE tlm_tool_product; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_tool_product IS '工装与产品的关联(一工具多产品)';


--
-- Name: COLUMN tlm_tool_product.kind; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tool_product.kind IS '产品类型: MATERIAL/SEMI/FINISHED';


--
-- Name: COLUMN tlm_tool_product.spec_model; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tool_product.spec_model IS '规格型号(取自 MES 源表)';


--
-- Name: tlm_tool_product tlm_tool_product_org_id_tool_id_product_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tool_product
    ADD CONSTRAINT tlm_tool_product_org_id_tool_id_product_code_key UNIQUE (org_id, tool_id, product_code);


--
-- Name: tlm_tool_product tlm_tool_product_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tool_product
    ADD CONSTRAINT tlm_tool_product_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_tool_product_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tool_product_org_tool ON ops.tlm_tool_product USING btree (org_id, tool_id);


--
-- Name: tlm_tool_product tlm_tool_product_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tool_product tlm_tool_product_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_metro_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_metro_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    tool_id uuid NOT NULL,
    tool_no character varying(64),
    tool_name character varying(128),
    wo_no character varying(64),
    batch_no character varying(64),
    measure_point character varying(64),
    measure_value character varying(64),
    measure_unit character varying(16),
    standard_value character varying(64),
    upper_limit character varying(64),
    lower_limit character varying(64),
    judged character varying(16) DEFAULT '合格'::character varying NOT NULL,
    measure_time timestamp without time zone,
    operator character varying(64),
    remark character varying(255),
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    created_by character varying(36),
    updated_by character varying(36),
    version integer DEFAULT 0
);


--
-- Name: tlm_metro_record tlm_metro_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_metro_record
    ADD CONSTRAINT tlm_metro_record_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_metro_record_time; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_metro_record_time ON ops.tlm_metro_record USING btree (measure_time);


--
-- Name: idx_tlm_metro_record_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_metro_record_tool ON ops.tlm_metro_record USING btree (tool_id);


--
-- Name: idx_tlm_metro_record_wo; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_metro_record_wo ON ops.tlm_metro_record USING btree (wo_no);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: tlm_tool_wo_bind; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_tool_wo_bind (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    wo_no character varying(32) NOT NULL,
    bound_at timestamp with time zone DEFAULT now() NOT NULL,
    unbound_at timestamp with time zone,
    created_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(64),
    version integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT now(),
    calib_no character varying(64),
    calib_date date,
    calib_due_date date
);


--
-- Name: TABLE tlm_tool_wo_bind; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_tool_wo_bind IS '工装-工单绑定(寿命计数触发)';


--
-- Name: tlm_tool_wo_bind tlm_tool_wo_bind_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tool_wo_bind
    ADD CONSTRAINT tlm_tool_wo_bind_pkey PRIMARY KEY (id);


--
-- Name: tlm_tool_wo_bind tlm_tool_wo_bind_tool_id_wo_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tool_wo_bind
    ADD CONSTRAINT tlm_tool_wo_bind_tool_id_wo_no_key UNIQUE (tool_id, wo_no);


--
-- Name: idx_tlm_tool_wo_bind_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tool_wo_bind_org_tool ON ops.tlm_tool_wo_bind USING btree (org_id, tool_id);


--
-- Name: tlm_tool_wo_bind tlm_tool_wo_bind_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tool_wo_bind tlm_tool_wo_bind_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_tool_version; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_tool_version (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    version_no character varying(16) NOT NULL,
    change_type character varying(16) DEFAULT 'OTHER'::character varying NOT NULL,
    change_desc character varying(512),
    changed_by character varying(64),
    changed_at date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE tlm_tool_version; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_tool_version IS '工装版本变更履历: 设计变更/升级记录, 仅留痕可追溯';


--
-- Name: tlm_tool_version tlm_tool_version_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tool_version
    ADD CONSTRAINT tlm_tool_version_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_tool_version_org_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tool_version_org_tool ON ops.tlm_tool_version USING btree (org_id, tool_id);


--
-- Name: tlm_tool_version tlm_tool_version_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tool_version tlm_tool_version_tool_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_tooling; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_tooling (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    tool_no character varying(32) NOT NULL,
    tool_name character varying(128) NOT NULL,
    tool_category character varying(8) DEFAULT 'TOOL'::character varying NOT NULL,
    tool_type character varying(32),
    risk_class character varying(8),
    process_id uuid,
    product_code character varying(64),
    spec character varying(256),
    material character varying(64),
    supplier_id uuid,
    status character varying(16) DEFAULT 'IN_USE'::character varying NOT NULL,
    location character varying(128),
    owner_id uuid,
    admin_id uuid,
    software_ver character varying(32),
    precision_val character varying(32),
    measure_point character varying(64),
    calib_date date,
    calib_due_date date,
    calib_cycle integer,
    bind_count integer DEFAULT 0 NOT NULL,
    design_life integer,
    next_maint_date date,
    maint_cycle integer,
    cost numeric(12,2),
    inbound_date date,
    locked boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    purchase_date date,
    proc_name character varying(128),
    quantity integer DEFAULT 1,
    verify_cycle character varying(32),
    remark character varying(512),
    owner_name character varying(64),
    admin_name character varying(64),
    supplier_name character varying(128)
);


--
-- Name: TABLE tlm_tooling; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.tlm_tooling IS '工装管理主表: 工装夹具与监视测量设备合一台账';


--
-- Name: COLUMN tlm_tooling.purchase_date; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.purchase_date IS '采购日期(资产入账时间)';


--
-- Name: COLUMN tlm_tooling.proc_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.proc_name IS '工序名称(与 process_id 同源，用于 FIA/SPC 标准匹配)';


--
-- Name: COLUMN tlm_tooling.quantity; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.quantity IS '数量(工装夹具台账/设备总表)';


--
-- Name: COLUMN tlm_tooling.verify_cycle; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.verify_cycle IS '验证周期(工装夹具台账,如 一年)';


--
-- Name: COLUMN tlm_tooling.remark; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.remark IS '备注(工装夹具台账/设备总表)';


--
-- Name: COLUMN tlm_tooling.owner_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.owner_name IS '领用人(设备总表,纯文本)';


--
-- Name: COLUMN tlm_tooling.admin_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.admin_name IS '设备管理员(设备总表,纯文本)';


--
-- Name: COLUMN tlm_tooling.supplier_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.tlm_tooling.supplier_name IS '供应商/生产厂家(设备总表,纯文本)';


--
-- Name: tlm_tooling tlm_tooling_org_id_tool_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_org_id_tool_no_key UNIQUE (org_id, tool_no);


--
-- Name: tlm_tooling tlm_tooling_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_tooling
    ADD CONSTRAINT tlm_tooling_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_tooling_org_calib; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tooling_org_calib ON ops.tlm_tooling USING btree (org_id, calib_due_date);


--
-- Name: idx_tlm_tooling_org_cat; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tooling_org_cat ON ops.tlm_tooling USING btree (org_id, tool_category);


--
-- Name: idx_tlm_tooling_org_maint; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tooling_org_maint ON ops.tlm_tooling USING btree (org_id, next_maint_date);


--
-- Name: idx_tlm_tooling_org_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tooling_org_status ON ops.tlm_tooling USING btree (org_id, status);


--
-- Name: idx_tlm_tooling_org_tool_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_tooling_org_tool_no ON ops.tlm_tooling USING btree (org_id, tool_no);


--
-- Name: tlm_tooling tlm_tooling_admin_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tooling tlm_tooling_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tooling tlm_tooling_owner_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tooling tlm_tooling_process_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: tlm_tooling tlm_tooling_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_scrap_archive; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_scrap_archive (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    archive_no character varying(32) NOT NULL,
    scrap_id uuid NOT NULL,
    tool_id uuid NOT NULL,
    scrap_no character varying(32),
    tool_no character varying(32),
    tool_name character varying(128),
    scrap_method character varying(16),
    reason character varying(512),
    archive_date timestamp with time zone DEFAULT now() NOT NULL,
    retention_until date,
    report_hash character varying(128),
    pdf_ref character varying(256),
    status character varying(16) DEFAULT '已归档'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    last_calib_date date,
    calib_due_date date,
    tool_category character varying(16)
);


--
-- Name: tlm_scrap_archive tlm_scrap_archive_archive_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_scrap_archive
    ADD CONSTRAINT tlm_scrap_archive_archive_no_key UNIQUE (archive_no);


--
-- Name: tlm_scrap_archive tlm_scrap_archive_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_scrap_archive
    ADD CONSTRAINT tlm_scrap_archive_pkey PRIMARY KEY (id);


--
-- Name: tlm_scrap_archive uq_tlm_scrap_archive_scrap_id; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_scrap_archive
    ADD CONSTRAINT uq_tlm_scrap_archive_scrap_id UNIQUE (scrap_id);


--
-- Name: idx_tlm_scrap_archive_org; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_scrap_archive_org ON ops.tlm_scrap_archive USING btree (org_id);


--
-- Name: tlm_scrap_archive tlm_scrap_archive_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: tlm_calib_plan; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.tlm_calib_plan (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    tool_id uuid NOT NULL,
    tool_no character varying(64),
    tool_name character varying(128),
    plan_cycle integer,
    plan_due_date date,
    status character varying(16) DEFAULT 'PENDING'::character varying NOT NULL,
    owner_id character varying(36),
    source character varying(16) DEFAULT 'AUTO'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    is_deleted boolean DEFAULT false NOT NULL,
    created_by character varying(36),
    updated_by character varying(36),
    version integer DEFAULT 0 NOT NULL,
    calib_no character varying(32),
    calib_date date,
    calib_due_date date,
    calib_cycle integer,
    upper_limit character varying(32),
    result character varying(16),
    remark character varying(512),
    cert_no character varying(64)
);


--
-- Name: tlm_calib_plan tlm_calib_plan_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.tlm_calib_plan
    ADD CONSTRAINT tlm_calib_plan_pkey PRIMARY KEY (id);


--
-- Name: idx_tlm_calib_plan_due; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_calib_plan_due ON ops.tlm_calib_plan USING btree (plan_due_date);


--
-- Name: idx_tlm_calib_plan_stat; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_calib_plan_stat ON ops.tlm_calib_plan USING btree (status);


--
-- Name: idx_tlm_calib_plan_tool; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_tlm_calib_plan_tool ON ops.tlm_calib_plan USING btree (tool_id);


--
-- PostgreSQL database dump complete
--



