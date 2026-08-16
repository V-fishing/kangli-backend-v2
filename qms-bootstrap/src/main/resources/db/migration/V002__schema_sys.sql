-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [系统/权限/基础 (SYS)]
-- ----------------------------------------------------------------------------
-- 板块 : 系统/权限/基础 (SYS)
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
-- Name: todo_item; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.todo_item (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    title text NOT NULL,
    biz_type character varying(32) NOT NULL,
    biz_id character varying(64) NOT NULL,
    source_module character varying(16) NOT NULL,
    assignee_role character varying(32),
    assignee_user_id uuid,
    priority character varying(8) DEFAULT 'normal'::character varying,
    status character varying(16) DEFAULT '待处理'::character varying NOT NULL,
    due_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE todo_item; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.todo_item IS '统一待办收件箱(各模块推送)';


--
-- Name: todo_item todo_item_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.todo_item
    ADD CONSTRAINT todo_item_pkey PRIMARY KEY (id);


--
-- Name: idx_todo_assignee; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_todo_assignee ON ops.todo_item USING btree (org_id, assignee_user_id, status);


--
-- Name: todo_item todo_item_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_org; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_org (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_code character varying(32) NOT NULL,
    org_name character varying(128) NOT NULL,
    parent_id uuid,
    path ops.ltree,
    sort_order smallint DEFAULT 0 NOT NULL,
    org_type character varying(16),
    status character varying(8) DEFAULT '启用'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sys_org; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_org IS '组织树(LTREE 物化路径,最深10层;公司=顶级 org)';


--
-- Name: sys_org sys_org_org_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_org
    ADD CONSTRAINT sys_org_org_code_key UNIQUE (org_code);


--
-- Name: sys_org sys_org_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_org
    ADD CONSTRAINT sys_org_pkey PRIMARY KEY (id);


--
-- Name: idx_org_parent; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_org_parent ON ops.sys_org USING btree (parent_id);


--
-- Name: idx_org_path; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_org_path ON ops.sys_org USING gist (path);


--
-- Name: sys_org sys_org_parent_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_user; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_user (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    username character varying(64) NOT NULL,
    password_hash character varying(128) NOT NULL,
    pwd_history jsonb,
    real_name character varying(64) NOT NULL,
    emp_no character varying(32),
    org_id uuid,
    email character varying(128),
    phone character varying(32),
    status character varying(16) DEFAULT '启用'::character varying NOT NULL,
    pwd_expire_at date,
    last_login_at timestamp with time zone,
    last_login_ip character varying(64),
    fail_count integer DEFAULT 0 NOT NULL,
    lock_until timestamp with time zone,
    inactive_since date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sys_user; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_user IS '用户(密码策略/锁定/不活跃禁用)';


--
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (id);


--
-- Name: sys_user sys_user_username_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_user
    ADD CONSTRAINT sys_user_username_key UNIQUE (username);


--
-- Name: sys_user sys_user_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_user_role; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_user_role (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


--
-- Name: sys_user_role sys_user_role_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (id);


--
-- Name: sys_user_role sys_user_role_user_id_role_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_user_role
    ADD CONSTRAINT sys_user_role_user_id_role_id_key UNIQUE (user_id, role_id);


--
-- Name: sys_user_role sys_user_role_role_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_user_role sys_user_role_user_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_role; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_role (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    role_code character varying(32) NOT NULL,
    role_name character varying(64) NOT NULL,
    role_type character varying(8) DEFAULT '预置'::character varying NOT NULL,
    perm_desc character varying(255),
    status character varying(8) DEFAULT '启用'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    org_id uuid
);


--
-- Name: TABLE sys_role; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_role IS '角色(预置 + 自定义)';


--
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);


--
-- Name: idx_sys_role_org; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sys_role_org ON ops.sys_role USING btree (org_id);


--
-- Name: uq_sys_role_code_global; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uq_sys_role_code_global ON ops.sys_role USING btree (role_code) WHERE (org_id IS NULL);


--
-- Name: uq_sys_role_code_org; Type: INDEX; Schema: ops; Owner: -
--

CREATE UNIQUE INDEX uq_sys_role_code_org ON ops.sys_role USING btree (role_code, org_id) WHERE (org_id IS NOT NULL);


--
-- Name: sys_role sys_role_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_menu; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_menu (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    parent_id uuid,
    menu_code character varying(32) NOT NULL,
    menu_name character varying(64) NOT NULL,
    menu_type character varying(8) NOT NULL,
    path character varying(128),
    component character varying(128),
    icon character varying(32),
    sort_order smallint DEFAULT 0 NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sys_menu sys_menu_menu_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_menu
    ADD CONSTRAINT sys_menu_menu_code_key UNIQUE (menu_code);


--
-- Name: sys_menu sys_menu_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_menu
    ADD CONSTRAINT sys_menu_pkey PRIMARY KEY (id);


--
-- Name: sys_menu sys_menu_parent_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_button; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_button (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    menu_id uuid NOT NULL,
    btn_code character varying(32) NOT NULL,
    btn_name character varying(64) NOT NULL
);


--
-- Name: sys_button sys_button_menu_id_btn_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_button
    ADD CONSTRAINT sys_button_menu_id_btn_code_key UNIQUE (menu_id, btn_code);


--
-- Name: sys_button sys_button_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_button
    ADD CONSTRAINT sys_button_pkey PRIMARY KEY (id);


--
-- Name: sys_button sys_button_menu_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_role_menu; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_role_menu (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    role_id uuid NOT NULL,
    menu_id uuid NOT NULL
);


--
-- Name: sys_role_menu sys_role_menu_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_role_menu
    ADD CONSTRAINT sys_role_menu_pkey PRIMARY KEY (id);


--
-- Name: sys_role_menu sys_role_menu_role_id_menu_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_role_menu
    ADD CONSTRAINT sys_role_menu_role_id_menu_id_key UNIQUE (role_id, menu_id);


--
-- Name: sys_role_menu sys_role_menu_menu_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_role_menu sys_role_menu_role_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_role_button; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_role_button (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    role_id uuid NOT NULL,
    button_id uuid NOT NULL
);


--
-- Name: sys_role_button sys_role_button_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_role_button
    ADD CONSTRAINT sys_role_button_pkey PRIMARY KEY (id);


--
-- Name: sys_role_button sys_role_button_role_id_button_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_role_button
    ADD CONSTRAINT sys_role_button_role_id_button_id_key UNIQUE (role_id, button_id);


--
-- Name: sys_role_button sys_role_button_button_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_role_button sys_role_button_role_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_data_scope; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_data_scope (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    role_id uuid NOT NULL,
    menu_id uuid NOT NULL,
    scope_type character varying(16) NOT NULL,
    scope_org_id uuid
);


--
-- Name: TABLE sys_data_scope; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_data_scope IS '数据范围(三级 RBAC)';


--
-- Name: sys_data_scope sys_data_scope_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_data_scope
    ADD CONSTRAINT sys_data_scope_pkey PRIMARY KEY (id);


--
-- Name: sys_data_scope sys_data_scope_role_id_menu_id_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_data_scope
    ADD CONSTRAINT sys_data_scope_role_id_menu_id_key UNIQUE (role_id, menu_id);


--
-- Name: sys_data_scope sys_data_scope_menu_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_data_scope sys_data_scope_role_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_delegation; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_delegation (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    delegator_id uuid NOT NULL,
    delegatee_id uuid NOT NULL,
    role_id uuid NOT NULL,
    start_at timestamp with time zone NOT NULL,
    end_at timestamp with time zone NOT NULL,
    status character varying(16) DEFAULT '生效'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sys_delegation; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_delegation IS '代班委派(到期自动失效)';


--
-- Name: sys_delegation sys_delegation_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_delegation
    ADD CONSTRAINT sys_delegation_pkey PRIMARY KEY (id);


--
-- Name: sys_delegation sys_delegation_delegatee_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_delegation sys_delegation_delegator_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
--



--
-- Name: sys_delegation sys_delegation_role_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: role_module_banner; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.role_module_banner (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    module character varying(8) NOT NULL,
    role_code character varying(32) NOT NULL,
    banner_title character varying(64) NOT NULL,
    banner_desc text NOT NULL
);


--
-- Name: role_module_banner role_module_banner_module_role_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.role_module_banner
    ADD CONSTRAINT role_module_banner_module_role_code_key UNIQUE (module, role_code);


--
-- Name: role_module_banner role_module_banner_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.role_module_banner
    ADD CONSTRAINT role_module_banner_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: role_module_permission; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.role_module_permission (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    role_code character varying(32) NOT NULL,
    module character varying(8) NOT NULL,
    access_level character varying(16) NOT NULL,
    panel_key character varying(32)
);


--
-- Name: role_module_permission role_module_permission_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.role_module_permission
    ADD CONSTRAINT role_module_permission_pkey PRIMARY KEY (id);


--
-- Name: role_module_permission role_module_permission_role_code_module_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.role_module_permission
    ADD CONSTRAINT role_module_permission_role_code_module_key UNIQUE (role_code, module);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: audit_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.audit_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    biz_type character varying(32) NOT NULL,
    biz_id character varying(64),
    operator_id uuid NOT NULL,
    operator_name character varying(64) NOT NULL,
    operate_time timestamp with time zone DEFAULT now() NOT NULL,
    client_ip character varying(64),
    user_agent character varying(255),
    action character varying(64) NOT NULL,
    before_value jsonb,
    after_value jsonb,
    remark text
);


--
-- Name: TABLE audit_log; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.audit_log IS '操作审计日志(写审计库,不可删改,ALCOA+)';


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: idx_audit_biz; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_biz ON ops.audit_log USING btree (biz_type, biz_id);


--
-- Name: idx_audit_op; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_op ON ops.audit_log USING btree (operator_id, operate_time);


--
-- Name: audit_log audit_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_attachment; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_attachment (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    biz_type character varying(32) NOT NULL,
    biz_id character varying(64) NOT NULL,
    file_name character varying(255) NOT NULL,
    file_path character varying(512) NOT NULL,
    file_hash character(64) NOT NULL,
    file_size bigint,
    mime_type character varying(64),
    uploaded_by uuid,
    uploaded_at timestamp with time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);


--
-- Name: TABLE sys_attachment; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_attachment IS '通用附件(照片/报告/证据,MinIO 存储)';


--
-- Name: sys_attachment sys_attachment_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_attachment
    ADD CONSTRAINT sys_attachment_pkey PRIMARY KEY (id);


--
-- Name: idx_attach_biz; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_attach_biz ON ops.sys_attachment USING btree (org_id, biz_type, biz_id);


--
-- Name: sys_attachment sys_attachment_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: sys_dict; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_dict (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    dict_type character varying(32) NOT NULL,
    dict_key character varying(32) NOT NULL,
    dict_value character varying(64) NOT NULL,
    sort_order smallint DEFAULT 0 NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE sys_dict; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_dict IS '通用字典(枚举集中管理)';


--
-- Name: sys_dict sys_dict_dict_type_dict_key_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_dict
    ADD CONSTRAINT sys_dict_dict_type_dict_key_key UNIQUE (dict_type, dict_key);


--
-- Name: sys_dict sys_dict_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_dict
    ADD CONSTRAINT sys_dict_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: sys_audit_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_audit_log (
    id bigint NOT NULL,
    module character varying(32) NOT NULL,
    action character varying(16) NOT NULL,
    method character varying(128),
    operator_id character varying(64),
    operator_name character varying(64) DEFAULT 'anonymous'::character varying NOT NULL,
    record_id character varying(64),
    detail character varying(512),
    status character varying(8) DEFAULT 'SUCCESS'::character varying NOT NULL,
    error text,
    cost_ms integer,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE sys_audit_log; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.sys_audit_log IS '操作审计日志(AuditAspect 自动写入)';


--
-- Name: COLUMN sys_audit_log.module; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sys_audit_log.module IS '模块: FIA/SPC/NCM/SQM/UOP';


--
-- Name: COLUMN sys_audit_log.action; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sys_audit_log.action IS '动作: CREATE/UPDATE/APPROVE/CLOSE/DELETE';


--
-- Name: COLUMN sys_audit_log.operator_name; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sys_audit_log.operator_name IS '操作人账号(从 JWT 解析)';


--
-- Name: COLUMN sys_audit_log.record_id; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sys_audit_log.record_id IS '被操作记录的 ID(UUID)';


--
-- Name: COLUMN sys_audit_log.cost_ms; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.sys_audit_log.cost_ms IS '耗时(毫秒)';


--
-- Name: sys_audit_log_id_seq; Type: SEQUENCE; Schema: ops; Owner: -
--

ALTER TABLE ops.sys_audit_log ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ops.sys_audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: sys_audit_log sys_audit_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_audit_log
    ADD CONSTRAINT sys_audit_log_pkey PRIMARY KEY (id);


--
-- Name: idx_audit_action; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_action ON ops.sys_audit_log USING btree (action);


--
-- Name: idx_audit_module; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_module ON ops.sys_audit_log USING btree (module);


--
-- Name: idx_audit_record; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_record ON ops.sys_audit_log USING btree (record_id);


--
-- Name: idx_audit_time; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_audit_time ON ops.sys_audit_log USING btree (created_at DESC);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: sys_notification; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_notification (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    user_id character varying(64) NOT NULL,
    user_name character varying(64),
    title character varying(255) NOT NULL,
    content text,
    biz_type character varying(32),
    biz_id character varying(64),
    link character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: sys_notification sys_notification_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_notification
    ADD CONSTRAINT sys_notification_pkey PRIMARY KEY (id);


--
-- Name: idx_sys_notification_user; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sys_notification_user ON ops.sys_notification USING btree (user_id, is_read);


--
-- Name: idx_sys_notification_user_created; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_sys_notification_user_created ON ops.sys_notification USING btree (user_id, created_at DESC);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: sys_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.sys_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    config_key character varying(128) NOT NULL,
    config_value text,
    remark character varying(255),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    created_by character varying(64),
    updated_by character varying(64),
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: sys_config sys_config_config_key_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_config
    ADD CONSTRAINT sys_config_config_key_key UNIQUE (config_key);


--
-- Name: sys_config sys_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.sys_config
    ADD CONSTRAINT sys_config_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--



