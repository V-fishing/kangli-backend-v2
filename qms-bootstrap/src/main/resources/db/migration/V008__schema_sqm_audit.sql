-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 审核 (SQM-AUDIT)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 审核 (SQM-AUDIT)
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
-- Name: sqm_audit_workflow_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_workflow_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    plan_id uuid NOT NULL,
    node character varying(32) NOT NULL,
    action character varying(64),
    operator character varying(64),
    remark text,
    create_time timestamp with time zone DEFAULT now() NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_audit_workflow_log; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_workflow_log IS '供应商审核流程轨迹(全节点留痕,供时间轴审计)';


--
-- Name: sqm_audit_workflow_log sqm_audit_workflow_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_workflow_log
    ADD CONSTRAINT sqm_audit_workflow_log_pkey PRIMARY KEY (id);


--
-- Name: idx_sqm_audit_wf_plan; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sqm_audit_wf_plan ON ops.sqm_audit_workflow_log USING btree (plan_id, create_time);


--
-- Name: sqm_audit_workflow_log sqm_audit_workflow_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_workflow_log sqm_audit_workflow_log_plan_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_plan; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_plan (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    plan_no character varying(32) NOT NULL,
    supplier_id uuid NOT NULL,
    audit_type character varying(16) NOT NULL,
    plan_date date NOT NULL,
    audit_lead character varying(64),
    auditor_team text,
    scope character varying(255),
    risk_level character varying(8),
    actual_date date,
    status character varying(16) DEFAULT '计划中'::character varying NOT NULL,
    record_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    ext_json jsonb,
    actual_auditors text,
    change_id character varying(64),
    audit_lead_user_id character varying(36)
);


--
-- Name: TABLE sqm_audit_plan; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_plan IS '供应商审核计划';


--
-- Name: COLUMN sqm_audit_plan.ext_json; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_audit_plan.ext_json IS '审核类型特有字段(JSON 字符串),按 audit_type 差异化';


--
-- Name: COLUMN sqm_audit_plan.change_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_audit_plan.change_id IS '来源变更单 id(物料变更审核由变更单提交联动生成,用于双向追溯)';


--
-- Name: COLUMN sqm_audit_plan.audit_lead_user_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_audit_plan.audit_lead_user_id IS '审核组长用户 ID(对应 ops.sys_user.id)';


--
-- Name: sqm_audit_plan sqm_audit_plan_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_plan
    ADD CONSTRAINT sqm_audit_plan_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_plan sqm_audit_plan_plan_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_plan
    ADD CONSTRAINT sqm_audit_plan_plan_no_key UNIQUE (plan_no);


--
-- Name: sqm_audit_plan sqm_audit_plan_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_plan sqm_audit_plan_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    record_no character varying(32) NOT NULL,
    plan_id uuid,
    supplier_id uuid NOT NULL,
    audit_type character varying(16) NOT NULL,
    audit_date date NOT NULL,
    audit_lead character varying(64),
    auditor_team text,
    result character varying(16) NOT NULL,
    score numeric(5,2),
    nc_count integer DEFAULT 0 NOT NULL,
    conclusion text,
    status character varying(16) DEFAULT '执行中'::character varying NOT NULL,
    archive_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    ext_json jsonb
);


--
-- Name: COLUMN sqm_audit_record.ext_json; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_audit_record.ext_json IS '审核记录类型特有字段(JSON 字符串)';


--
-- Name: sqm_audit_record sqm_audit_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_record
    ADD CONSTRAINT sqm_audit_record_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_record sqm_audit_record_record_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_record
    ADD CONSTRAINT sqm_audit_record_record_no_key UNIQUE (record_no);


--
-- Name: sqm_audit_record sqm_audit_record_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_record sqm_audit_record_plan_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_record sqm_audit_record_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_checklist_item; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_checklist_item (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    record_id uuid NOT NULL,
    seq integer NOT NULL,
    clause character varying(32),
    item_name character varying(128) NOT NULL,
    result character varying(16) NOT NULL,
    evidence text,
    nc_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_audit_checklist_item sqm_audit_checklist_item_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_checklist_item
    ADD CONSTRAINT sqm_audit_checklist_item_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_checklist_item sqm_audit_checklist_item_record_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_checklist_item
    ADD CONSTRAINT sqm_audit_checklist_item_record_id_seq_key UNIQUE (record_id, seq);


--
-- Name: sqm_audit_checklist_item sqm_audit_checklist_item_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_checklist_item sqm_audit_checklist_item_record_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_nc; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_nc (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    nc_no character varying(32) NOT NULL,
    record_id uuid NOT NULL,
    supplier_id uuid NOT NULL,
    clause character varying(32),
    description text NOT NULL,
    level character varying(16) NOT NULL,
    status character varying(16) DEFAULT '整改中'::character varying NOT NULL,
    responsible character varying(64),
    deadline date,
    rectify_measure text,
    rectify_attachment jsonb,
    rectify_date timestamp with time zone,
    need_site_review boolean DEFAULT false NOT NULL,
    verify_result character varying(16),
    verify_comment text,
    verify_date timestamp with time zone,
    verify_by character varying(64),
    verified_batches integer,
    close_date timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_audit_nc; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_nc IS '审核不符合项(分级整改,严重项现场复核+连续3批闭环)';


--
-- Name: sqm_audit_nc sqm_audit_nc_nc_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_nc
    ADD CONSTRAINT sqm_audit_nc_nc_no_key UNIQUE (nc_no);


--
-- Name: sqm_audit_nc sqm_audit_nc_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_nc
    ADD CONSTRAINT sqm_audit_nc_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_nc sqm_audit_nc_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_nc sqm_audit_nc_record_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_nc sqm_audit_nc_supplier_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_photo; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_photo (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    record_id uuid NOT NULL,
    checklist_item_id uuid,
    file_path character varying(512) NOT NULL,
    file_name character varying(255),
    file_hash character(64),
    watermark_time timestamp with time zone NOT NULL,
    watermark_location character varying(255) NOT NULL,
    shoot_by character varying(64),
    shoot_time timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_audit_photo; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_photo IS '审核现场照片(时间+GPS 水印)';


--
-- Name: sqm_audit_photo sqm_audit_photo_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_photo
    ADD CONSTRAINT sqm_audit_photo_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_photo sqm_audit_photo_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_photo sqm_audit_photo_record_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_report_archive; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_report_archive (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    archive_no character varying(32) NOT NULL,
    record_id uuid NOT NULL,
    plan_id uuid,
    supplier_id uuid NOT NULL,
    report_file_path character varying(512) NOT NULL,
    report_hash character(64) NOT NULL,
    assembled_at timestamp with time zone NOT NULL,
    archive_date timestamp with time zone NOT NULL,
    retention_until date NOT NULL
);


--
-- Name: TABLE sqm_audit_report_archive; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_report_archive IS '审核报告归档(15年,受控文件水印)';


--
-- Name: sqm_audit_report_archive sqm_audit_report_archive_archive_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_report_archive
    ADD CONSTRAINT sqm_audit_report_archive_archive_no_key UNIQUE (archive_no);


--
-- Name: sqm_audit_report_archive sqm_audit_report_archive_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_report_archive
    ADD CONSTRAINT sqm_audit_report_archive_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_report_archive sqm_audit_report_archive_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_report_archive sqm_audit_report_archive_record_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_freq_rule; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_freq_rule (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    risk_level character varying(8) NOT NULL,
    level character(1),
    freq_per_year smallint NOT NULL,
    audit_type character varying(16) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sqm_audit_freq_rule sqm_audit_freq_rule_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_freq_rule
    ADD CONSTRAINT sqm_audit_freq_rule_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_freq_rule sqm_audit_freq_rule_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_approval; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_approval (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    audit_id uuid NOT NULL,
    approval_role character varying(16) NOT NULL,
    role_label character varying(32) NOT NULL,
    status character varying(16) DEFAULT 'pending'::character varying NOT NULL,
    operator character varying(64),
    operate_date timestamp with time zone,
    opinion text,
    has_veto boolean DEFAULT false NOT NULL,
    seq_order integer NOT NULL,
    approver_id text
);


--
-- Name: TABLE sqm_audit_approval; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_approval IS '审核会签(质量/采购/研发并行 + 质量一票否决)';


--
-- Name: COLUMN sqm_audit_approval.approver_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sqm_audit_approval.approver_id IS '该会签节点指定审批人(ops.sys_user.id,逗号分隔可多选);空表示不绑定特定人';


--
-- Name: sqm_audit_approval sqm_audit_approval_audit_id_approval_role_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_approval
    ADD CONSTRAINT sqm_audit_approval_audit_id_approval_role_key UNIQUE (audit_id, approval_role);


--
-- Name: sqm_audit_approval sqm_audit_approval_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_approval
    ADD CONSTRAINT sqm_audit_approval_pkey PRIMARY KEY (id);


--
-- Name: sqm_audit_approval sqm_audit_approval_audit_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sqm_audit_approval sqm_audit_approval_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sqm_audit_approval_cfg; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_audit_approval_cfg (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    audit_type character varying(64) NOT NULL,
    auditors text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_audit_approval_cfg; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_audit_approval_cfg IS '审核会签配置:按审核类型配置会签人员与否决权';


--
-- Name: sqm_audit_approval_cfg sqm_audit_approval_cfg_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_audit_approval_cfg
    ADD CONSTRAINT sqm_audit_approval_cfg_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--



