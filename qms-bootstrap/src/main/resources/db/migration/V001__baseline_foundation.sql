-- ============================================================================
-- 康立 QMS 数据库结构基线 - 地基 (BASELINE FOUNDATION)
-- ----------------------------------------------------------------------------
-- 内容 : 必需扩展 + CREATE SCHEMA ops + 全部公共函数 (gen_uuid_v7 等)
-- 说明 : 所有业务表 id 默认值依赖本文件函数, 必须最先执行。
--        各板块表结构见 V002~V016, 外键见 V017。
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS ops;

CREATE EXTENSION IF NOT EXISTS ltree WITH SCHEMA ops;
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA ops;
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA ops;

--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Name: ops; Type: SCHEMA; Schema: -; Owner: -
--

--
-- Name: gen_uuid_v7(); Type: FUNCTION; Schema: ops; Owner: -
--

CREATE FUNCTION ops.gen_uuid_v7() RETURNS uuid
    LANGUAGE plpgsql
    SET search_path TO 'ops', 'public'
    AS $$
DECLARE
  ts BIGINT;
  b  BYTEA;
BEGIN
  ts := (EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::BIGINT;
  b  := gen_random_bytes(16);
  b := set_byte(b, 0, ((ts >> 40) & 255)::int);
  b := set_byte(b, 1, ((ts >> 32) & 255)::int);
  b := set_byte(b, 2, ((ts >> 24) & 255)::int);
  b := set_byte(b, 3, ((ts >> 16) & 255)::int);
  b := set_byte(b, 4, ((ts >> 8) & 255)::int);
  b := set_byte(b, 5, (ts & 255)::int);
  b := set_byte(b, 6, (get_byte(b, 6) & 15) | 112);   -- version 7
  b := set_byte(b, 8, (get_byte(b, 8) & 63) | 128);   -- variant 10
  RETURN encode(b, 'hex')::UUID;
END;
$$;


--
-- Name: sqm_calc_abnormal_level(numeric); Type: FUNCTION; Schema: ops; Owner: -
--

CREATE FUNCTION ops.sqm_calc_abnormal_level(p_qty numeric) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_min integer;
BEGIN
    SELECT COALESCE(severe_min_qty, 3) INTO v_min
    FROM ops.sqm_abnormal_rule
    WHERE org_id IS NULL
    LIMIT 1;
    IF v_min IS NULL THEN
        v_min := 3;
    END IF;
    IF p_qty IS NULL OR p_qty < v_min THEN
        RETURN '一般';
    END IF;
    RETURN '严重';
END;
$$;




--
-- Name: audit_log; Type: TABLE; Schema: ops; Owner: -
--

