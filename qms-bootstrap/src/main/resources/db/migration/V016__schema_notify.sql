-- ============================================================================
-- 康立 QMS 数据库结构基线 - 表结构 [通知 (NOTIFY)]
-- ----------------------------------------------------------------------------
-- 板块 : 通知 (NOTIFY)
-- 内容 : 4 张表 (CREATE TABLE / 索引 / 非外键约束)
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
-- Name: notification_log; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.notification_log (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid NOT NULL,
    biz_type character varying(32) NOT NULL,
    biz_id character varying(64) NOT NULL,
    channel character varying(16) NOT NULL,
    receiver character varying(128) NOT NULL,
    content text NOT NULL,
    level character varying(8),
    send_status character varying(16) NOT NULL,
    fail_reason text,
    retry_count integer DEFAULT 0 NOT NULL,
    sent_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE notification_log; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.notification_log IS '通知发送日志(多通道,失败重试)';


--
-- Name: notification_log notification_log_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notification_log
    ADD CONSTRAINT notification_log_pkey PRIMARY KEY (id);


--
-- Name: idx_notif_biz; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_notif_biz ON ops.notification_log USING btree (org_id, biz_type, biz_id);


--
-- Name: notification_log notification_log_org_id_fkey; Type: FK CONSTRAINT; Schema: ops; Owner: -
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
-- Name: notify_channel; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.notify_channel (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    channel character varying(32) NOT NULL,
    webhook_url text,
    is_enabled boolean DEFAULT false NOT NULL,
    level character varying(16) DEFAULT '普通'::character varying,
    remark character varying(128),
    channel_type character varying(16) DEFAULT 'webhook'::character varying NOT NULL,
    config_json text
);


--
-- Name: TABLE notify_channel; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.notify_channel IS '全局通知外部渠道(钉钉/企微/自定义webhook)';


--
-- Name: COLUMN notify_channel.channel_type; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.notify_channel.channel_type IS '渠道类型: webhook=群机器人 / direct=点对点';


--
-- Name: COLUMN notify_channel.config_json; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.notify_channel.config_json IS '渠道凭据 JSON(secret 字段返回时脱敏)';


--
-- Name: notify_channel notify_channel_channel_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notify_channel
    ADD CONSTRAINT notify_channel_channel_key UNIQUE (channel);


--
-- Name: notify_channel notify_channel_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notify_channel
    ADD CONSTRAINT notify_channel_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: notify_message; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.notify_message (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    org_id uuid,
    sender_id uuid,
    sender_name character varying(64),
    receiver_id uuid,
    receiver_name character varying(64),
    receiver_type character varying(16) DEFAULT 'user'::character varying,
    channel character varying(32) NOT NULL,
    channel_type character varying(16) DEFAULT 'direct'::character varying,
    title character varying(256),
    content text,
    biz_type character varying(32),
    biz_id character varying(64),
    biz_no character varying(64),
    status character varying(16) DEFAULT '发送中'::character varying NOT NULL,
    fail_reason character varying(512),
    send_time timestamp with time zone DEFAULT now(),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    is_deleted boolean DEFAULT false NOT NULL,
    version integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE notify_message; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.notify_message IS '点对点通知发送记录';


--
-- Name: notify_message notify_message_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notify_message
    ADD CONSTRAINT notify_message_pkey PRIMARY KEY (id);


--
-- Name: idx_notify_message_create; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_notify_message_create ON ops.notify_message USING btree (created_at DESC);


--
-- Name: idx_notify_message_receiver; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_notify_message_receiver ON ops.notify_message USING btree (receiver_id);


--
-- Name: idx_notify_message_status; Type: INDEX; Schema: ops; Owner: -
--

CREATE INDEX idx_notify_message_status ON ops.notify_message USING btree (status);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)




--
-- Name: notify_config; Type: TABLE; Schema: ops; Owner: -
--

CREATE TABLE ops.notify_config (
    id uuid DEFAULT ops.gen_uuid_v7() NOT NULL,
    module character varying(32) NOT NULL,
    event_code character varying(64) NOT NULL,
    event_name character varying(128) NOT NULL,
    role_codes character varying(256),
    channels character varying(256) DEFAULT '站内弹窗'::character varying,
    enabled boolean DEFAULT true NOT NULL,
    org_id uuid,
    receiver_ids text
);


--
-- Name: TABLE notify_config; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON TABLE ops.notify_config IS '统一通知配置: 模块×事件 的接收角色与外部渠道';


--
-- Name: COLUMN notify_config.receiver_ids; Type: COMMENT; Schema: ops; Owner: -
--

COMMENT ON COLUMN ops.notify_config.receiver_ids IS '具体接收人用户ID(逗号分隔, 可空); 与 role_codes 并存取并集';


--
-- Name: notify_config notify_config_module_event_code_key; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notify_config
    ADD CONSTRAINT notify_config_module_event_code_key UNIQUE (module, event_code);


--
-- Name: notify_config notify_config_pkey; Type: CONSTRAINT; Schema: ops; Owner: -
--

ALTER TABLE ONLY ops.notify_config
    ADD CONSTRAINT notify_config_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--



