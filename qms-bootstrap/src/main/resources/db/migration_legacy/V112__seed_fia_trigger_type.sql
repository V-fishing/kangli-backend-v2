-- FIA 首件检验-触发类型字典(主数据, org_id=NULL=全局)
-- name 有 UNIQUE 约束, 幂等: 已存在的名称跳过, 不覆盖用户后续的编辑/停用状态。
INSERT INTO ops.fia_trigger_type (id, org_id, name, is_enabled, description, created_at, updated_at, is_deleted, version)
SELECT ops.gen_uuid_v7(), NULL, v.name, true, v.description, now(), now(), false, 0
FROM (VALUES
    ('换批次', '批次更换时触发首件检验'),
    ('换模',   '更换模具后触发首件检验'),
    ('换设备', '更换设备后触发首件检验'),
    ('首班',   '每日首班开工时触发首件检验'),
    ('换料',   '更换原材料后触发首件检验'),
    ('停线重启', '产线停机后恢复生产时触发首件检验'),
    ('来料入库', '来料入库时触发首件检验'),
    ('换规格', '产品规格切换时触发首件检验'),
    ('新工艺', '新工艺/新工序投产时触发首件检验'),
    ('设备维修', '设备维修后恢复生产时触发首件检验')
) AS v(name, description)
WHERE NOT EXISTS (SELECT 1 FROM ops.fia_trigger_type t WHERE t.name = v.name);
