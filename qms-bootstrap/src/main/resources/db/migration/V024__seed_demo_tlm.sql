-- ============================================================================
-- 种子数据 - 演示数据-工装/计量(可选) (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : tlm_tooling, tlm_metro_record, tlm_repair, tlm_scrap, tlm_calib_plan, tlm_maint_plan, tlm_tool_product, tlm_tool_version, tlm_tool_wo_bind
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
-- Data for Name: tlm_tooling; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5aa-7ce9-823e-d6ffd7760613', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'MKL-C-20210001', '立式压力蒸汽灭菌器', 'GAUGE', NULL, NULL, NULL, NULL, 'TS-30B', NULL, NULL, 'SCRAPPED', '品管部', NULL, NULL, NULL, NULL, NULL, '2022-04-24', '2023-04-23', 12, 0, NULL, NULL, 1, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-14 12:02:41.68712+08', NULL, false, 0, NULL, NULL, 1, NULL, NULL, NULL, '莫珍弟', '上海博讯实业有限');
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5aa-7d5c-840b-871d3276f215', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'MKL-A-20210024', '1000L不锈钢反应釜', 'GAUGE', NULL, NULL, NULL, NULL, '1000L', NULL, NULL, 'SCRAPPED', '试剂洁净车间', NULL, NULL, NULL, NULL, NULL, '2025-10-30', '2026-10-29', 12, 0, NULL, NULL, 1, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-14 12:02:41.68712+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 2, NULL, NULL, 1, NULL, NULL, '黎智勇', '黎智勇', '深圳市汇兴源科技');
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5aa-7d71-a283-dfefd87ea6cc', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'A241101-597', '弹簧全启式安全阀', 'GAUGE', '安全阀类', NULL, NULL, NULL, 'A28X6T/8', '合金钢', NULL, 'IN_USE', '微生物室准备间', NULL, NULL, NULL, '0.25MPa', NULL, '2026-08-15', '2027-08-15', 365, 1, NULL, NULL, 1, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-14 12:02:41.68712+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 4, NULL, NULL, 1, '一年', 'CSV导入补录', '莫珍弟', '莫珍弟', '宁波某医疗器械');
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-717c-b98b-eefcb4113875', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260010', '多通道GL电极活化工装', 'TOOL', '研发工装', 'I', NULL, '10.01.010400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, 'DT04GLDJHHGZ030 V1.0.0.20260623', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-14 12:02:41.68712+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 5, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5aa-71b3-9606-9b3f7891f29d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'MKL-C-20210008', '绝缘电阻测试仪', 'GAUGE', NULL, NULL, NULL, NULL, 'CS2675CX-1', NULL, NULL, 'DISABLED', '品管部', NULL, NULL, NULL, '±2%MΩ', NULL, '2021-01-13', NULL, 12, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-14 12:02:41.68712+08', NULL, false, 0, NULL, NULL, 1, NULL, NULL, NULL, '温桂萍', '南京长盛仪器有限');
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260011', '多通道GL电极活化工装', 'TOOL', '研发工装', 'I', NULL, 'GL-ELECTRODE-ACT', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, 'DT04GLDJHHGZ030 V1.0.0.20260623', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:09:28.804031+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 1, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7e23-b061-6b34c4fa1d5f', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230001', '气血平衡仪', 'TOOL', '研发工装', 'II', NULL, '99.04.200003', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-73dc-9a63-8e7cfe402ad6', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230002', 'V5E乳酸电极膜定位工装', 'TOOL', '研发工装', 'III', NULL, '10.01.010400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7477-9242-26c69791cd60', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230004', 'BG3000试剂简易焊接工装', 'TOOL', '研发工装', 'III', NULL, '10.01.020400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-78eb-a759-fe16e42bab36', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230005', '比色皿压圈工装', 'TOOL', '研发工装', 'II', NULL, '10.01.040400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7096-98ab-7e7ab0961e95', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230006', '免疫模块调试工装', 'TOOL', '研发工装', 'I', NULL, '10.01.060600', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, 'V1.0.0.230801', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7800-9815-557c5bd99b05', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20230008', '免疫试剂卡检具', 'TOOL', '研发工装', 'III', NULL, '10.01.070208', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-755f-95b2-9be8411fd36b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20240001', '葡萄糖、乳酸电极检验工装', 'TOOL', '研发工装', 'I', NULL, '10.01.010400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, 'V2021.09.12.01', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-78fa-89b6-3095ee36db3f', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20240002', '滴眼工装', 'TOOL', '研发工装', 'I', NULL, '10.01.020400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7f65-afc6-be6bbed69fd5', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20250001', '电极PCB大板清洗工装', 'TOOL', '研发工装', 'III', NULL, '10.01.030400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7517-b9b0-8dd07bc0ab34', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20250002', '电极PCB大板清洗工装', 'TOOL', '研发工装', 'III', NULL, '10.01.040400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-79e7-a4bf-81327e1bcf77', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20250003', '点胶托盘工装', 'TOOL', '研发工装', 'III', NULL, '10.01.060600', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7380-902f-2afebb4c8bfb', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20250004', '点胶托盘工装', 'TOOL', '研发工装', 'III', NULL, '10.01.070208', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7f3c-b21a-3970bfb00be4', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20250005', '电极卡测试工装', 'TOOL', '研发工装', 'II', NULL, '10.01.010400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a7-78fe-b21b-fe694947d784', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260001', 'V5血气电解质分析仪活动门老化工装', 'TOOL', '研发工装', 'II', NULL, '10.01.020400', NULL, NULL, NULL, 'SCRAPPED', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 3, '2026-08-02', '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-77e2-88f3-63595c9a010b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260002', 'V5血气电解质分析仪活动门老化工装', 'TOOL', '研发工装', 'II', NULL, '10.01.030400', NULL, NULL, NULL, 'SCRAPPED', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', false, 1, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7215-8646-bb087815844c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260003', 'V8电极膜裁切工具', 'TOOL', '研发工装', 'IV', NULL, '10.01.040400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7175-8a92-f913526ef9e7', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260004', 'V8电极膜芯电焊夹具', 'TOOL', '研发工装', 'III', NULL, '10.01.060600', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7e43-bd9c-c6c4ff4dc095', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260005', 'Q80000试剂盒切管夹具', 'TOOL', '研发工装', 'III', NULL, '10.01.070208', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-78a4-9510-540753951fce', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260006', '（截图未显示，编号SKL-J-20260006）', 'TOOL', '研发工装', NULL, NULL, '10.01.010400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7c1d-a60e-fb6bdcb935db', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260007', '（截图未显示，编号SKL-J-20260007）', 'TOOL', '研发工装', NULL, NULL, '10.01.020400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-7dca-9ed5-e7af5457f223', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260009', '多通道GL电极检验工装', 'TOOL', '研发工装', 'I', NULL, '10.01.030400', NULL, NULL, NULL, 'REPAIRING', '技术部', NULL, NULL, 'DT04DJJYG005 V1.0.0.20260430', NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 1, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe6f-b5a8-76ad-a0e9-a42d9fbe0128', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'SKL-J-20260012', '电极卡带阻抗板测试工装', 'TOOL', '研发工装', 'II', NULL, '10.01.040400', NULL, NULL, NULL, 'IN_USE', '技术部', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-08-14', false, '2026-08-14 12:02:41.68712+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe69-cc66-7628-bf55-ac44941a5f2c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'T1', 'a', 'TOOL', NULL, NULL, NULL, '10.01.060600', NULL, NULL, NULL, 'IN_USE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, false, '2026-08-14 11:56:14.30549+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.tlm_tooling VALUES ('019ffe69-cc67-7bdb-9903-ba592fd6e473', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'T2', 'b', 'TOOL', NULL, NULL, NULL, '10.01.070208', NULL, NULL, NULL, 'IN_USE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, false, '2026-08-14 11:56:14.30549+08', NULL, '2026-08-16 11:16:24.553239+08', NULL, false, 0, NULL, '检测', 1, NULL, NULL, NULL, NULL, NULL);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_metro_record; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_metro_record VALUES ('cbc4914e-883b-f667-699b-b11860b48d9e', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7ce9-823e-d6ffd7760613', 'MKL-C-20210001', '立式压力蒸汽灭菌器', 'WO-TEST-001', 'B-001', '???', '10.02', 'mm', '10.00', '10.05', '9.95', '???', '2026-08-16 00:00:00', 'admin', NULL, false, '2026-08-16 13:39:07.325829', '2026-08-16 13:39:07.325829', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_repair; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_repair VALUES ('8825f70e-d1f3-4346-8e2d-a433e01b48b0', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'RP-DEMO-20260011-1', '活化温度波动超差', '校准温控模块,更换温度传感器', 'DONE', NULL, NULL, '2026-08-16 11:00:20.802902+08', NULL, '2026-08-16 11:00:20.802902+08', NULL, false, 0, '其他');
INSERT INTO ops.tlm_repair VALUES ('6c7571f7-9ccd-4146-bdb9-792e770a6a65', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'RP-DEMO-20260011-2', '卡槽夹具有松动', '紧固并加防松垫圈,点检通过', 'DONE', NULL, NULL, '2026-08-16 11:00:20.802902+08', NULL, '2026-08-16 11:00:20.802902+08', NULL, false, 0, '其他');
INSERT INTO ops.tlm_repair VALUES ('a487861f-8f05-306d-b630-e038367a0ff7', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-717c-b98b-eefcb4113875', 'TLM-RP-1786858797313', 'test', NULL, 'PENDING', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', NULL, '2026-08-16 13:39:57.316654+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', '2026-08-16 13:39:57.316654+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0, '磨损');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_scrap; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_scrap VALUES ('9c1bbf4d-e850-6275-56cd-57630cdab68d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a7-78fe-b21b-fe694947d784', 'TLM-SC-1786681564583', 'DESTROY', 'verify', 'APPROVED', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', NULL, '2026-08-14 12:26:04.584393+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 12:26:04.584393+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 2);
INSERT INTO ops.tlm_scrap VALUES ('c6dc8a54-3b9f-42f6-cf52-abc69c447f8a', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715635414', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:53:55.416603+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:53:55.416603+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('b9e9ca1f-3703-963b-5ce1-5863514dbb67', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715668106', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:54:28.106161+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:54:28.106161+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('bdb4b831-40ec-84de-b3c6-4e4c9a0018b4', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715680081', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:54:40.081672+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:54:40.081672+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('fa2977d6-ffa9-c8fd-7574-cb901e80c44c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715710123', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:55:10.124333+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:55:10.124333+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('4d02534a-a657-3a93-ffc3-d393739fc330', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715737549', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:55:37.54994+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:55:37.54994+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('98eb974b-d73b-3359-6219-69d410dfe2a3', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715788753', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:56:28.753745+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:56:28.753745+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('847df05e-acb9-a612-576b-e2c0e05af704', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715808232', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:56:48.233306+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:56:48.233306+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('4b83588d-11c5-b5a1-88b3-ad69644795e7', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715854759', 'DESTROY', 'UI实测报废', 'PENDING', NULL, NULL, '2026-08-14 21:57:34.761045+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:57:34.761045+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('3b942008-4add-5cdf-174b-8ede3b2006ee', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786715902420', 'DESTROY', 'UI实测报废', 'APPROVED', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', NULL, '2026-08-14 21:58:22.421163+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 21:58:22.421163+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 1);
INSERT INTO ops.tlm_scrap VALUES ('f63169b7-0f75-63ee-aa7d-e48ed4df52e4', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-77e2-88f3-63595c9a010b', 'TLM-SC-1786716216547', 'DESTROY', 'Playwright实测报废', 'PENDING', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', NULL, '2026-08-14 22:03:36.547342+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 22:03:36.547342+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('5db3e44b-291c-3776-6aa0-f6101f838d96', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-71b3-9606-9b3f7891f29d', 'TLM-SC-1786762091541', '报废', '测试', 'PENDING', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', NULL, '2026-08-15 10:48:11.542966+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', '2026-08-15 10:48:11.542966+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('692286ac-6087-3503-8b6e-038deeddad1d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d71-a283-dfefd87ea6cc', 'TLM-SC-1786763429134', '报废', '计量器具到期报废测试', 'PENDING', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', NULL, '2026-08-15 11:10:29.135594+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', '2026-08-15 11:10:29.135594+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.tlm_scrap VALUES ('46ef8032-9e62-8c34-5d3c-5b42c598cc9b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d5c-840b-871d3276f215', 'TLM-SC-1786763786436', '报废', '计量归档验证', 'APPROVED', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', NULL, '2026-08-15 11:16:26.438632+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', '2026-08-15 11:16:26.438632+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 1);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_calib_plan; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_calib_plan VALUES ('515f830e-526a-5bc1-a100-4186d93ae32c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d71-a283-dfefd87ea6cc', 'A241101-597', '弹簧全启式安全阀', 90, '2026-11-15', 'DONE', NULL, 'MANUAL', '2026-08-15 19:08:59.747671', '2026-08-15 19:08:59.747671', false, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 1, 'A241101-597-C1786763409814', '2026-08-15', '2027-08-15', 365, '±0.01mm', '合格', '周期校准', NULL);
INSERT INTO ops.tlm_calib_plan VALUES ('cfba9615-d533-3a91-d166-410d3f377a2f', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d71-a283-dfefd87ea6cc', 'A241101-597', '弹簧全启式安全阀', 90, '2026-12-31', 'DONE', NULL, 'MANUAL', '2026-08-15 19:31:52.48945', '2026-08-15 19:31:52.48945', false, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 1, 'A241101-597-C1786764753389', '2026-08-15', '2027-08-15', 365, '±0.01mm', '合格', '周期校准', 'CERT-2026-0001');
INSERT INTO ops.tlm_calib_plan VALUES ('8fb971c7-05d0-3f66-e913-1aae75734e0c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d5c-840b-871d3276f215', 'MKL-A-20210024', '1000L不锈钢反应釜', 180, '2027-01-31', 'PENDING', NULL, 'MANUAL', '2026-08-15 11:36:14.084407', '2026-08-15 11:36:14.084407', false, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_maint_plan; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_maint_plan VALUES ('57d0472a-41dd-cf9b-db67-fd0284dcaded', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a7-78fe-b21b-fe694947d784', 'TLM-MP-1786688375292', 'MONTH', '2026-08-02', NULL, NULL, '2026-08-14 14:19:35.296859+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0, '2026-08-14 14:19:35.296859', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1');
INSERT INTO ops.tlm_maint_plan VALUES ('29769497-c217-4074-479c-e520572027bc', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a7-78fe-b21b-fe694947d784', 'TLM-MP-1786752000038', 'MONTH', '2026-09-02', NULL, NULL, '2026-08-15 08:00:00.040547+08', NULL, false, 0, '2026-08-15 08:00:00.040547', NULL);
INSERT INTO ops.tlm_maint_plan VALUES ('d5c07f94-3121-43f2-bb60-85f4419a569d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'MP-DEMO-20260011', 'MONTH', '2026-09-14', '019fd5f3-ff15-7f6b-8bfd-ab276070f75e', '月度保养:清洁与功能点检', '2026-08-16 11:00:20.802902+08', NULL, false, 0, '2026-08-16 11:00:20.802902', NULL);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_tool_product; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_tool_product VALUES ('019fff38-0478-71b0-b566-867480577424', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7e23-b061-6b34c4fa1d5f', '10.01.070402', 'REF电极', '2026-08-14 15:41:29.079187+08', NULL, false, '2026-08-14 15:41:29.079187', NULL, 0, 'FINISHED', 'BG800系列用，KL04.0132.0000.09');
INSERT INTO ops.tlm_tool_product VALUES ('019fff38-047b-76b9-b5be-cd2c4d57b000', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-73dc-9a63-8e7cfe402ad6', '10.01.010400', '*K电极', '2026-08-14 15:41:29.082702+08', NULL, false, '2026-08-14 15:41:29.082702', NULL, 0, 'FINISHED', 'BG800专用，KL04.0133.0000.04');
INSERT INTO ops.tlm_tool_product VALUES ('019fff38-047c-7f42-8993-d570fafa4bd9', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7477-9242-26c69791cd60', '10.01.020400', '*Na电极', '2026-08-14 15:41:29.083648+08', NULL, false, '2026-08-14 15:41:29.083648', NULL, 0, 'FINISHED', 'BG800专用，KL04.0134.0000.05');
INSERT INTO ops.tlm_tool_product VALUES ('019fff38-047d-7064-bbe3-b1eb1ec67079', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7e23-b061-6b34c4fa1d5f', '99.04.200003', 'BG5000仪器侧面标贴', '2026-08-14 15:41:29.084485+08', NULL, false, '2026-08-14 15:41:29.084485', NULL, 0, 'MATERIAL', 'BXQ.1000.0008.00');
INSERT INTO ops.tlm_tool_product VALUES ('d0662571-3e74-45fd-9d78-d7034a276125', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'P-GL-V5', 'V5血气电解质分析仪', '2026-08-16 11:00:20.802902+08', NULL, false, '2026-08-16 11:00:20.802902', NULL, 0, 'FINISHED', 'V5');
INSERT INTO ops.tlm_tool_product VALUES ('97355046-0cf2-46d4-b1a5-c68f0b02524d', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'P-GL-V8', 'V8血气电解质分析仪', '2026-08-16 11:00:20.802902+08', NULL, false, '2026-08-16 11:00:20.802902', NULL, 0, 'FINISHED', 'V8');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_tool_version; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_tool_version VALUES ('a4256c22-9285-ca99-1229-87cf7932f840', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a7-78fe-b21b-fe694947d784', 'V1', 'DESIGN', '电极结构优化升级，提升密封性', 'mz.admin', '2026-08-02', '2026-08-14 15:02:17.19037+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 15:02:17.19037+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_tool_version VALUES ('6e42657f-5601-69e6-8ced-049c61667db0', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a7-78fe-b21b-fe694947d784', 'V2', 'UPGRADE', '更换耐高温材料，延长使用寿命', 'mz.admin', NULL, '2026-08-14 15:03:57.4361+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '2026-08-14 15:03:57.4361+08', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.tlm_tool_version VALUES ('60112b57-5a23-4398-bb15-06af17657c86', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'V1.0', 'DESIGN', '初始设计发布,用于 V5/V8 电极活化', 'MZ-研发工程师', '2026-03-01', '2026-08-16 11:00:20.802902+08', NULL, '2026-08-16 11:00:20.802902+08', NULL, false, 0);
INSERT INTO ops.tlm_tool_version VALUES ('f446f02c-6782-4f02-87e2-6e208b69ac60', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'V1.1', 'IMPROVE', '优化活化槽温控精度,提升电极一致性', 'MZ-研发工程师', '2026-06-15', '2026-08-16 11:00:20.802902+08', NULL, '2026-08-16 11:00:20.802902+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: tlm_tool_wo_bind; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.tlm_tool_wo_bind VALUES ('71dd61e1-6e0e-4ec0-9c8e-720528700e4b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5aa-7d71-a283-dfefd87ea6cc', 'WO-TEST-METRO-001', '2026-08-15 11:09:37.828135+08', NULL, 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, '2026-08-15 11:09:37.829851', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 0, '2026-08-15 11:09:37.829851', 'A241101-597', '2026-07-01', '2027-06-30');
INSERT INTO ops.tlm_tool_wo_bind VALUES ('e72ab1a8-003e-a268-7716-33931de69ceb', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '019ffe6f-b5a8-7c0e-ac82-4bbf1316cf43', 'MO000954-2', '2026-08-16 10:14:11.433534+08', NULL, '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, '2026-08-16 10:14:11.435041', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', 0, '2026-08-16 10:14:11.435041', NULL, NULL, NULL);


--
-- PostgreSQL database dump complete
--



