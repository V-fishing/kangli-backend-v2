-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 物料追溯 (SQM-TRACE)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 物料追溯 (SQM-TRACE)
-- 内容 : 6 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: sqm_trace_node; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_node (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    root_lot_id uuid,
    parent_node_id uuid,
    node_type character varying(16) NOT NULL,
    node_name character varying(255) NOT NULL,
    batch_no character varying(64),
    qty numeric(14,2),
    unit character varying(16),
    node_date date,
    supplier_id uuid,
    remark text,
    tree_level integer DEFAULT 0 NOT NULL,
    is_valid character varying(8) DEFAULT '是'::character varying NOT NULL,
    invalid_by character varying(64),
    invalid_time timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    root_node_id uuid,
    qualification_type character varying(16),
    material_code character varying(64),
    production_order_no character varying(32),
    stage character varying(16)
);


--
-- Name: TABLE sqm_trace_node; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_trace_node IS '追溯节点(自引用树,WITH RECURSIVE 正/逆向)';


--
-- Name: COLUMN sqm_trace_node.material_code; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_node.material_code IS '物料号(与 batch_no 组成物料条码); 按物料号反查构成去向/召回扩展时使用';


--
-- Name: COLUMN sqm_trace_node.production_order_no; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_node.production_order_no IS '生产工单(MES 工单枢纽 TASK_NO/WORK_ORDER)';


--
-- Name: COLUMN sqm_trace_node.stage; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_node.stage IS 'MES 检验阶段 IQC/IPQC/SQC/FQC/OQC/RQC/PKG';


--
-- Name: sqm_trace_node sqm_trace_node_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_node
    ADD CONSTRAINT sqm_trace_node_pkey PRIMARY KEY (id);


--
-- Name: idx_node_batch_no; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_batch_no ON ops.sqm_trace_node USING btree (batch_no);


--
-- Name: idx_node_mat_code; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_mat_code ON ops.sqm_trace_node USING btree (material_code);


--
-- Name: idx_node_name; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_name ON ops.sqm_trace_node USING btree (node_name);


--
-- Name: idx_node_trgm_batch; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_trgm_batch ON ops.sqm_trace_node USING gin (batch_no ops.gin_trgm_ops);


--
-- Name: idx_node_trgm_mat; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_trgm_mat ON ops.sqm_trace_node USING gin (material_code ops.gin_trgm_ops);


--
-- Name: idx_node_trgm_name; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_node_trgm_name ON ops.sqm_trace_node USING gin (node_name ops.gin_trgm_ops);


--
-- Name: idx_sqm_trace_root_node; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_trace_root_node ON ops.sqm_trace_node USING btree (org_id, root_node_id);


--
-- Name: idx_trace_material; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trace_material ON ops.sqm_trace_node USING btree (org_id, material_code);


--
-- Name: idx_trace_parent; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trace_parent ON ops.sqm_trace_node USING btree (org_id, parent_node_id);


--
-- Name: idx_trace_root; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trace_root ON ops.sqm_trace_node USING btree (org_id, root_lot_id);


--
-- Name: sqm_trace_node sqm_trace_node_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_node sqm_trace_node_parent_node_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_node sqm_trace_node_root_node_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_node sqm_trace_node_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_trace_raw_detail; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_raw_detail (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    node_id uuid NOT NULL,
    category character varying(32),
    wo_no character varying(32),
    product_barcode character varying(64),
    product_part_no character varying(64),
    product_name character varying(128),
    wo_qty numeric(14,2),
    material_barcode character varying(64),
    material_code character varying(64),
    material_name character varying(128),
    spec_model character varying(128),
    scanner character varying(64),
    scan_time timestamp with time zone,
    process_code character varying(32),
    process_name character varying(64),
    batch_no character varying(64)
);


--
-- Name: COLUMN sqm_trace_raw_detail.batch_no; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_raw_detail.batch_no IS '组件批号(SON_LOT_NO)';


--
-- Name: sqm_trace_raw_detail sqm_trace_raw_detail_node_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_raw_detail
    ADD CONSTRAINT sqm_trace_raw_detail_node_id_key UNIQUE (node_id);


--
-- Name: sqm_trace_raw_detail sqm_trace_raw_detail_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_raw_detail
    ADD CONSTRAINT sqm_trace_raw_detail_pkey PRIMARY KEY (id);


--
-- Name: sqm_trace_raw_detail sqm_trace_raw_detail_node_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_raw_detail sqm_trace_raw_detail_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_trace_link; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_link (
    id bigint NOT NULL,
    org_id uuid,
    parent_node_id uuid NOT NULL,
    child_node_id uuid NOT NULL,
    link_type character varying(32),
    sort_order integer DEFAULT 0,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    created_by uuid
);


--
-- Name: sqm_trace_link_id_seq; Type: SEQUENCE; Schema: ops; Owner: -
--

CREATE SEQUENCE ops.sqm_trace_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sqm_trace_link_id_seq; Type: SEQUENCE OWNED BY; Schema: ops; Owner: -
--

ALTER SEQUENCE ops.sqm_trace_link_id_seq OWNED BY ops.sqm_trace_link.id;


--
-- Name: sqm_trace_link id; Type: DEFAULT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_link ALTER COLUMN id SET DEFAULT nextval('ops.sqm_trace_link_id_seq'::regclass);


--
-- Name: sqm_trace_link sqm_trace_link_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_link
    ADD CONSTRAINT sqm_trace_link_pkey PRIMARY KEY (id);


--
-- Name: sqm_trace_link uk_trace_link; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_link
    ADD CONSTRAINT uk_trace_link UNIQUE (parent_node_id, child_node_id);


--
-- Name: idx_trace_link_child; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trace_link_child ON ops.sqm_trace_link USING btree (child_node_id);


--
-- Name: idx_trace_link_parent; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_trace_link_parent ON ops.sqm_trace_link USING btree (parent_node_id);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: sqm_trace_relation; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_relation (
    id bigint NOT NULL,
    org_id uuid NOT NULL,
    parent_barcode text NOT NULL,
    child_barcode text NOT NULL,
    relation_type character varying(32) NOT NULL,
    is_deleted character(1) DEFAULT '0'::bpchar NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE sqm_trace_relation; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_trace_relation IS '统一追溯关系表: 仅存条码之间的边, 节点详情取自三张源表';


--
-- Name: COLUMN sqm_trace_relation.parent_barcode; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_relation.parent_barcode IS '父节点业务条码(来料/关键件/半成品条码)';


--
-- Name: COLUMN sqm_trace_relation.child_barcode; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_relation.child_barcode IS '子节点业务条码(关键件/成品/半成品条码)';


--
-- Name: COLUMN sqm_trace_relation.relation_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_relation.relation_type IS 'incoming->keypart / keypart->finished / keypart->semi / semi->finished';


--
-- Name: sqm_trace_relation_id_seq; Type: SEQUENCE; Schema: ops; Owner: -
--

CREATE SEQUENCE ops.sqm_trace_relation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sqm_trace_relation_id_seq; Type: SEQUENCE OWNED BY; Schema: ops; Owner: -
--

ALTER SEQUENCE ops.sqm_trace_relation_id_seq OWNED BY ops.sqm_trace_relation.id;


--
-- Name: sqm_trace_relation id; Type: DEFAULT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_relation ALTER COLUMN id SET DEFAULT nextval('ops.sqm_trace_relation_id_seq'::regclass);


--
-- Name: sqm_trace_relation sqm_trace_relation_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_relation
    ADD CONSTRAINT sqm_trace_relation_pkey PRIMARY KEY (id);


--
-- Name: sqm_trace_relation uq_sqm_trace_relation_edge; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_relation
    ADD CONSTRAINT uq_sqm_trace_relation_edge UNIQUE (org_id, parent_barcode, child_barcode, relation_type);


--
-- Name: idx_sqm_trace_relation_child; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_trace_relation_child ON ops.sqm_trace_relation USING btree (org_id, child_barcode) WHERE (is_deleted = '0'::bpchar);


--
-- Name: idx_sqm_trace_relation_parent; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_trace_relation_parent ON ops.sqm_trace_relation USING btree (org_id, parent_barcode) WHERE (is_deleted = '0'::bpchar);


--
-- Name: idx_sqm_trace_relation_type; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_trace_relation_type ON ops.sqm_trace_relation USING btree (org_id, relation_type) WHERE (is_deleted = '0'::bpchar);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: sqm_trace_product_detail; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_product_detail (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    node_id uuid NOT NULL,
    is_urgent character varying(8),
    qc_review character varying(16),
    mg_approve character varying(16),
    inspect_result character varying(16),
    report_no character varying(32),
    inspect_order_no character varying(32),
    production_order_no character varying(32),
    material_code character varying(64),
    product_name character varying(128),
    model_spec character varying(128),
    batch_no character varying(64),
    production_date date,
    expiry_date date,
    inspect_qty numeric(14,2),
    inspect_count numeric(14,2),
    pass_qty numeric(14,2),
    fail_qty numeric(14,2),
    unit character varying(16),
    inspector character varying(64),
    category character varying(32),
    qc_reviewer character varying(64),
    qc_review_time timestamp with time zone,
    mg_approver character varying(64),
    mg_approve_time timestamp with time zone,
    drug_reg_no character varying(64),
    perf_inspect_method character varying(64),
    perf_batch_no character varying(64),
    customer character varying(128),
    customer_code character varying(64),
    customer_order_no character varying(64),
    ship_date date,
    tracking_no character varying(64),
    ship_address character varying(255),
    product_barcode character varying(64)
);


--
-- Name: COLUMN sqm_trace_product_detail.product_barcode; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_trace_product_detail.product_barcode IS '产品条码=产品批号';


--
-- Name: sqm_trace_product_detail sqm_trace_product_detail_node_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_product_detail
    ADD CONSTRAINT sqm_trace_product_detail_node_id_key UNIQUE (node_id);


--
-- Name: sqm_trace_product_detail sqm_trace_product_detail_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_product_detail
    ADD CONSTRAINT sqm_trace_product_detail_pkey PRIMARY KEY (id);


--
-- Name: sqm_trace_product_detail sqm_trace_product_detail_node_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_product_detail sqm_trace_product_detail_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_trace_customer_detail; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_trace_customer_detail (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    node_id uuid NOT NULL,
    customer_name character varying(128) NOT NULL,
    customer_code character varying(64),
    customer_order_no character varying(64),
    ship_date date,
    tracking_no character varying(64),
    ship_address character varying(255),
    contact_person character varying(64),
    contact_phone character varying(32),
    qty numeric(14,2),
    unit character varying(16)
);


--
-- Name: sqm_trace_customer_detail sqm_trace_customer_detail_node_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_customer_detail
    ADD CONSTRAINT sqm_trace_customer_detail_node_id_key UNIQUE (node_id);


--
-- Name: sqm_trace_customer_detail sqm_trace_customer_detail_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_trace_customer_detail
    ADD CONSTRAINT sqm_trace_customer_detail_pkey PRIMARY KEY (id);


--
-- Name: sqm_trace_customer_detail sqm_trace_customer_detail_node_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_trace_customer_detail sqm_trace_customer_detail_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



