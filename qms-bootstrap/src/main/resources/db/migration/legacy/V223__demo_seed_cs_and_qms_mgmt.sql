-- V223: 演示数据(seed) —— 售后(CS) 与 体系运行监控(qms-mgmt)。
-- 目的: 让四模块(工装/计量/体系/售后)全流程跑通、页面有可读数据,供操作指南编写。
-- 说明:
--   1) 工装(TLM)/计量(Metro) 已由 V169 灌入 27 条台账,本脚本不重复插入。
--   2) 本脚本只补充 售后(CS) 与 体系(qms-mgmt) 的演示数据,全部幂等(NOT EXISTS)。
--   3) org 用子查询 (SELECT id FROM ops.sys_org WHERE org_code='MZ'),负责人用 sys_user.real_name 反查。
--   4) 不使用 DO $$ 匿名块,逐条独立 INSERT,避免解析歧义。
--   5) 业务唯一键: cs_work_order(order_no)、qms_internal_audit(audit_no)、
--      qms_audit_nc(nc_no)、qms_adverse_event(event_no)、qms_quality_goal(goal_name+period)。

-- ===== 一、售后 CS: 工单(cs_work_order) 覆盖状态机 + INSTALL/REPAIR =====
INSERT INTO ops.cs_work_order (id, org_id, order_no, customer_name, customer_contact, wo_type, priority,
     product_name, fault_desc, status, owner_id, owner_name, assign_at, handle_detail, handle_at,
     close_at, satisfaction, satisfaction_comment, expect_time, address, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'WO-DEMO-001','康立医疗器械有限公司','13800000001','INSTALL','NORMAL','V5血气电解质分析仪',
     '新装机现场安装与调试','CLOSED',
     (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name='MZ-SQE' AND o.org_code='MZ' LIMIT 1),
     'MZ-SQE','2026-08-01 09:00','已完成安装调试,设备运行正常,客户签字确认','2026-08-03 16:00','2026-08-03 16:30',
     5,'安装规范,响应及时','2026-08-10 00:00','广东省梅州市梅江区', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_work_order WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND order_no='WO-DEMO-001');

INSERT INTO ops.cs_work_order (id, org_id, order_no, customer_name, customer_contact, wo_type, priority,
     product_name, fault_desc, status, owner_id, owner_name, assign_at,
     satisfaction, satisfaction_comment, expect_time, address, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'WO-DEMO-002','梅州人民医院','13900000002','REPAIR','URGENT','V8电极膜裁切工具',
     '裁切尺寸偏差超差','ASSIGNED',
     (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name='MZ-质量工程师' AND o.org_code='MZ' LIMIT 1),
     'MZ-质量工程师','2026-08-12 10:00',
     4,'处理较快','2026-08-15 00:00','广东省梅州市', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_work_order WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND order_no='WO-DEMO-002');

INSERT INTO ops.cs_work_order (id, org_id, order_no, customer_name, customer_contact, wo_type, priority,
     product_name, fault_desc, status, owner_id, owner_name, assign_at, handle_detail, handle_at,
     satisfaction, satisfaction_comment, expect_time, address, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'WO-DEMO-003','深圳汇兴源科技','13700000003','REPAIR','NORMAL','1000L不锈钢反应釜',
     '搅拌异响','DONE',
     (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name='梅州操作员' AND o.org_code='MZ' LIMIT 1),
     '梅州操作员','2026-08-05 14:00','更换轴承后异响消除','2026-08-07 11:00',
     3,'维修尚可','2026-08-12 00:00','广东省深圳市', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_work_order WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND order_no='WO-DEMO-003');

INSERT INTO ops.cs_work_order (id, org_id, order_no, customer_name, customer_contact, wo_type, priority,
     product_name, fault_desc, status, expect_time, address, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'WO-DEMO-004','广州某三甲医院','13600000004','INSTALL','LOW','多通道GL电极检验工装',
     '新院区设备装机','PENDING','2026-08-20 00:00','广东省广州市', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_work_order WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND order_no='WO-DEMO-004');

INSERT INTO ops.cs_work_order (id, org_id, order_no, customer_name, customer_contact, wo_type, priority,
     product_name, fault_desc, status, expect_time, address, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'WO-DEMO-005','东莞某检验所','13500000005','REPAIR','NORMAL','绝缘电阻测试仪',
     '读数漂移','PENDING','2026-08-18 00:00','广东省东莞市', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_work_order WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND order_no='WO-DEMO-005');

-- ===== 二、售后 CS: 客户反馈(cs_feedback) 投诉/建议/表扬/咨询 =====
INSERT INTO ops.cs_feedback (id, org_id, customer_name, customer_contact, fb_type, content,
     related_wo_no, status, handle_detail, handle_at, owner_name, satisfaction, cause, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '康立医疗器械有限公司','13800000001','COMPLAINT','设备到货后安装等待时间过长,影响上线进度',
     'WO-DEMO-001','DONE','已协调工程师优先排期,并补偿现场培训一次','2026-08-04 10:00','MZ-SQE',4,'RESPONSE_SLOW', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_feedback WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND content='设备到货后安装等待时间过长,影响上线进度');

INSERT INTO ops.cs_feedback (id, org_id, customer_name, customer_contact, fb_type, content, status, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '梅州人民医院','13900000002','SUGGESTION','建议增加裁切工具的备用刀头配置,减少停机','OPEN', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_feedback WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND content='建议增加裁切工具的备用刀头配置,减少停机');

INSERT INTO ops.cs_feedback (id, org_id, customer_name, customer_contact, fb_type, content,
     related_wo_no, status, handle_detail, handle_at, owner_name, satisfaction, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '深圳汇兴源科技','13700000003','PRAISE','维修响应快,工程师专业,问题一次解决',
     'WO-DEMO-003','DONE','感谢反馈,已记录至服务改进','2026-08-08 09:00','梅州操作员',5, now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_feedback WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND content='维修响应快,工程师专业,问题一次解决');

INSERT INTO ops.cs_feedback (id, org_id, customer_name, customer_contact, fb_type, content,
     status, handle_detail, handle_at, owner_name, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '广州某三甲医院','13600000004','INQUIRY','咨询新院区装机是否需要提前准备气源管路',
     'HANDLING','已答复需提前预留压缩空气接口','2026-08-13 15:00','MZ-SQE', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.cs_feedback WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND content='咨询新院区装机是否需要提前准备气源管路');

-- ===== 三、体系: 质量目标(qms_quality_goal) =====
INSERT INTO ops.qms_quality_goal (id, org_id, goal_name, goal_type, period, target_value, actual_value, unit, owner, deadline, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '来料合格率','QUALITY','2026Q3',99.00,99.87,'%','MZ-SQE','2026-09-30 00:00','月度统计 IQC 合格批次/总批次', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_quality_goal WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND goal_name='来料合格率' AND period='2026Q3');

INSERT INTO ops.qms_quality_goal (id, org_id, goal_name, goal_type, period, target_value, actual_value, unit, owner, deadline, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '成品一次交验合格率','QUALITY','2026Q3',98.00,97.50,'%','MZ-质量工程师','2026-09-30 00:00','终检一次合格', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_quality_goal WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND goal_name='成品一次交验合格率' AND period='2026Q3');

INSERT INTO ops.qms_quality_goal (id, org_id, goal_name, goal_type, period, target_value, actual_value, unit, owner, deadline, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '交付及时率','DELIVERY','2026Q3',95.00,96.30,'%','MZ-SQE','2026-09-30 00:00','按合同交期达成', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_quality_goal WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND goal_name='交付及时率' AND period='2026Q3');

INSERT INTO ops.qms_quality_goal (id, org_id, goal_name, goal_type, period, target_value, actual_value, unit, owner, deadline, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '客户满意度','SATISFACTION','2026Q3',90.00,92.00,'分','MZ-SQE','2026-09-30 00:00','售后回访打分均值', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_quality_goal WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND goal_name='客户满意度' AND period='2026Q3');

INSERT INTO ops.qms_quality_goal (id, org_id, goal_name, goal_type, period, target_value, actual_value, unit, owner, deadline, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     '不良成本率','COST','2026Q3',2.00,1.85,'%','MZ-质量工程师','2026-09-30 00:00','不良损失/产值', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_quality_goal WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND goal_name='不良成本率' AND period='2026Q3');

-- ===== 四、体系: 内部审核(qms_internal_audit) =====
INSERT INTO ops.qms_internal_audit (id, org_id, audit_no, audit_name, audit_scope, plan_date, auditor, status, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'IA-DEMO-2026-01','2026 年度质量管理体系内审','生产/检验/仓储','2026-09-10 00:00','MZ-SQE','DONE','覆盖 ISO13485 全部条款,已出报告', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_internal_audit WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND audit_no='IA-DEMO-2026-01');

INSERT INTO ops.qms_internal_audit (id, org_id, audit_no, audit_name, audit_scope, plan_date, auditor, status, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'IA-DEMO-2026-02','供应商管理专项审核','采购/供应商管理','2026-08-20 00:00','MZ-质量工程师','ONGOING','现场审核进行中', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_internal_audit WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND audit_no='IA-DEMO-2026-02');

-- ===== 五、体系: 不符合项(qms_audit_nc) 关联内审 =====
INSERT INTO ops.qms_audit_nc (id, org_id, audit_id, nc_no, nc_desc, clause, severity, status,
     owner, due_date, corrective, verify_result, closed_at, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     (SELECT id FROM ops.qms_internal_audit WHERE audit_no='IA-DEMO-2026-01' AND org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ')),
     'NC-DEMO-001','检验记录填写不完整,部分批次缺复核人签字','8.2.4','MAJOR','IN_PROGRESS',
     'MZ-检验员','2026-09-30 00:00','已修订记录模板并组织培训','待验证',NULL, now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_audit_nc WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND nc_no='NC-DEMO-001');

INSERT INTO ops.qms_audit_nc (id, org_id, audit_id, nc_no, nc_desc, clause, severity, status,
     owner, due_date, corrective, verify_result, closed_at, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     (SELECT id FROM ops.qms_internal_audit WHERE audit_no='IA-DEMO-2026-01' AND org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ')),
     'NC-DEMO-002','仓库温湿度记录间隔超规定','7.5.1','MINOR','CLOSED',
     'MZ-SQE','2026-08-25 00:00','增设自动记录仪,取消人工抄表','验证通过','2026-08-26 00:00', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_audit_nc WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND nc_no='NC-DEMO-002');

-- ===== 六、体系: 不良事件(qms_adverse_event) =====
INSERT INTO ops.qms_adverse_event (id, org_id, event_no, event_type, occur_stage, severity,
     occur_at, report_at, root_cause, handle_desc, handle_timeliness, status, owner, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'AE-DEMO-001','投诉','使用环节','SERIOUS','2026-07-28 00:00','2026-07-29 00:00',
     '电极膜裁切尺寸偏差导致测试结果波动','已召回同批产品并复检,更换工装刀头','TIMELY','DONE','MZ-SQE','已闭环并纳入 8D 整改', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_adverse_event WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND event_no='AE-DEMO-001');

INSERT INTO ops.qms_adverse_event (id, org_id, event_no, event_type, occur_stage, severity,
     occur_at, report_at, root_cause, handle_desc, handle_timeliness, status, owner, remark, created_at)
SELECT gen_random_uuid(), (SELECT id FROM ops.sys_org WHERE org_code='MZ'),
     'AE-DEMO-002','不良事件','生产环节','GENERAL','2026-08-10 00:00','2026-08-11 00:00',
     '反应釜搅拌异响','紧固轴承后恢复,纳入预防维护计划','TIMELY','HANDLING','MZ-质量工程师','处理中', now()
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_adverse_event WHERE org_id=(SELECT id FROM ops.sys_org WHERE org_code='MZ') AND event_no='AE-DEMO-002');
