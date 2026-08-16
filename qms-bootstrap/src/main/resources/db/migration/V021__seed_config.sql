-- ============================================================================
-- 种子数据 - 模块配置与规则种子 (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : qms_8d_approval_config, qms_8d_stage_config, sqm_audit_approval_cfg, sqm_perf_metric_cfg, sqm_supplier_grade_rule, spc_rule, sqm_abnormal_rule, sqm_audit_checklist_item, sqm_audit_freq_rule, sqm_change_risk_rule
-- 性质 : 业务配置/基础数据, 需随库初始化
-- 重跑 : 普通 INSERT, 全新库直接执行; 已有库先清对应表再执行。
-- 说明 : 开头禁用 FK 即时检查(session_replication_role=replica), 解决自引用
--        (如 sys_menu 父子菜单) 的 INSERT 顺序问题, 末尾恢复。
-- 生成 : pg_dump --data-only --inserts 逐表导出。
-- ============================================================================
SET session_replication_role = replica;

--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: qms_8d_approval_config; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545e-7409-be7b-30c47a8efa4e', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'D3', true, NULL, 3);
INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545e-7280-b1c1-5282543f2658', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'D5', true, NULL, 5);
INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545e-7bb3-bc8e-c07cd5b820b6', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'D7', true, NULL, 7);
INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545f-72f7-bdfc-0fa9972fea69', '019fd5dc-5198-7184-a384-fc3c578bf11e', 'D3', true, NULL, 3);
INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545f-73f6-802b-9e9beb199c4b', '019fd5dc-5198-7184-a384-fc3c578bf11e', 'D5', true, NULL, 5);
INSERT INTO ops.qms_8d_approval_config VALUES ('019fd5dc-545f-765b-ba3c-7281996fbfe2', '019fd5dc-5198-7184-a384-fc3c578bf11e', 'D7', true, NULL, 7);
INSERT INTO ops.qms_8d_approval_config VALUES ('51647b1d-68ae-6d61-2c47-420babc73710', 'ROOT', 'D1', false, NULL, 1);
INSERT INTO ops.qms_8d_approval_config VALUES ('d07d96fc-f6ca-acb4-ec9f-79af86b8da9c', 'ROOT', 'D2', false, NULL, 2);
INSERT INTO ops.qms_8d_approval_config VALUES ('81fbd3a9-1023-28b8-ec50-e2711b9cce02', 'ROOT', 'D3', true, NULL, 3);
INSERT INTO ops.qms_8d_approval_config VALUES ('82c8bee0-4540-b537-e20d-92ade8c5cc11', 'ROOT', 'D4', false, NULL, 4);
INSERT INTO ops.qms_8d_approval_config VALUES ('5f45a930-f88c-2a20-55fe-2c1ee8e62783', 'ROOT', 'D5', true, NULL, 5);
INSERT INTO ops.qms_8d_approval_config VALUES ('66fa5106-dd9a-bd1a-1c3c-cd5ef0bcdf2c', 'ROOT', 'D6', false, NULL, 6);
INSERT INTO ops.qms_8d_approval_config VALUES ('60e20041-cfa2-b481-ab65-f069c3af0050', 'ROOT', 'D7', true, NULL, 7);
INSERT INTO ops.qms_8d_approval_config VALUES ('5f7e5509-ce72-9a42-dc52-0c98a9e37dbf', 'ROOT', 'D8', false, NULL, 8);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: qms_8d_stage_config; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-7cc4-b8a2-7fb66c6f788b', NULL, 'D1', '团队组建', 1, false, NULL, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-7180-9914-56aefc2d0eb9', NULL, 'D2', '问题描述(5W2H)', 2, false, NULL, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-7138-90c1-9e7b279e3218', NULL, 'D3', '临时措施', 3, true, '24:00:00', '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-76bd-b38c-bdd3b0f4bd50', NULL, 'D4', '根因分析', 4, false, '7 days', '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-75a0-9adb-fc7e9e4ec7f1', NULL, 'D5', '永久纠正措施', 5, true, '7 days', '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-7d09-ae05-863d7eb09604', NULL, 'D6', '实施与验证', 6, false, '30 days', '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-73df-94ae-534465a9b1d0', NULL, 'D7', '预防再发', 7, true, '30 days', '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.qms_8d_stage_config VALUES ('019fd5dc-50be-7f72-886a-395ab74fdc0f', NULL, 'D8', '闭环归档', 8, false, NULL, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_audit_approval_cfg; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000004', NULL, '季度审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000005', NULL, '来料异常审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000007', NULL, '重大来料异常审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000008', NULL, '供应商准入审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000009', NULL, '年度复审', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000a', NULL, '过程审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000b', NULL, '专项审核', '[{"role":"zhiliang","label":"质量","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000c', NULL, '飞行检查', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000d', NULL, '初次审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"caigou","label":"采购","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000e', NULL, '附加审核', '[{"role":"zhiliang","label":"质量","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-00000000000f', NULL, '重新审核', '[{"role":"zhiliang","label":"质量","veto":false},{"role":"sqe","label":"SQE","veto":false}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000003', NULL, '年度审核', '[{"role":"admin","label":"MZ-质量经理","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f"},{"role":"admin","label":"MZ-采购员","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f"}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 9);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('42e50205-4408-e79d-7088-229d95ba7327', NULL, '??????', '[{"role":"admin","label":"?????","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f"},{"role":"admin","label":"?????","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f"}]', '2026-08-08 18:07:10.403541+08', '2026-08-08 18:07:10.403541+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 1);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000006', NULL, '临时审核', '[{"role":"zhiliang","label":"质量","veto":false,"userId":""}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 1);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('190b74c2-2b1d-3082-80c2-e5e00c4b27e6', NULL, '工装报废审核', '[{"role":"zhiliang","label":"MZ-质量经理","veto":false,"userId":"a9d66060-9fa3-d966-d945-9a3b1337b2fc","userIds":["a9d66060-9fa3-d966-d945-9a3b1337b2fc"]}]', '2026-08-14 21:58:22.270829+08', '2026-08-14 21:58:22.270829+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('33b23d12-23c5-cefa-d569-1b44fff0c16f', NULL, '供应商审核', '[{"role":"mzsqe","label":"MZ-SQE、MZ-质量经理","veto":false,"userId":"118db5e2-3f9e-b0fc-17b1-40a7be92aa33,a9d66060-9fa3-d966-d945-9a3b1337b2fc","userIds":["118db5e2-3f9e-b0fc-17b1-40a7be92aa33","a9d66060-9fa3-d966-d945-9a3b1337b2fc"]}]', '2026-08-13 19:45:47.745936+08', '2026-08-13 19:45:47.745936+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('01a0011b-e19a-781a-8379-e74846adfd4e', NULL, '工装维修审核', '[{"role":"zhiliang","label":"MZ-质量经理","veto":false,"userId":"a9d66060-9fa3-d966-d945-9a3b1337b2fc","userIds":["a9d66060-9fa3-d966-d945-9a3b1337b2fc"]}]', '2026-08-15 00:29:59.561964+08', '2026-08-15 00:29:59.561964+08', NULL, NULL, false, 0);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000001', NULL, '物料变更审核', '[{"role":"admin","label":"集团管理员","veto":true,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f","userIds":["bf33d3bd-902c-1d3d-406c-4b5939483b8f"]},{"role":"admin","label":"集团管理员","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f","userIds":["bf33d3bd-902c-1d3d-406c-4b5939483b8f"]},{"role":"admin","label":"集团管理员","veto":false,"userId":"bf33d3bd-902c-1d3d-406c-4b5939483b8f","userIds":["bf33d3bd-902c-1d3d-406c-4b5939483b8f"]}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 13);
INSERT INTO ops.sqm_audit_approval_cfg VALUES ('f0000000-0000-0000-0000-000000000002', NULL, '资质审核', '[{"role":"zhiliang","label":"质量","veto":false,"userId":"","userIds":[]}]', '2026-08-06 14:56:54.232245+08', '2026-08-06 14:56:54.232245+08', NULL, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 3);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_perf_metric_cfg; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_perf_metric_cfg VALUES ('019ff87b-1546-728b-9c46-58440a7551f2', NULL, 'INCOMING_PASS', '来料合格率', 30.00, 98.00, 99.50, true, false, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_perf_metric_cfg VALUES ('019ff87b-1549-75e8-8eb1-f955b2cc7133', NULL, 'DELIVERY', '交付及时率', 30.00, 95.00, 99.00, true, false, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_perf_metric_cfg VALUES ('019ff87b-154a-7f5e-876f-783fcedccb8c', NULL, 'QUALITY', '质量分(SPC过程能力)', 40.00, 90.00, 95.00, true, false, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_perf_metric_cfg VALUES ('019ff87b-154b-73e5-8bc7-0ac8ade33aeb', NULL, 'RECTIFY', '整改及时率', 0.00, 90.00, 98.00, false, false, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_perf_metric_cfg VALUES ('019ff87b-154c-7049-a0e9-99e0abb40426', NULL, 'COMPLIANCE', '合规率', 0.00, 95.00, 99.00, false, false, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_supplier_grade_rule; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_supplier_grade_rule VALUES ('019fd63c-49a5-7515-a8b6-61b4d22d53ba', NULL, 90.00, 101.00, 'A', false);
INSERT INTO ops.sqm_supplier_grade_rule VALUES ('019fd63c-49aa-76b1-bae5-a1eef1c215d7', NULL, 80.00, 90.00, 'B', false);
INSERT INTO ops.sqm_supplier_grade_rule VALUES ('019fd63c-49ab-70fe-b479-3a2691d29bce', NULL, 70.00, 80.00, 'C', true);
INSERT INTO ops.sqm_supplier_grade_rule VALUES ('019fd63c-49ab-7963-895c-131af1010c0b', NULL, 0.00, 70.00, 'D', true);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: spc_rule; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-7904-b1ea-0fd78faa990d', NULL, '①', '1点超出3σ', '报警', true, 1);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-757e-add7-d962cca194ef', NULL, '②', '连续3点中2点在A区(同侧2σ外)', '报警', true, 2);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-7271-8c68-f4173f84d643', NULL, '③', '连续5点中4点在B区外(1σ外)', '预警', true, 3);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-72db-9192-c11c5f0a169b', NULL, '⑤', '连续6点递增或递减', '预警', true, 5);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-7706-ad77-3d881efdc30f', NULL, '⑥', '连续14点交替上下', '预警', true, 6);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-782e-82b6-4c9eb12dc79d', NULL, '⑦', '连续15点在C区(1σ内)', '预警', true, 7);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-7d9a-9249-7f108ccfed46', NULL, '⑧', '连续8点在B区外(1σ外)', '预警', true, 8);
INSERT INTO ops.spc_rule VALUES ('019fd5dc-50bc-7e6b-9a8e-9518eb423075', NULL, '④', '连续8点在中心线一侧', '预警', true, 4);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_abnormal_rule; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_abnormal_rule VALUES ('019fd657-e0e1-7ee6-9b00-2fb9d93a5686', NULL, 3, 30, 3, 'default', '2026-08-06 17:20:57.903121+08');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_audit_checklist_item; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_audit_checklist_item VALUES ('c70b91bf-9fea-0352-e007-f9222824956d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'c5eac29e-d27c-da7b-eca8-54ac55c512c5', 1, '7.1', 'file control', '合格', 'checked', NULL, '2026-08-13 20:40:24.352973+08', '2026-08-13 20:40:24.352973+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('30322b39-67eb-4a97-a587-3ec158b0a4dd', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'c5eac29e-d27c-da7b-eca8-54ac55c512c5', 2, '8.2', 'training', '不合格', 'no record', NULL, '2026-08-13 20:40:24.358572+08', '2026-08-13 20:40:24.358572+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('a5ac313f-00b0-b12e-a671-d090bebf6d5d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '9cf5fc71-3589-1aef-1a32-979606ef8e88', 1, '7.1', 'file control', '合格', 'checked', NULL, '2026-08-13 20:44:04.707232+08', '2026-08-13 20:44:04.707232+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('439a46a8-e2a8-d741-5b33-ea132e891375', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '9cf5fc71-3589-1aef-1a32-979606ef8e88', 2, '8.2', 'training', '不合格', 'no record', NULL, '2026-08-13 20:44:04.711927+08', '2026-08-13 20:44:04.711927+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('91cf5fbd-8c16-f236-039c-71cbb0981546', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '2882a372-4b89-305b-f9c1-f39a18eac0b1', 1, '7.1', '文件控制程序', '符合', '已查阅质量手册', NULL, '2026-08-13 20:51:49.353883+08', '2026-08-13 20:51:49.353883+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('988200c1-f52d-b41e-cae3-8048add48d15', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'f01d75a0-2fe9-2ac1-5cdf-b198de9efda9', 1, '7.1', '测试', '符合', '123', NULL, '2026-08-13 20:58:11.904069+08', '2026-08-13 20:58:11.904069+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('6634174e-3f38-3b25-7c4c-f332ee022b29', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'ba9870ab-28fc-65f2-e3d7-11e135d69689', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:17:54.868206+08', '2026-08-13 21:17:54.868206+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('69592f78-19c2-e595-239e-4c2bb141e5a1', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'e1a2e461-e191-a84e-4584-72575b5bbc64', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:17:57.933884+08', '2026-08-13 21:17:57.933884+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('a2e3ffb6-db5c-5277-6a98-9f0be32f0fd1', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'a54fbbaf-d268-09ce-5142-942803afecb2', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:19:42.100005+08', '2026-08-13 21:19:42.100005+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('8da498e2-f2d4-d51f-9b52-6e0654280018', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '35a008b2-174d-110a-ec96-39a1f09a45e9', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:19:45.106834+08', '2026-08-13 21:19:45.106834+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('d50b1d07-7e3b-46d6-8f09-2dd79f5175e1', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '80c82629-be9f-de08-a45b-d873a350e157', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:20:15.171616+08', '2026-08-13 21:20:15.171616+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('52a43059-01d0-6eff-7f5e-48a6924f9c7c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '05d5f8ea-048d-9e4e-4e52-099d610854ac', 1, '7.1', 'file control', '符合', NULL, NULL, '2026-08-13 21:20:16.660246+08', '2026-08-13 21:20:16.660246+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('b87a84a1-21b2-fcd2-2e49-edc3254c8384', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '07734783-ab1b-a02d-b1ad-713ec150fabf', 1, '7.1', 'file', '符合', NULL, NULL, '2026-08-13 21:20:17.482263+08', '2026-08-13 21:20:17.482263+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.sqm_audit_checklist_item VALUES ('6a3d9bdb-868e-ed2a-8aa0-287b88328349', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'ab3c50ca-d26c-66b9-4a80-2f0a65646c39', 1, '1', '1', '符合', '1', NULL, '2026-08-13 21:22:49.532361+08', '2026-08-13 21:22:49.532361+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_audit_freq_rule; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sqm_audit_freq_rule VALUES ('019ff87b-167c-7502-9e73-99b81d21a522', NULL, '高', 'D', 4, '现场审核', '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_audit_freq_rule VALUES ('019ff87b-167e-7377-ae6d-290196d79d73', NULL, '中', 'C', 2, '现场审核', '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_audit_freq_rule VALUES ('019ff87b-1680-721f-9b5b-fb596ee976e1', NULL, '低', 'B', 1, '文件审核', '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sqm_audit_freq_rule VALUES ('019ff87b-1680-7837-a92d-a0060db95fd4', NULL, '极低', 'A', 1, '文件审核', '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sqm_change_risk_rule; Type: TABLE DATA; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



