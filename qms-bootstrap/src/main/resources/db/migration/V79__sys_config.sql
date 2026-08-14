-- V79 系统开关表:支撑「组织切换是否影响写入归属」等可配置开关(key-value 型全局配置)。
CREATE TABLE IF NOT EXISTS ops.sys_config (
    id           uuid PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
    config_key   varchar(128) NOT NULL UNIQUE,
    config_value text,
    remark       varchar(255),
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    created_by   varchar(64),
    updated_by   varchar(64),
    is_deleted   boolean NOT NULL DEFAULT false,
    version      integer NOT NULL DEFAULT 0
);

-- 种子:组织切换写入归属开关(默认关闭——仅查询过滤;开启后新建/修改数据归属所选分公司)。
INSERT INTO ops.sys_config (id, config_key, config_value, remark, created_at, updated_at)
SELECT ops.gen_uuid_v7(), 'org.switch.affectsWrite', 'false',
       '组织切换是否影响新建/修改数据的归属组织(true=归属所选分公司,false=仅查询过滤)', now(), now()
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_config WHERE config_key = 'org.switch.affectsWrite');
