-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [巡检 (PATROL)]
-- ----------------------------------------------------------------------------
-- 板块 : 巡检 (PATROL)
-- 内容 : 7 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: patl_record; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_record (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    task_id uuid NOT NULL,
    checkpoint_id uuid NOT NULL,
    checkpoint_name character varying(128) NOT NULL,
    result character varying(8) DEFAULT '正常'::character varying NOT NULL,
    check_time timestamp with time zone DEFAULT now() NOT NULL,
    operator_id uuid,
    photo_ref character varying(512),
    remark text,
    item_results jsonb
);


--
-- Name: TABLE patl_record; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_record IS '巡检记录(点位级,含检查项明细 JSONB)';


--
-- Name: patl_record patl_record_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_record
    ADD CONSTRAINT patl_record_pkey PRIMARY KEY (id);


--
-- Name: patl_record patl_record_checkpoint_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_record patl_record_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_record patl_record_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_route; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_route (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    route_code character varying(32) NOT NULL,
    route_name character varying(128) NOT NULL,
    proc_name character varying(32),
    freq character varying(32),
    status character varying(8) DEFAULT '启用'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE patl_route; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_route IS '巡检路线(配置:频次+关联工序)';


--
-- Name: patl_route patl_route_org_id_route_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_route
    ADD CONSTRAINT patl_route_org_id_route_code_key UNIQUE (org_id, route_code);


--
-- Name: patl_route patl_route_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_route
    ADD CONSTRAINT patl_route_pkey PRIMARY KEY (id);


--
-- Name: patl_route patl_route_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_checkpoint; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_checkpoint (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    route_id uuid NOT NULL,
    seq smallint NOT NULL,
    point_name character varying(128) NOT NULL,
    location character varying(255),
    need_photo boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE patl_checkpoint; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_checkpoint IS '巡检点位(路线下有序点位)';


--
-- Name: patl_checkpoint patl_checkpoint_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_checkpoint
    ADD CONSTRAINT patl_checkpoint_pkey PRIMARY KEY (id);


--
-- Name: patl_checkpoint patl_checkpoint_route_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_checkpoint
    ADD CONSTRAINT patl_checkpoint_route_id_seq_key UNIQUE (route_id, seq);


--
-- Name: patl_checkpoint patl_checkpoint_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_checkpoint patl_checkpoint_route_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_check_item; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_check_item (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    checkpoint_id uuid NOT NULL,
    seq smallint NOT NULL,
    item_name character varying(255) NOT NULL,
    check_type character varying(16) DEFAULT 'enum'::character varying NOT NULL,
    std_value character varying(64),
    enum_values character varying(255),
    is_required boolean DEFAULT true NOT NULL
);


--
-- Name: TABLE patl_check_item; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_check_item IS '点位检查项(可配:枚举/数值/文本)';


--
-- Name: patl_check_item patl_check_item_checkpoint_id_seq_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_check_item
    ADD CONSTRAINT patl_check_item_checkpoint_id_seq_key UNIQUE (checkpoint_id, seq);


--
-- Name: patl_check_item patl_check_item_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_check_item
    ADD CONSTRAINT patl_check_item_pkey PRIMARY KEY (id);


--
-- Name: patl_check_item patl_check_item_checkpoint_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_check_item patl_check_item_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_task; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_task (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    task_no character varying(32) NOT NULL,
    route_id uuid NOT NULL,
    shift character varying(8),
    plan_time timestamp with time zone,
    actual_time timestamp with time zone,
    finish_time timestamp with time zone,
    inspector_id uuid,
    status character varying(16) DEFAULT '待巡检'::character varying NOT NULL,
    total_points integer DEFAULT 0 NOT NULL,
    done_points integer DEFAULT 0 NOT NULL,
    abnormal_count integer DEFAULT 0 NOT NULL,
    remark text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE patl_task; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_task IS '巡检任务(按路线生成,含班次+状态)';


--
-- Name: patl_task patl_task_org_id_task_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_task
    ADD CONSTRAINT patl_task_org_id_task_no_key UNIQUE (org_id, task_no);


--
-- Name: patl_task patl_task_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_task
    ADD CONSTRAINT patl_task_pkey PRIMARY KEY (id);


--
-- Name: patl_task patl_task_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_task patl_task_route_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_abnormal; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_abnormal (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    task_id uuid NOT NULL,
    record_id uuid,
    checkpoint_name character varying(128) NOT NULL,
    description text NOT NULL,
    severity character varying(8) DEFAULT '一般'::character varying NOT NULL,
    status character varying(16) DEFAULT '待处理'::character varying NOT NULL,
    d8_id uuid,
    ncm_record_id uuid,
    handle_remark text,
    handled_by uuid,
    handled_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE patl_abnormal; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_abnormal IS '巡检异常(可转 NCM/8D)';


--
-- Name: patl_abnormal patl_abnormal_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_abnormal
    ADD CONSTRAINT patl_abnormal_pkey PRIMARY KEY (id);


--
-- Name: patl_abnormal patl_abnormal_d8_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_abnormal patl_abnormal_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_abnormal patl_abnormal_record_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_abnormal patl_abnormal_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: patl_archived_report; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.patl_archived_report (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    archive_no character varying(32) NOT NULL,
    task_id uuid NOT NULL,
    task_no character varying(32) NOT NULL,
    route_id uuid,
    archive_date date NOT NULL,
    status character varying(16) DEFAULT '已归档'::character varying NOT NULL,
    pdf_ref character varying(255) NOT NULL,
    report_hash character(64) NOT NULL,
    retention_until date NOT NULL
);


--
-- Name: TABLE patl_archived_report; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.patl_archived_report IS '巡检任务归档报告(15 年留存)';


--
-- Name: patl_archived_report patl_archived_report_archive_no_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_archived_report
    ADD CONSTRAINT patl_archived_report_archive_no_key UNIQUE (archive_no);


--
-- Name: patl_archived_report patl_archived_report_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.patl_archived_report
    ADD CONSTRAINT patl_archived_report_pkey PRIMARY KEY (id);


--
-- Name: idx_patl_archive_date; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_patl_archive_date ON ops.patl_archived_report USING btree (org_id, archive_date);


--
-- Name: idx_patl_archive_task; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_patl_archive_task ON ops.patl_archived_report USING btree (org_id, task_id);


--
-- Name: patl_archived_report patl_archived_report_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: patl_archived_report patl_archived_report_task_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



