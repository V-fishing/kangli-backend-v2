-- ============================================================================
-- 种子数据 - 演示数据-售后(可选) (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : cs_work_order, cs_feedback
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
-- Data for Name: cs_work_order; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.cs_work_order VALUES ('0420ef32-756f-e843-2957-7129eaae579b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-1786770720961', '测试客户A', NULL, 'REPAIR', 'URGENT', '设备X', '无法启动', 'CLOSED', '019fd5f3-ff15-7f6b-8bfd-ab276070f75e', 'MeizhouOp', '2026-08-16 05:12:55.242012', 'replace power module', '2026-08-15 21:12:55.313121', '2026-08-15 13:12:55.390873', 5, 'good', NULL, NULL, '2026-08-16 13:12:00.963717', '2026-08-15 13:13:12.424136', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 3);
INSERT INTO ops.cs_work_order VALUES ('3b095bef-1e76-f3a3-8fee-c2706f062828', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-1786773083536', 'Playwright测试客户', '', 'REPAIR', 'NORMAL', '测试设备', 'Playwright 自动录入：设备无法启动', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-08-15 13:51:23.540098', '2026-08-15 13:51:23.540098', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 0);
INSERT INTO ops.cs_work_order VALUES ('21029e2c-4e65-4968-be99-2a3362f9e142', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-1786775305656', 'C2验证客户', '13800000000', 'REPAIR', 'NORMAL', '测试设备', 'C2通知验证工单', 'ASSIGNED', '118db5e2-3f9e-b0fc-17b1-40a7be92aa33', 'C2测试负责人', '2026-08-15 14:28:48.665987', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-08-15 22:28:25.658393', '2026-08-15 14:29:39.635436', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 1);
INSERT INTO ops.cs_work_order VALUES ('b7710527-b4d6-7dd9-23c6-e0b7ca05d07c', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-1786775866260', '超时预警验证客户', '13600000000', 'REPAIR', 'URGENT', '超时设备', '验证超时预警', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-08-10 09:00:00', NULL, '2026-08-15 14:37:46.261733', '2026-08-15 14:46:34.024999', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0);
INSERT INTO ops.cs_work_order VALUES ('42b222b8-bc2d-4b58-b712-f8064ae2fc67', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-DEMO-001', '康立医疗器械有限公司', '13800000001', 'INSTALL', 'NORMAL', 'V5血气电解质分析仪', '新装机现场安装与调试', 'CLOSED', '118db5e2-3f9e-b0fc-17b1-40a7be92aa33', 'MZ-SQE', '2026-08-01 09:00:00', '已完成安装调试,设备运行正常,客户签字确认', '2026-08-03 16:00:00', '2026-08-03 16:30:00', 5, '安装规范,响应及时', '2026-08-10 00:00:00', '广东省梅州市梅江区', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.cs_work_order VALUES ('002d5643-c631-44de-ab2e-07812b6d99b9', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-DEMO-002', '梅州人民医院', '13900000002', 'REPAIR', 'URGENT', 'V8电极膜裁切工具', '裁切尺寸偏差超差', 'ASSIGNED', 'f28ef82a-f574-8916-41e6-bdd04ead7725', 'MZ-质量工程师', '2026-08-12 10:00:00', NULL, NULL, NULL, 4, '处理较快', '2026-08-15 00:00:00', '广东省梅州市', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.cs_work_order VALUES ('318d3bf3-c686-444d-935e-9a18d01f61d6', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-DEMO-003', '深圳汇兴源科技', '13700000003', 'REPAIR', 'NORMAL', '1000L不锈钢反应釜', '搅拌异响', 'DONE', '019fd5f3-ff15-7f6b-8bfd-ab276070f75e', '梅州操作员', '2026-08-05 14:00:00', '更换轴承后异响消除', '2026-08-07 11:00:00', NULL, 3, '维修尚可', '2026-08-12 00:00:00', '广东省深圳市', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.cs_work_order VALUES ('6230f379-f2ed-4b78-a22e-8306546d55d6', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-DEMO-004', '广州某三甲医院', '13600000004', 'INSTALL', 'LOW', '多通道GL电极检验工装', '新院区设备装机', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-08-20 00:00:00', '广东省广州市', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);
INSERT INTO ops.cs_work_order VALUES ('50557ee2-95ab-45d3-aae8-0009a61ef8f1', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'WO-DEMO-005', '东莞某检验所', '13500000005', 'REPAIR', 'NORMAL', '绝缘电阻测试仪', '读数漂移', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-08-18 00:00:00', '广东省东莞市', '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: cs_feedback; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.cs_feedback VALUES ('d49445e7-a3c3-92bd-c348-68e1a9a7d401', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '反馈客户B', NULL, 'COMPLAINT', '设备噪音过大', 'WO-TEST-001', 'OPEN', NULL, NULL, NULL, NULL, '2026-08-15 13:42:30.478112', '2026-08-15 13:42:30.478112', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('9107fea0-8dd2-1a66-cb14-ba7050dc1cba', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'C2反馈客户', '13900000000', 'COMPLAINT', 'C2通知验证反馈内容', 'WO-1786775305656', 'OPEN', NULL, NULL, NULL, NULL, '2026-08-15 14:29:27.144879', '2026-08-15 14:29:39.553593', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('293fe135-e89c-fe76-de4e-14f59c6c8774', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'HANDLING验证客户', '13700000000', 'SUGGESTION', '验证处理中状态贯通', '', 'DONE', '已处理完成', '2026-08-15 14:37:35.340655', '测试处理人', NULL, '2026-08-16 06:37:19.115401', '2026-08-15 14:46:33.968037', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', true, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('36b7b3dc-ae4a-7181-7cb0-13ea3cecb08b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'Playwright反馈客户', '', 'COMPLAINT', 'Playwright 自动登记反馈：服务态度好', '', 'OPEN', NULL, NULL, NULL, 0, '2026-08-15 21:53:47.198633', '2026-08-15 21:53:47.198633', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', false, 1, NULL, '09af4353-7c68-49a1-b274-c3b9b3d5652c', '09af4353-7c68-49a1-b274-c3b9b3d5652c', NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('af98ea0f-66ed-4eee-93cc-f8340bfbad7b', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '康立医疗器械有限公司', '13800000001', 'COMPLAINT', '设备到货后安装等待时间过长,影响上线进度', 'WO-DEMO-001', 'DONE', '已协调工程师优先排期,并补偿现场培训一次', '2026-08-04 10:00:00', 'MZ-SQE', 4, '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, 'RESPONSE_SLOW', NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('612a22b2-2ece-4267-ac87-0de47ed3738a', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '梅州人民医院', '13900000002', 'SUGGESTION', '建议增加裁切工具的备用刀头配置,减少停机', NULL, 'OPEN', NULL, NULL, NULL, NULL, '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('413401e3-2bf1-4748-b5a5-e14ae7d869fe', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '深圳汇兴源科技', '13700000003', 'PRAISE', '维修响应快,工程师专业,问题一次解决', 'WO-DEMO-003', 'DONE', '感谢反馈,已记录至服务改进', '2026-08-08 09:00:00', '梅州操作员', 5, '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ops.cs_feedback VALUES ('ce41cc54-0a7c-4bfe-8ca0-1d09a7a64e15', '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '广州某三甲医院', '13600000004', 'INQUIRY', '咨询新院区装机是否需要提前准备气源管路', NULL, 'HANDLING', '已答复需提前预留压缩空气接口', '2026-08-13 15:00:00', 'MZ-SQE', NULL, '2026-08-16 10:43:05.663601', '2026-08-16 10:43:05.663601', NULL, NULL, false, 0, NULL, NULL, NULL, NULL, NULL);


--
-- PostgreSQL database dump complete
--



