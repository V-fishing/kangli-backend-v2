-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [SQM 供应商绩效 (SQM-PERF)]
-- ----------------------------------------------------------------------------
-- 板块 : SQM 供应商绩效 (SQM-PERF)
-- 内容 : 1 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: sqm_perf_metric_cfg; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sqm_perf_metric_cfg (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    metric_code character varying(32) NOT NULL,
    metric_name character varying(64) NOT NULL,
    weight numeric(5,2) DEFAULT 0 NOT NULL,
    target numeric(5,2),
    challenge numeric(5,2),
    enabled boolean DEFAULT true NOT NULL,
    auto_linkage boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sqm_perf_metric_cfg; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sqm_perf_metric_cfg IS '供应商绩效指标配置(权重/阈值,calc 读取而非写死)';


--
-- Name: sqm_perf_metric_cfg sqm_perf_metric_cfg_metric_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_perf_metric_cfg
    ADD CONSTRAINT sqm_perf_metric_cfg_metric_code_key UNIQUE (metric_code);


--
-- Name: sqm_perf_metric_cfg sqm_perf_metric_cfg_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sqm_perf_metric_cfg
    ADD CONSTRAINT sqm_perf_metric_cfg_pkey PRIMARY KEY (id);


--
-- Name: sqm_perf_metric_cfg sqm_perf_metric_cfg_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



