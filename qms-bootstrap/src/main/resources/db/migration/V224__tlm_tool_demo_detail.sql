-- V224: 工装详情页四个 Tab 的演示数据(保养历史/产品关联/版本变更/维修历史)。
-- 目标工装: SKL-J-20260011 (多通道GL电极活化工装, id 固定), org=MZ。
-- 目的: 让工装详情页四个 Tab 有可读数据,供操作指南编写。全部幂等(NOT EXISTS)。
-- 不使用 DO $$ 匿名块(此前 V223 首版因此解析失败),逐条独立 INSERT。

-- ===== 1) 保养计划(tlm_maint_plan) 供保养记录关联 =====
INSERT INTO ops.tlm_maint_plan (id, org_id, tool_id, plan_no, cycle_type, next_date, responsible_id, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43',
     'MP-DEMO-20260011','MONTH','2026-09-14',
     (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name='梅州操作员' AND o.org_code='MZ' LIMIT 1),
     '月度保养:清洁与功能点检', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_maint_plan WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND plan_no='MP-DEMO-20260011');

-- ===== 2) 保养历史(tlm_maint_record) =====
INSERT INTO ops.tlm_maint_record (id, org_id, plan_id, tool_id, maint_date, result, responsible_id, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     (SELECT id FROM ops.tlm_maint_plan WHERE plan_no='MP-DEMO-20260011' AND org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ')),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', v.d, v.r,
     (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name='梅州操作员' AND o.org_code='MZ' LIMIT 1),
     now()
FROM (VALUES ('2026-07-14'::date,'清洁电极卡槽,功能点检正常,无异常'),('2026-08-14'::date,'更换老化密封圈,紧固连接件,运行平稳')) AS v(d, r)
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_maint_record WHERE tool_id='019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43' AND maint_date=v.d);

-- ===== 3) 产品关联(tlm_tool_product) =====
INSERT INTO ops.tlm_tool_product (id, org_id, tool_id, product_code, product_name, kind, spec_model, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','P-GL-V5','V5血气电解质分析仪','FINISHED','V5', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tool_product WHERE tool_id='019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43' AND product_code='P-GL-V5');

INSERT INTO ops.tlm_tool_product (id, org_id, tool_id, product_code, product_name, kind, spec_model, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','P-GL-V8','V8血气电解质分析仪','FINISHED','V8', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tool_product WHERE tool_id='019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43' AND product_code='P-GL-V8');

-- ===== 4) 版本变更(tlm_tool_version) =====
INSERT INTO ops.tlm_tool_version (id, org_id, tool_id, version_no, change_type, change_desc, changed_by, changed_at, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','V1.0','DESIGN','初始设计发布,用于 V5/V8 电极活化','MZ-研发工程师','2026-03-01', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tool_version WHERE tool_id='019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43' AND version_no='V1.0');

INSERT INTO ops.tlm_tool_version (id, org_id, tool_id, version_no, change_type, change_desc, changed_by, changed_at, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','V1.1','IMPROVE','优化活化槽温控精度,提升电极一致性','MZ-研发工程师','2026-06-15', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tool_version WHERE tool_id='019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43' AND version_no='V1.1');

-- ===== 5) 维修历史(tlm_repair) =====
INSERT INTO ops.tlm_repair (id, org_id, tool_id, repair_no, fault_desc, measure, status, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','RP-DEMO-20260011-1','活化温度波动超差','校准温控模块,更换温度传感器','DONE', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_repair WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND repair_no='RP-DEMO-20260011-1');

INSERT INTO ops.tlm_repair (id, org_id, tool_id, repair_no, fault_desc, measure, status, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43','RP-DEMO-20260011-2','卡槽夹具有松动','紧固并加防松垫圈,点检通过','DONE', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_repair WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND repair_no='RP-DEMO-20260011-2');
