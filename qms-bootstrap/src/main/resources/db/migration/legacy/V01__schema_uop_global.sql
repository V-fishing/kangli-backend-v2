-- V01: 地基 - 扩展 + ops schema + UUIDv7 函数 + UOP + 全局核心表
-- 翻译规则(按代码规范文档):id BIGSERIAL -> UUIDv7;表加 ops. 前缀;
-- BIGINT 外键/created_by/updated_by -> UUID;保留 org_id(公司=顶级 org)/version/is_deleted;
-- 删 company_id/sys_company/sys_user_company(简化多分公司不要)。

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_bytes
CREATE EXTENSION IF NOT EXISTS ltree;      -- sys_org.path 物化路径

CREATE SCHEMA IF NOT EXISTS ops;

-- UUIDv7 生成(代码规范要求 UUIDv7;PG16 无内置,PL/pgSQL 实现:48bit 毫秒时间戳 + 随机)
-- SET search_path = ops, public:确保 gen_random_bytes(pgcrypto)无论调用方 search_path 都能解析
CREATE OR REPLACE FUNCTION ops.gen_uuid_v7() RETURNS UUID
LANGUAGE plpgsql VOLATILE
SET search_path = ops, public
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

-- ============ UOP 用户-组织-权限 ============

-- 组织树(公司->工厂->车间->产线->工位,最深10层;公司=顶级 org)
CREATE TABLE ops.sys_org (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  org_code   VARCHAR(32) NOT NULL UNIQUE,
  org_name   VARCHAR(128) NOT NULL,
  parent_id  UUID REFERENCES ops.sys_org(id),
  path       LTREE,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  org_type   VARCHAR(16),                      -- 公司/工厂/车间/产线/工位
  status     VARCHAR(8) NOT NULL DEFAULT '启用',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_org_parent ON ops.sys_org(parent_id);
CREATE INDEX idx_org_path   ON ops.sys_org USING GIST(path);
COMMENT ON TABLE ops.sys_org IS '组织树(LTREE 物化路径,最深10层;公司=顶级 org)';

-- 用户
CREATE TABLE ops.sys_user (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  username      VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,        -- bcrypt
  pwd_history   JSONB,                        -- 最近3次密码
  real_name     VARCHAR(64) NOT NULL,
  emp_no        VARCHAR(32),
  org_id        UUID REFERENCES ops.sys_org(id),
  email         VARCHAR(128),
  phone         VARCHAR(32),
  status        VARCHAR(16) NOT NULL DEFAULT '启用',  -- 启用/停用/锁定
  pwd_expire_at DATE,
  last_login_at TIMESTAMPTZ,
  last_login_ip VARCHAR(64),
  fail_count    INTEGER NOT NULL DEFAULT 0,
  lock_until    TIMESTAMPTZ,
  inactive_since DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sys_user IS '用户(密码策略/锁定/不活跃禁用)';

-- 角色
CREATE TABLE ops.sys_role (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  role_code  VARCHAR(32) NOT NULL UNIQUE,
  role_name  VARCHAR(64) NOT NULL,
  role_type  VARCHAR(8) NOT NULL DEFAULT '预置',
  perm_desc  VARCHAR(255),
  status     VARCHAR(8) NOT NULL DEFAULT '启用',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sys_role IS '角色(预置 + 自定义)';

-- 用户-角色
CREATE TABLE ops.sys_user_role (
  id      UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  user_id UUID NOT NULL REFERENCES ops.sys_user(id),
  role_id UUID NOT NULL REFERENCES ops.sys_role(id),
  UNIQUE(user_id, role_id)
);

-- 菜单(树形)
CREATE TABLE ops.sys_menu (
  id         UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  parent_id  UUID REFERENCES ops.sys_menu(id),
  menu_code  VARCHAR(32) NOT NULL UNIQUE,
  menu_name  VARCHAR(64) NOT NULL,
  menu_type  VARCHAR(8) NOT NULL,             -- 目录/菜单/按钮
  path       VARCHAR(128),
  component  VARCHAR(128),
  icon       VARCHAR(32),
  sort_order SMALLINT NOT NULL DEFAULT 0,
  visible    BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);

-- 按钮
CREATE TABLE ops.sys_button (
  id       UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  menu_id  UUID NOT NULL REFERENCES ops.sys_menu(id),
  btn_code VARCHAR(32) NOT NULL,
  btn_name VARCHAR(64) NOT NULL,
  UNIQUE(menu_id, btn_code)
);

-- 角色-菜单
CREATE TABLE ops.sys_role_menu (
  id      UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  role_id UUID NOT NULL REFERENCES ops.sys_role(id),
  menu_id UUID NOT NULL REFERENCES ops.sys_menu(id),
  UNIQUE(role_id, menu_id)
);

-- 角色-按钮
CREATE TABLE ops.sys_role_button (
  id        UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  role_id   UUID NOT NULL REFERENCES ops.sys_role(id),
  button_id UUID NOT NULL REFERENCES ops.sys_button(id),
  UNIQUE(role_id, button_id)
);

-- 数据范围(三级:ALL/ORG_AND_SUB/SELF)
CREATE TABLE ops.sys_data_scope (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  role_id      UUID NOT NULL REFERENCES ops.sys_role(id),
  menu_id      UUID NOT NULL REFERENCES ops.sys_menu(id),
  scope_type   VARCHAR(16) NOT NULL,
  scope_org_id UUID,
  UNIQUE(role_id, menu_id)
);
COMMENT ON TABLE ops.sys_data_scope IS '数据范围(三级 RBAC)';

-- 代班委派
CREATE TABLE ops.sys_delegation (
  id            UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  delegator_id  UUID NOT NULL REFERENCES ops.sys_user(id),
  delegatee_id  UUID NOT NULL REFERENCES ops.sys_user(id),
  role_id       UUID NOT NULL REFERENCES ops.sys_role(id),
  start_at      TIMESTAMPTZ NOT NULL,
  end_at        TIMESTAMPTZ NOT NULL,
  status        VARCHAR(16) NOT NULL DEFAULT '生效',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0
);
COMMENT ON TABLE ops.sys_delegation IS '代班委派(到期自动失效)';

-- 角色-模块横幅(配置)
CREATE TABLE ops.role_module_banner (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  module       VARCHAR(8) NOT NULL,
  role_code    VARCHAR(32) NOT NULL,
  banner_title VARCHAR(64) NOT NULL,
  banner_desc  TEXT NOT NULL,
  UNIQUE(module, role_code)
);

-- 角色-模块权限矩阵
CREATE TABLE ops.role_module_permission (
  id           UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  role_code    VARCHAR(32) NOT NULL,
  module       VARCHAR(8) NOT NULL,
  access_level VARCHAR(16) NOT NULL,          -- 读写/只读/弱关联只读/无权限
  panel_key    VARCHAR(32),
  UNIQUE(role_code, module)
);

-- ============ 全局:电子签名哈希链(Part 11,写审计库) ============
CREATE TABLE ops.qms_esign_log (
  id              UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  biz_type        VARCHAR(32) NOT NULL,
  biz_id          VARCHAR(64) NOT NULL,
  sign_role       VARCHAR(16) NOT NULL,
  signer_user_id  UUID NOT NULL,
  signer_username VARCHAR(64) NOT NULL,
  sign_method     VARCHAR(16) NOT NULL,
  sign_time       TIMESTAMPTZ NOT NULL DEFAULT now(),
  client_ip       VARCHAR(64),
  content_hash    CHAR(64) NOT NULL,
  prev_hash       CHAR(64),
  chain_hash      CHAR(64) NOT NULL,
  status          VARCHAR(16) NOT NULL DEFAULT '成功',
  fail_count      INTEGER NOT NULL DEFAULT 0,
  is_rejected     BOOLEAN NOT NULL DEFAULT false,
  remark          TEXT
);
CREATE UNIQUE INDEX uk_esign_chain ON ops.qms_esign_log(chain_hash);
CREATE INDEX idx_esign_biz ON ops.qms_esign_log(biz_type, biz_id);
COMMENT ON TABLE ops.qms_esign_log IS '电子签名哈希链日志(Part 11,不可篡改)';
