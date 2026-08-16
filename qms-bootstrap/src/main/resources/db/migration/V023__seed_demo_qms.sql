-- ============================================================================
-- 种子数据 - 演示数据-体系运行监控(可选) (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : qms_internal_audit, qms_audit_nc, qms_adverse_event, qms_quality_goal
-- 性质 : 演示数据, 交付生产可省略 [可选, 可关闭]
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
-- Data for Name: qms_internal_audit; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_internal_audit VALUES ('ba9ccbeb-ba0b-fecc-8bae-b8727387977b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-1786778552478', '年度内审测试', '', NULL, '', 'PLANNED', '', '2026-08-15 15:22:32.479362', '2026-08-15 15:30:31.434093', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0);
INSERT INTO ops.qms_internal_audit VALUES ('622c6003-3c6e-d402-3fee-10e49e71d142', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-1786780236443', 'SM测试', NULL, NULL, NULL, 'ONGOING', NULL, '2026-08-15 23:50:36.444677', '2026-08-15 15:50:36.64016', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 1);
INSERT INTO ops.qms_internal_audit VALUES ('3296c0d0-b3c4-478d-420c-dd55ed05073d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-1786780255971', 'SM2', NULL, NULL, NULL, 'PLANNED', NULL, '2026-08-15 15:50:55.972387', '2026-08-15 15:50:56.236884', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0);
INSERT INTO ops.qms_internal_audit VALUES ('533a3fc5-92c2-f292-089d-c4bff8388b91', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-1786780283634', 'SM3', NULL, NULL, NULL, 'PLANNED', NULL, '2026-08-15 15:51:23.635064', '2026-08-15 15:51:23.710097', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0);
INSERT INTO ops.qms_internal_audit VALUES ('bc1daa98-6c63-4c60-a335-bcffa72bb395', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-DEMO-2026-01', '2026 年度质量管理体系内审', '生产/检验/仓储', '2026-09-10 00:00:00', 'MZ-SQE', 'DONE', '覆盖 ISO13485 全部条款,已出报告', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_internal_audit VALUES ('7454caef-f03b-4234-a60a-8b7e083a6f83', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'IA-DEMO-2026-02', '供应商管理专项审核', '采购/供应商管理', '2026-08-20 00:00:00', 'MZ-质量工程师', 'ONGOING', '现场审核进行中', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: qms_audit_nc; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_audit_nc VALUES ('f83f94ea-84e3-edea-57b2-9a3b22ed375d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'ba9ccbeb-ba0b-fecc-8bae-b8727387977b', 'NC-1786778883039', '现场未按要求填写检验记录，追溯信息缺失', '', 'MINOR', 'OPEN', '', NULL, '', '', NULL, '2026-08-15 15:28:03.041662', '2026-08-15 15:30:23.149001', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0);
INSERT INTO ops.qms_audit_nc VALUES ('72f9132d-3097-468d-aa3e-38a015959928', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'bc1daa98-6c63-4c60-a335-bcffa72bb395', 'NC-DEMO-001', '检验记录填写不完整,部分批次缺复核人签字', '8.2.4', 'MAJOR', 'IN_PROGRESS', 'MZ-检验员', '2026-09-30 00:00:00', '已修订记录模板并组织培训', '待验证', NULL, '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_audit_nc VALUES ('440c960b-51b0-4f60-bfe8-85f417f5c894', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'bc1daa98-6c63-4c60-a335-bcffa72bb395', 'NC-DEMO-002', '仓库温湿度记录间隔超规定', '7.5.1', 'MINOR', 'CLOSED', 'MZ-SQE', '2026-08-25 00:00:00', '增设自动记录仪,取消人工抄表', '验证通过', '2026-08-26 00:00:00', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: qms_adverse_event; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_adverse_event VALUES ('0ef2097c-558c-1b42-3946-5c0ff27a386d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'AE-1786778463197', '投诉', '使用', 'GENERAL', NULL, NULL, '', '', '及时', 'HANDLING', '', '2026-08-15 23:21:03.197838', '2026-08-15 15:22:04.399074', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 1, NULL);
INSERT INTO ops.qms_adverse_event VALUES ('a256fa83-eb5d-43e4-afa1-e6295dc661f0', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'AE-DEMO-001', '投诉', '使用环节', 'SERIOUS', '2026-07-28 00:00:00', '2026-07-29 00:00:00', '电极膜裁切尺寸偏差导致测试结果波动', '已召回同批产品并复检,更换工装刀头', 'TIMELY', 'DONE', '已闭环并纳入 8D 整改', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, 'MZ-SQE');
INSERT INTO ops.qms_adverse_event VALUES ('1a664adb-2d04-4397-ae83-8d3b9bb907b3', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'AE-DEMO-002', '不良事件', '生产环节', 'GENERAL', '2026-08-10 00:00:00', '2026-08-11 00:00:00', '反应釜搅拌异响', '紧固轴承后恢复,纳入预防维护计划', 'TIMELY', 'HANDLING', '处理中', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, 'MZ-质量工程师');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: qms_quality_goal; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.qms_quality_goal VALUES ('58be0fe0-a50e-499b-8308-347889358807', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '来料合格率', 'QUALITY', '2026Q3', 99.0000, 99.8700, '%', 'MZ-SQE', '2026-09-30 00:00:00', '月度统计 IQC 合格批次/总批次', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_quality_goal VALUES ('bba930e8-9e19-42dc-bf31-50511a0ca9e9', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '成品一次交验合格率', 'QUALITY', '2026Q3', 98.0000, 97.5000, '%', 'MZ-质量工程师', '2026-09-30 00:00:00', '终检一次合格', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_quality_goal VALUES ('adb237da-8486-4341-99b3-e93392c29fd8', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '交付及时率', 'DELIVERY', '2026Q3', 95.0000, 96.3000, '%', 'MZ-SQE', '2026-09-30 00:00:00', '按合同交期达成', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_quality_goal VALUES ('74295df1-a09d-4c25-9f6a-53074b5203f7', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '客户满意度', 'SATISFACTION', '2026Q3', 90.0000, 92.0000, '分', 'MZ-SQE', '2026-09-30 00:00:00', '售后回访打分均值', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.qms_quality_goal VALUES ('c6d86d4a-2be9-48e3-bb01-85d31c3b5944', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '不良成本率', 'COST', '2026Q3', 2.0000, 1.8500, '%', 'MZ-质量工程师', '2026-09-30 00:00:00', '不良损失/产值', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);


--
-- PostgreSQL database dump complete
--



