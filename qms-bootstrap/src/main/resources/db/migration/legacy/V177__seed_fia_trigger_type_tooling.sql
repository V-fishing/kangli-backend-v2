-- FIA 首件检验-触发类型字典: 工装相关触发类型
-- 幂等: 已存在的名称跳过，不覆盖已有状态
INSERT INTO ops.fia_trigger_type (id, org_id, name, is_enabled, description, created_at, updated_at, is_deleted, version)
SELECT ops.gen_uuid_v7(), NULL, v.name, true, v.description, now(), now(), false, 0
FROM (VALUES
    ('工装维修后', '工装维修完成后恢复投用时触发首件检验'),
    ('工装变更后', '工装版本变更(设计变更/升级)后触发首件检验')
) AS v(name, description)
WHERE NOT EXISTS (SELECT 1 FROM ops.fia_trigger_type t WHERE t.name = v.name);