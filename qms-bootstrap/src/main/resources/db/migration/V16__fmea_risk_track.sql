-- 4.6 FMEA 风险跟踪：演示种子数据（qms_fmea_risk_track 表由 V02 创建）
-- 表 ops.qms_fmea_risk_track 列：id, org_id, risk_id, from_status, to_status,
--   operator, operate_time, action_note, evidence, esign_id

-- 确保演示所需基础组织存在:应用层 SeedRunner 在 Flyway 之后才写入 sys_org,
-- 本脚本依赖 org_code='MZ',故此处先行 seed,避免 V16 执行早于 SeedRunner 导致 org_id 为 NULL
INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status)
SELECT 'MZ', '梅州分公司', 1, '公司', '启用'
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_org WHERE org_code='MZ');
INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status)
SELECT 'SZ', '深圳分公司', 2, '公司', '启用'
WHERE NOT EXISTS (SELECT 1 FROM ops.sys_org WHERE org_code='SZ');

-- 演示风险项（仅当不存在时插入，使用固定 UUID 便于轨迹关联）
INSERT INTO ops.qms_fmea_risk (id, org_id, risk_no, fmea_type, product, process, failure_mode,
                               severity_s, occurrence_o, detection_d, rpn, risk_level, high_risk_flag,
                               status, action, owner, target_date, created_at, updated_at, is_deleted)
SELECT '11111111-1111-1111-1111-111111111101',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       'FMEA-DEMO-001', 'PFMEA', '储能电芯', '电芯叠片', '极片对齐偏移导致内部短路',
       9, 4, 5, 180, '高', true,
       '待闭环', '增加叠片CCD在线检测工位，偏移超差自动剔除', '张工', '2026-08-15',
       now(), now(), false
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk WHERE id='11111111-1111-1111-1111-111111111101');

INSERT INTO ops.qms_fmea_risk (id, org_id, risk_no, fmea_type, product, process, failure_mode,
                               severity_s, occurrence_o, detection_d, rpn, risk_level, high_risk_flag,
                               status, action, owner, target_date, created_at, updated_at, is_deleted)
SELECT '11111111-1111-1111-1111-111111111102',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       'FMEA-DEMO-002', 'DFMEA', '储能电芯', '结构设计', '壳体密封不良导致进水',
       7, 3, 4, 84, '中', false,
       '进行中', '优化密封圈结构，提升IP67等级', '李工', '2026-09-01',
       now(), now(), false
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk WHERE id='11111111-1111-1111-1111-111111111102');

INSERT INTO ops.qms_fmea_risk (id, org_id, risk_no, fmea_type, product, process, failure_mode,
                               severity_s, occurrence_o, detection_d, rpn, risk_level, high_risk_flag,
                               status, action, owner, target_date, close_date, created_at, updated_at, is_deleted)
SELECT '11111111-1111-1111-1111-111111111103',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       'FMEA-DEMO-003', 'PFMEA', 'PACK模组', '激光焊接', '焊缝气孔导致强度不足',
       8, 5, 3, 120, '高', true,
       '已闭环', '导入焊接参数DOE与X-ray全检', '王工', '2026-06-30', '2026-07-10',
       now(), now(), false
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk WHERE id='11111111-1111-1111-1111-111111111103');

-- 演示：已闭环项(103)的闭环轨迹（识别 → 措施分配 → 措施验证 → 闭环）
INSERT INTO ops.qms_fmea_risk_track (id, org_id, risk_id, from_status, to_status, operator, operate_time, action_note, evidence)
SELECT '22222222-2222-2222-2222-222222222201',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       '11111111-1111-1111-1111-111111111103', NULL, '待闭环', '系统', now()-interval '40 day',
       '创建风险项(高风险); RPN=120,S=8', NULL
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk_track WHERE id='22222222-2222-2222-2222-222222222201');

INSERT INTO ops.qms_fmea_risk_track (id, org_id, risk_id, from_status, to_status, operator, operate_time, action_note, evidence)
SELECT '22222222-2222-2222-2222-222222222202',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       '11111111-1111-1111-1111-111111111103', '待闭环', '进行中', '王工', now()-interval '35 day',
       '更新纠正措施/责任人/目标日期; 措施=导入焊接参数DOE与X-ray全检', NULL
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk_track WHERE id='22222222-2222-2222-2222-222222222202');

INSERT INTO ops.qms_fmea_risk_track (id, org_id, risk_id, from_status, to_status, operator, operate_time, action_note, evidence)
SELECT '22222222-2222-2222-2222-222222222203',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       '11111111-1111-1111-1111-111111111103', '进行中', '进行中', '王工', now()-interval '10 day',
       '提交措施验证证据; DOE报告+X-ray全检记录', 'DOE报告+X-ray全检记录'
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk_track WHERE id='22222222-2222-2222-2222-222222222203');

INSERT INTO ops.qms_fmea_risk_track (id, org_id, risk_id, from_status, to_status, operator, operate_time, action_note, evidence)
SELECT '22222222-2222-2222-2222-222222222204',
       (SELECT id FROM ops.sys_org WHERE org_code='MZ' LIMIT 1),
       '11111111-1111-1111-1111-111111111103', '进行中', '已闭环', '王工', now()-interval '9 day',
       '通过闭环确认; 证据完整且3个月无复发', 'DOE报告+X-ray全检记录'
WHERE NOT EXISTS (SELECT 1 FROM ops.qms_fmea_risk_track WHERE id='22222222-2222-2222-2222-222222222204');
