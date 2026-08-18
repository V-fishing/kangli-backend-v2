-- ============================================================================
-- 种子数据 - 系统/权限/基础种子 (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : sys_org, sys_user, sys_role, sys_menu, sys_role_button, sys_role_menu, sys_user_role, sys_dict, sys_config, sys_data_scope, sys_delegation, sys_button, sys_attachment
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
-- Data for Name: sys_org; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_org VALUES ('019fd5dc-5197-75d2-bc6c-6c7be0def01a', 'MZ', '梅州分公司', NULL, NULL, 1, '公司', '启用', '2026-08-06 14:56:53.650669+08', NULL, '2026-08-06 14:56:53.650669+08', NULL, false, 0);
INSERT INTO ops.sys_org VALUES ('019fd5dc-5198-7184-a384-fc3c578bf11e', 'SZ', '深圳分公司', NULL, NULL, 2, '公司', '启用', '2026-08-06 14:56:53.650669+08', NULL, '2026-08-06 14:56:53.650669+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_user; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_user VALUES ('019fe08a-c40d-72af-82a1-e4ebd1d335b5', 'MZ_mes_sync', '$2a$10$placeholder_mes_sync_not_used_for_login', NULL, 'MES同步', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', '', '', '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-08 16:43:38.352108+08', NULL, '2026-08-08 17:50:57.273359+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.sys_user VALUES ('019fe08a-c40e-78de-927e-17487fd232e6', 'SZ_mes_sync', '$2a$10$placeholder_mes_sync_not_used_for_login', NULL, 'MES同步', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', '', '', '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-08 16:43:38.352108+08', NULL, '2026-08-13 09:51:20.5265+08', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', false, 0);
INSERT INTO ops.sys_user VALUES ('019fd5f3-ff15-7f6b-8bfd-ab276070f75e', 'mzuser', '$2a$10$gnTfAA1s5O7hMv025zDppeIGvPyRuMNcp7K3tG7K9FVDAo4WNwzQ.', NULL, '梅州操作员', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:45.396539+08', NULL, '2026-08-06 15:22:45.396539+08', NULL, false, 0);
INSERT INTO ops.sys_user VALUES ('a1fb9215-2ab0-2f96-7fec-e2f795283b9b', 'mz.operator', '$2a$10$qsPlkHwyZY5ry/OVR.PkMOtCucmyhHQI5Xoi5ssMH9CibYzQfx/nC', NULL, 'MZ-操作工', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:43.735575+08', NULL, '2026-08-06 15:22:43.735575+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('bc74d5a2-e920-3284-9a9e-b9c638df80ef', 'mz.inspector', '$2a$10$64mbhPJ2vp6fZUkwMGgdKuh6E.A3UWmChUvPYgMZXLWTKtsWg/R22', NULL, 'MZ-检验员', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:43.801846+08', NULL, '2026-08-06 15:22:43.801846+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('79eb8a6d-9676-515f-3ad9-696e3a0fa301', 'mz.shiftleader', '$2a$10$rjGuiRmTr4NDJH4Pluwe.u9nh.nPcJ5UwL.OeGnpTl8gePpQ0ISGu', NULL, 'MZ-班组长', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:43.868598+08', NULL, '2026-08-06 15:22:43.868598+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('f28ef82a-f574-8916-41e6-bdd04ead7725', 'mz.qe', '$2a$10$vRrdYuyY8WIAtv8KRSvR7OFm.Lcbh/i3KTnZLYBM9n9NSRglrgC8C', NULL, 'MZ-质量工程师', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:43.930665+08', NULL, '2026-08-06 15:22:43.930665+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('118db5e2-3f9e-b0fc-17b1-40a7be92aa33', 'mz.sqe', '$2a$10$gfJMkCONfGQMbWx4G4HXzOFkDC8mk7ahpd3V2PqoF1q1uPNmXg1I.', NULL, 'MZ-SQE', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:43.994269+08', NULL, '2026-08-06 15:22:43.994269+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('a9d66060-9fa3-d966-d945-9a3b1337b2fc', 'mz.qmanager', '$2a$10$BsmTnSq0pszaCHz0Do5J0uFhTnrGTMUXFbr37b/k/Bq9yHCAaS5Ra', NULL, 'MZ-质量经理', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.068701+08', NULL, '2026-08-06 15:22:44.068701+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('42ea2264-1ba7-85af-4370-78365f779ca2', 'sz.admin', '$2a$10$v30aGsksZspif4lClZr4wO.N7/mNvGXofVwFKjNdAr6wBvGGlAdiS', NULL, 'SZ-管理员', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.693599+08', NULL, '2026-08-06 15:22:44.693599+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('81138dd8-e3b1-53dd-fae3-99836f0de225', 'mz.rd', '$2a$10$ie3ygVVWa.oIXf1s.cocpetW0no21OxqB7frVKREREZ1YDMN93sTa', NULL, 'MZ-研发工程师', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.760218+08', NULL, '2026-08-06 15:22:44.760218+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('d6f3a0a0-2b81-9957-e293-a2c7696d3fac', 'mz.purchaser', '$2a$10$ShvUwooSMvD9QS2Q5JUkbuaJBBa4ZO4zyA3kCvkT2zpA0IEV2K8ZC', NULL, 'MZ-采购员', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.13061+08', NULL, '2026-08-06 15:22:44.13061+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('eea2d9c0-edcf-2de8-43f1-6ade752f23ec', 'sz.rd', '$2a$10$6Co4rpETovoipeUcQD9jDeXiMP8xNJxxhQMbSakPjLqr2Qi9UDoKy', NULL, 'SZ-研发工程师', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.818703+08', NULL, '2026-08-06 15:22:44.818703+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('bf33d3bd-902c-1d3d-406c-4b5939483b8f', 'admin', '$2a$10$9k5.LiUWogMA32ZuDG84d.5w4HWlyCozgNGs08WRXzhC15T3a2Ua.', NULL, '集团管理员', NULL, NULL, NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.885267+08', NULL, '2026-08-06 15:22:44.885267+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', 'mz.admin', '$2a$10$9eB/YhfqbEW1PyCm/iDFguV5qduCCf3M1ki1cX81xZiEZOePJMZUO', NULL, 'MZ-管理员', NULL, '019fd5dc-5197-75d2-bc6c-6c7be0def01a', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.194499+08', NULL, '2026-08-06 15:22:44.194499+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('0cdbda8a-77e4-236f-a10c-b52bbbd410d4', 'sz.operator', '$2a$10$TWUkRcsBS8YsgvqKYjt21OH9vz4vUdOATTwfECAWDq/pk33.C3c1u', NULL, 'SZ-操作工', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.260321+08', NULL, '2026-08-06 15:22:44.260321+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('565ceb7d-062e-de6a-a0bb-c06e163821c3', 'sz.inspector', '$2a$10$iKTi2maq24BbcYSO5y.pUOJFakaQVSh4WW.l5DgtJpummZPOKtPAO', NULL, 'SZ-检验员', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.320746+08', NULL, '2026-08-06 15:22:44.320746+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('55b888fc-b57e-130e-d33f-95f37deb6611', 'sz.shiftleader', '$2a$10$gdz8.2MKnV9WzLcHCS12wuA7Da9Lqr.sNY.yG6BKqvwxck0ic7hai', NULL, 'SZ-班组长', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.385316+08', NULL, '2026-08-06 15:22:44.385316+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('1e7a138b-a099-c1f1-b209-ed9ed54be803', 'sz.qe', '$2a$10$ahESxninsnw7c8dMyslMOeLXBeKFs7TXIxbVe38ecRYeUMfmNYNAO', NULL, 'SZ-质量工程师', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.448683+08', NULL, '2026-08-06 15:22:44.448683+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('dd9a2016-38af-8a88-8c68-7927d2201531', 'sz.sqe', '$2a$10$2.MceTep6dTpjB8pPQRtquR8Jf1gDCXeOGLXO6sowQsLqFe8pyqaC', NULL, 'SZ-SQE', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.510267+08', NULL, '2026-08-06 15:22:44.510267+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('5586f3b1-12d8-78b1-3383-cbc51203ec68', 'sz.qmanager', '$2a$10$cdVlq5YRp.6dq0x2QM1y.uecbBTZMWEEiOSe0n0S/V.E4EGX8OjG.', NULL, 'SZ-质量经理', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.568632+08', NULL, '2026-08-06 15:22:44.568632+08', NULL, false, 298);
INSERT INTO ops.sys_user VALUES ('e9d426be-a4b3-4a6b-648c-7df334c82eb6', 'sz.purchaser', '$2a$10$EBeEpv.Gs4jRVc1Rqft9WuPuaLhay2KpCztr/MUjEQ3vvMxEQBdzO', NULL, 'SZ-采购员', NULL, '019fd5dc-5198-7184-a384-fc3c578bf11e', NULL, NULL, '启用', NULL, NULL, NULL, 0, NULL, NULL, '2026-08-06 15:22:44.637678+08', NULL, '2026-08-06 15:22:44.637678+08', NULL, false, 298);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_role; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_role VALUES ('019fd5dc-5508-7434-9963-6ae87dff963c', 'sysadmin', '系统管理员', '预置', '全部权限', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, NULL);
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7bd7-8f04-fec45f22420e', 'operator', '操作工', '预置', '产线操作 · 自检数据录入', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7099-9dd7-05d9eee93e92', 'operator', '操作工', '预置', '产线操作 · 自检数据录入', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7de5-8ad9-4e085677e466', 'inspector', '生产检验员', '预置', '首件/来料检验 · 不良录入 · 器具使用', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7536-9965-e4a923dd33b6', 'inspector', '生产检验员', '预置', '首件/来料检验 · 不良录入 · 器具使用', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7108-b319-c61ea6ca0682', 'shiftleader', '班组长', '预置', '产线管理 · 报警确认关闭 · 工装状态', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7ee1-97f5-9f54bcadc895', 'shiftleader', '班组长', '预置', '产线管理 · 报警确认关闭 · 工装状态', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7d38-af2d-721cd77d0df8', 'qe', '质量工程师', '预置', '质量分析 · SPC · 8D整改 · CAPA', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7cfe-b814-0d76c14013cd', 'qe', '质量工程师', '预置', '质量分析 · SPC · 8D整改 · CAPA', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-7cb6-b760-c1da652ab9a0', 'sqe', 'SQE', '预置', '供应商审核 · 来料异常处置 · 整改验证', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-753b-91cb-03baaaa2517b', 'sqe', 'SQE', '预置', '供应商审核 · 来料异常处置 · 整改验证', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-78ec-ba54-c221b77dc53f', 'qmanager', '质量经理', '预置', '审批授权 · 趋势分析 · 绩效评审', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-5509-788e-b885-f37e68d31ddc', 'qmanager', '质量经理', '预置', '审批授权 · 趋势分析 · 绩效评审', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-74af-b487-c605e0524dc2', 'purchaser', '采购员', '预置', '采购订单 · 供应商准入 · 物料变更', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-7e00-bf48-9bdad1801952', 'purchaser', '采购员', '预置', '采购订单 · 供应商准入 · 物料变更', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-7d2b-b55c-f583dec4d8a9', 'rd', '研发工程师', '预置', '物料变更研发审批 · 工艺/验证评估', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-71c8-8224-10b5ceb6d875', 'rd', '研发工程师', '预置', '物料变更研发审批 · 工艺/验证评估', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-79df-b659-45ae9a203ec5', 'admin', '管理员', '预置', '公司内用户/角色管理 · 基础数据维护', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5197-75d2-bc6c-6c7be0def01a');
INSERT INTO ops.sys_role VALUES ('019fd5dc-550a-77d4-a06c-f38fbaf957f7', 'admin', '管理员', '预置', '公司内用户/角色管理 · 基础数据维护', '启用', '2026-08-06 14:56:54.514679+08', NULL, '2026-08-06 14:56:54.514679+08', NULL, false, 1, '019fd5dc-5198-7184-a384-fc3c578bf11e');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_menu; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_menu VALUES ('019fd5dc-54e1-7c92-815b-0dece11f1f79', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.org.list', '组织管理', '菜单', '/system/orgs', 'system/OrgView', '⚙️', 1, true, '2026-08-06 14:56:54.489942+08', NULL, '2026-08-06 14:56:54.489942+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-54e1-76b5-9e51-ac80d1c1f394', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.menu.list', '菜单管理', '菜单', '/system/menus', 'system/MenuList', '⚙️', 2, true, '2026-08-06 14:56:54.489942+08', NULL, '2026-08-06 14:56:54.489942+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-54e1-77a7-8f28-798acd1ad7b0', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.role.list', '角色管理', '菜单', '/system/roles', 'system/RoleList', '⚙️', 3, true, '2026-08-06 14:56:54.489942+08', NULL, '2026-08-06 14:56:54.489942+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-54e1-77c6-a607-95b99bc63155', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.user.list', '用户管理', '菜单', '/system/users', 'system/UserList', '⚙️', 4, true, '2026-08-06 14:56:54.489942+08', NULL, '2026-08-06 14:56:54.489942+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d4-758c-80f7-0221d64ec698', NULL, 'fia', '首件检验', '目录', '/fia', NULL, '📋', 10, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-7192-bb54-c37eb0729def', NULL, 'spc', 'SPC 监控', '目录', '/spc', NULL, '📈', 20, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d7-754a-839d-7f7265c42c50', NULL, 'ncm', '不良管理', '目录', '/ncm', NULL, '⚠️', 30, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-7a2f-9b21-7369bf2077ae', NULL, 'sqm', '供应商', '目录', '/sqm', NULL, '🏢', 40, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51da-77e7-a55d-16bea9adab3b', NULL, 'patrol', '巡检', '目录', '/patrol', NULL, '🔦', 50, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51da-7686-8064-c7c8019205b3', NULL, 'archive', '归档', '目录', '/archive', NULL, '🗄️', 60, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51db-7e02-9a2f-9c209fd28445', NULL, 'system', '系统管理', '目录', '/system', NULL, '⚙️', 90, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d2-7324-8a3e-16bd032a2b5e', NULL, 'dashboard', '工作台', '菜单', '/dashboard', 'dashboard/index', '📊', 0, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-7794-aee4-b73d0c73b3d0', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.dash', '任务列表', '菜单', '/fia/tasks', 'fia/TaskList', '', 1, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-7a32-ae2d-bfdf7d34066b', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.tasks', '新建任务', '菜单', '/fia/tasks/create', 'fia/TaskCreate', '', 2, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-77b6-af02-f3d418647ce7', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.entry', '检验标准', '菜单', '/fia/stds', 'fia/StdList', '', 3, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-780c-be52-dc2f774aadd9', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.trigcfg', '触发类型', '菜单', '/fia/triggers', 'fia/TriggerList', '', 4, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-7810-b631-aaf31236f6af', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.approve', '审批单', '菜单', '/fia/approvals', 'fia/ApprovalList', '', 5, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-7fca-92b3-22448bd8d8db', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.stdlib', '检验标准库', '菜单', 'stdlib', 'fia/Stdlib', '📚', 5, false, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d5-713d-9770-7262e3cc836e', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.trace', '追溯归档', '菜单', 'trace', 'fia/Trace', '🔗', 7, false, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-73ab-b38d-ea4c880f7fa1', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.dash', '控制图/参数', '菜单', '/spc/params', 'spc/ParamList', '', 1, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-73e5-8cb9-0d4d8694d3d9', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.collect', '数据采集', '菜单', '/spc/collect', 'spc/CollectView', '', 2, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-7aa1-bb1c-5532bac6fc97', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.alarm', '告警', '菜单', '/spc/alarms', 'spc/AlarmList', '', 3, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-799e-8336-a148f5fa3109', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.control', '控制图监控', '菜单', 'control', 'spc/Control', '📈', 2, false, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-7aab-ad65-eb8bb85a40f2', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.paramcfg', '参数配置', '菜单', 'paramcfg', 'spc/ParamCfg', '⚙️', 6, false, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-7c4c-8700-999b9e498a58', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.dash', '不良字典', '菜单', '/ncm/defect-dicts', 'ncm/DefectDictList', '', 1, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-776c-8cb6-74903e6501d6', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.entry', '不良记录', '菜单', '/ncm/defect-records', 'ncm/DefectRecordList', '', 2, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-72c6-a20a-013c193412ff', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.analysis', '8D报告', '菜单', '/ncm/8d-reports', 'ncm/8dList', '', 3, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-7e86-9643-08356660d83c', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d', 'CAPA', '菜单', '/ncm/capas', 'ncm/CapaList', '', 4, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7eaa-bd22-b3ca386fb084', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.dash', '供应商档案', '菜单', '/sqm/suppliers', 'sqm/SupplierList', '', 1, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7977-96dd-ff09b640a869', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.abnormal', '来料异常', '菜单', '/sqm/abnormals', 'sqm/AbnormalList', '', 2, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7408-97d3-e6ed0a92ecc4', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit', '供应商审核', '菜单', '/sqm/audits', 'sqm/AuditList', '', 3, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7701-8c2c-835ecfff3e60', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change', '物料变更', '菜单', '/sqm/changes', 'sqm/ChangeList', '', 4, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7ee6-a120-45b73eda04f3', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.trace', '物料追溯', '菜单', '/sqm/trace', 'sqm/TraceList', '', 5, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7fbe-b35c-c9a772bbc268', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.capa', '供应商绩效', '菜单', '/sqm/performance', 'sqm/PerformanceList', '', 6, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7e0e-a59c-b2e6adda7747', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea', 'FMEA风险管控', '菜单', '/sqm/fmea', 'sqm/FmeaList', '', 7, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d9-7f2f-80c1-2bba8d5d1592', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.lifecycle', '供应商全生命周期', '菜单', 'lifecycle', 'sqm/Lifecycle', '🗂️', 2, false, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5527-7cb5-86b8-8cc9741427a1', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patrol.routes', '巡检路线', '菜单', '/patrol/routes', 'patrol/RouteList', '', 1, true, '2026-08-06 14:56:54.558276+08', NULL, '2026-08-06 14:56:54.558276+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5527-7837-b567-8b4b2e21ba59', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patrol.tasks', '巡检任务', '菜单', '/patrol/tasks', 'patrol/TaskList', '', 2, true, '2026-08-06 14:56:54.558276+08', NULL, '2026-08-06 14:56:54.558276+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5527-7a99-b861-9a8a10988fbc', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patrol.abnormals', '巡检异常', '菜单', '/patrol/abnormals', 'patrol/AbnormalList', '', 3, true, '2026-08-06 14:56:54.558276+08', NULL, '2026-08-06 14:56:54.558276+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5527-7ec4-ac74-f8ded022d695', '019fd5dc-51da-7686-8064-c7c8019205b3', 'archive.list', '归档查询', '菜单', '/archive/list', 'archive/ArchiveList', '', 1, true, '2026-08-06 14:56:54.558276+08', NULL, '2026-08-06 14:56:54.558276+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5527-7bd0-8fad-057ac3513b9c', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.audit-config', '审核配置', '菜单', '/system/audit-config', 'sqm/AuditApprovalConfig', '', 5, true, '2026-08-06 14:56:54.558276+08', NULL, '2026-08-06 14:56:54.558276+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5559-79fe-95bc-f9e59bbab203', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.wolock', '工单锁定', '菜单', '/fia/wo-lock', 'fia/WoLockList', '', 6, true, '2026-08-06 14:56:54.609431+08', NULL, '2026-08-06 14:56:54.609431+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-556e-73cb-a804-9862819894b3', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.incoming', '来料检验', '菜单', '/fia/incoming', 'fia/IncomingList', '', 7, true, '2026-08-06 14:56:54.633501+08', NULL, '2026-08-06 14:56:54.633501+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-557e-70c7-a36f-f7c7f61c6a03', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.corrective-actions', '纠正措施', '菜单', '/ncm/corrective-actions', 'ncm/CorrectiveActionList', '', 5, true, '2026-08-06 14:56:54.649903+08', NULL, '2026-08-06 14:56:54.649903+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d8-7eec-9055-4f5b6aaaa29d', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.trend', '趋势报表', '菜单', '/ncm/trend-reports', 'ncm/TrendReport', '', 6, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('14fd60c2-2d74-400e-b68e-283cccf51649', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.audit.list', '审计日志', 'M', '/system/audit-logs', 'views/system/AuditLogList.vue', 'audit', 98, true, '2026-08-13 13:34:51.653864+08', '019fd5dc-51db-7e02-9a2f-9c209fd28445', '2026-08-13 13:34:51.653864+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-51d6-7668-93fe-89b57b6f90b3', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.capability', '能力分析', '菜单', '/spc/capability', 'spc/Capability', '', 4, true, '2026-08-06 14:56:53.710623+08', NULL, '2026-08-06 14:56:53.710623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5591-71f5-9e46-03cba2242165', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.rules', '判异规则', '菜单', '/spc/rules', 'spc/RuleDetection', '', 5, true, '2026-08-06 14:56:54.667623+08', NULL, '2026-08-06 14:56:54.667623+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-55b7-7f05-be96-93f5ef62a685', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.collecttasks', '采集任务管理', '菜单', '/spc/collect-tasks', 'spc/CollectTask', '🗓️', 3, true, '2026-08-06 14:56:54.704986+08', NULL, '2026-08-06 14:56:54.704986+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-55f1-7702-b666-f4431fa21c2a', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.std-items', '检验项明细', '菜单', '/fia/std-items', 'fia/StdItemMaintain', '', 6, true, '2026-08-06 14:56:54.764307+08', NULL, '2026-08-06 14:56:54.764307+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5dc-5773-7c2b-a8df-80ba0650d3ab', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.specstandard', '标准线管理', '菜单', '/spc/spec-standards', 'spc/SpecStandard', '', 6, true, '2026-08-06 14:56:55.148624+08', NULL, '2026-08-06 14:56:55.148624+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5f2-185d-7f1a-854d-056ee195acd8', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.notify.config', '通知配置', '菜单', '/system/notify-config', 'views/system/NotifyConfig.vue', '🔔', 95, true, '2026-08-06 15:20:40.791006+08', NULL, '2026-08-06 15:20:40.791006+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5f2-18ba-734e-8acc-4367490fcf80', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.notify.center', '通知中心', '菜单', '/system/notify-center', 'views/system/NotifyCenter.vue', '📨', 96, true, '2026-08-06 15:20:40.885113+08', NULL, '2026-08-06 15:20:40.885113+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd5f4-0019-7526-b0c0-f03a30f69e2b', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.process', 'SPC工序管理', '菜单', '/spc/processes', 'spc/processes', NULL, 1, true, '2026-08-06 15:22:45.656194+08', NULL, '2026-08-06 15:22:45.656194+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fd657-e0f1-7efa-b193-d90aa187a4de', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.abnormal-rule', '异常严重度规则', '菜单', '/system/abnormal-rule', 'views/system/AbnormalRuleConfig.vue', NULL, 97, true, '2026-08-06 17:11:51.222525+08', NULL, '2026-08-06 17:11:51.222525+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fdb0b-641b-7876-b1d1-2c53cf0c40a3', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.dashboard', '供应商质量看板', '菜单', '/sqm/dashboard', 'views/sqm/SqmDashboard.vue', '📊', 8, true, '2026-08-07 15:06:24.584272+08', NULL, '2026-08-07 15:06:24.584272+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019fe029-d89a-7416-9260-f9ab51194f91', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e', 'workbench.tasks', '个人任务中心', '菜单', '/workbench/tasks', 'workbench/TaskCenter', '🗂️', 1, true, '2026-08-08 14:57:46.635686+08', NULL, '2026-08-08 14:57:46.635686+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ff015-d932-7e5a-afc7-e96442821270', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.sampletask', '抽样任务创建', '菜单', '/spc/sample-tasks', 'spc/SampleTaskCreate', NULL, 4, true, '2026-08-11 17:09:51.537388+08', NULL, '2026-08-11 17:09:51.537388+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ff58d-680f-717d-a25c-f40a7a6cf78f', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e', 'workbench.messages', '消息中心', '菜单', '/workbench/messages', 'workbench/MessageCenter', '📮', 2, true, '2026-08-12 18:38:32.963235+08', NULL, '2026-08-12 18:38:32.963235+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ff87b-1682-738e-9900-b655a1f6bfb0', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.perf.config', '绩效指标配置', '菜单', '/sqm/perf-config', 'sqm/PerfConfig', '⚙️', 99, true, '2026-08-13 08:17:23.719489+08', NULL, '2026-08-13 08:17:23.719489+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ffe6f-b5e8-738d-ba9e-366909b4094f', NULL, 'tlm', '工装管理', '目录', '/tlm', NULL, '🔧', 10, true, '2026-08-14 12:02:41.76096+08', NULL, '2026-08-14 12:02:41.76096+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ffe6f-b5eb-7fef-8899-134a6bfc164b', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.tooling.list', '工装台账', '菜单', '/tlm/tooling', 'tlm/Tooling', '🔧', 1, true, '2026-08-14 12:02:41.76096+08', NULL, '2026-08-14 12:02:41.76096+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.maint.list', '工装维保', '菜单', '/tlm/maint', 'tlm/Maint', '🛠', 2, true, '2026-08-14 12:02:41.76096+08', NULL, '2026-08-14 12:02:41.76096+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('019ffe6f-b5eb-75dc-b569-a02d47dd9fca', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.abnormal.list', '工装异常', '菜单', '/tlm/abnormals', 'tlm/Abnormal', '⚠️', 3, true, '2026-08-14 12:02:41.76096+08', NULL, '2026-08-14 12:02:41.76096+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a000ba-ac7d-7a91-8d53-00bed2c4b73a', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.scrap.list', '报废管理', '菜单', '/tlm/scraps', 'tlm/Scraps', NULL, 4, true, '2026-08-14 22:43:48.988177+08', NULL, '2026-08-14 22:43:48.988177+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a000e9-117b-7526-9c17-7c79cf2a0ebe', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.repair.list', '维修工单', '菜单', '/tlm/repairs', 'tlm/Repair', '🔧', 4, true, '2026-08-14 23:34:29.483721+08', NULL, '2026-08-14 23:34:29.483721+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00327-5a87-7dc3-ac27-29b4702f190d', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.metro.list', '计量管理', '菜单', '/tlm/metro', 'tlm/Metro', '📐', 5, true, '2026-08-15 10:01:45.843759+08', NULL, '2026-08-15 10:01:45.843759+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a003d3-bc38-7749-9a97-f62f7b6471a7', NULL, 'cs', '售后管理', '目录', '/cs', NULL, '🎧', 11, true, '2026-08-15 13:10:03.052814+08', NULL, '2026-08-15 13:10:03.052814+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', '01a003d3-bc38-7749-9a97-f62f7b6471a7', 'cs.workorder.list', '工单管理', '菜单', '/cs/work-orders', 'cs/WorkOrder', '🛠', 1, true, '2026-08-15 13:10:03.052814+08', NULL, '2026-08-15 13:10:03.052814+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a003f0-ca57-79a9-ad2a-231407159c98', '01a003d3-bc38-7749-9a97-f62f7b6471a7', 'cs.satisfaction.list', '满意度看板', '菜单', '/cs/satisfaction', 'cs/Satisfaction', '📊', 2, true, '2026-08-15 13:41:47.196211+08', NULL, '2026-08-15 13:41:47.196211+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a003f0-ca5e-7d7f-9f09-a53d1828e380', '01a003d3-bc38-7749-9a97-f62f7b6471a7', 'cs.feedback.list', '客户反馈', '菜单', '/cs/feedback', 'cs/Feedback', '💬', 3, true, '2026-08-15 13:41:47.196211+08', NULL, '2026-08-15 13:41:47.196211+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c66-743a-ac33-db8b353c65e6', NULL, 'qms-mgmt', '体系管理', '目录', '/qms-mgmt', NULL, '🗂', 12, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c6d-7273-b552-36d597ef7989', '01a00441-5c66-743a-ac33-db8b353c65e6', 'qms-mgmt.goal', '质量目标管理', '菜单', '/qms-mgmt/goal', 'qms-mgmt/QualityGoal', '🎯', 1, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c6e-7f0a-ad28-54743f700ff8', '01a00441-5c66-743a-ac33-db8b353c65e6', 'qms-mgmt.audit', '内审数据管理', '菜单', '/qms-mgmt/audit', 'qms-mgmt/InternalAudit', '🔍', 2, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c6e-759e-b369-f35454a11edc', '01a00441-5c66-743a-ac33-db8b353c65e6', 'qms-mgmt.adverse', '不良事件管理', '菜单', '/qms-mgmt/adverse', 'qms-mgmt/AdverseEvent', '⚠', 3, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c6e-771a-a4cc-e605d0ff97da', '01a00441-5c66-743a-ac33-db8b353c65e6', 'qms-mgmt.feedback', '顾客反馈分析', '菜单', '/qms-mgmt/feedback', 'qms-mgmt/FeedbackAnalysis', '💬', 4, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a00441-5c6e-7554-be66-f8efb6cdc2d9', '01a00441-5c66-743a-ac33-db8b353c65e6', 'qms-mgmt.dashboard', '体系合规监控', '菜单', '/qms-mgmt/dashboard', 'qms-mgmt/ComplianceBoard', '📈', 5, true, '2026-08-15 15:09:47.474696+08', NULL, '2026-08-15 15:09:47.474696+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a008f2-6b72-7609-abe9-0dae1e9572c0', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.repair.analysis', '维修根因分析', '菜单', '/tlm/repair-analysis', 'tlm/RepairAnalysis', '📊', 5, true, '2026-08-16 13:01:40.062181+08', NULL, '2026-08-16 13:01:40.062181+08', NULL, false, 0);
INSERT INTO ops.sys_menu VALUES ('01a008f2-6bd6-71f7-9014-82fbcdf47619', '019ffe6f-b5e8-738d-ba9e-366909b4094f', 'tlm.metro.collect', '计量数据采集', '菜单', '/tlm/metro/collect', 'tlm/MetroCollect', '📏', 6, true, '2026-08-16 13:01:40.176127+08', NULL, '2026-08-16 13:01:40.176127+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_role_button; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-73d4-bf58-b92003be1293', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7df5-8139-b642d37694a6', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7bb5-bc58-25f662a2c3ea', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7b2a-afa7-292e247b3c30', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7d72-a56b-598d863fbe5b', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7a46-a05c-4f74465814da', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-75a5-b84d-e0098d0dcfa9', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-75aa-aa75-b076e5b0e52a', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7d88-a6cc-9f2bae9afb44', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7bd2-9c44-2cd881c9baf8', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7a94-a3e9-ab61993f0b9b', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7971-8824-b83806334ed7', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7ae9-8b73-814c19e397b4', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7231-aac3-5b96d320c3f7', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7fa1-9bab-d61ccfc8638f', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7123-9354-fb7e11646d45', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-723f-93ab-35f091fe8e84', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7037-ae87-26ddb17a7a26', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7e19-891c-4625c93bb5c7', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7515-9dc6-d42112d826bd', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-757c-a42a-a5e77743a04b', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7254-9006-8719614e478c', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7b5c-98fb-f1c7b5737321', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7f47-a858-56517031c6ff', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-712f-b853-f9ff9381356e', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7c74-a6c5-2b31cfbfff13', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-708b-9a1c-04ebf11c2014', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7382-bb9d-1ea6465008ca', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-711b-871a-50a23a44cf33', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7d0a-a207-bfbbc0c829fe', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-78e1-802e-7fd28276362c', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7dce-abcc-a2a9be92de47', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7f10-ad2d-e3ee8320c3d2', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-727e-a6b9-c867b6ff6e7a', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-70ba-835a-28ab8d5a3fb6', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-75f9-9cd1-17ed38368a4c', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-71a0-974e-58dc09b3b3e8', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7d53-b46e-6efab6c05127', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7416-8e7e-bc3f1f4c3e7e', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-76b1-815a-8e7cbf3b692f', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5861-7d56-a3da-7aadc8a153d7', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-71a5-bfdc-757ad0dc336b', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-75f7-8493-4feab476ea7d', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-77aa-9bc5-356e6c772314', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-77b8-b9b9-f09dcad97885', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7029-bf81-a3e0c652bb23', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7e9c-89a1-ee40c365e577', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7709-ab01-3651318f67df', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7af5-9cf2-100aecd23235', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-745c-a06f-cf4d1b513c80', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7936-b191-e8878984f940', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-72a4-9886-35bcbf4084f7', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7e28-9c4c-d9bad199280d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-74c5-b426-41f14d2d8977', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-720f-9e88-b7e13a8dc393', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7540-a1a6-b23573c9c36a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7931-88a7-e0b29a74b661', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-746f-96f5-62fc58e8e531', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7477-a38c-20cfa447f231', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550b-7113-a38c-706c54ecd39d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('019fe663-3e60-7a58-8f96-9ed55365953f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fe663-3e5a-7664-87ea-76e5e7b5c889');
INSERT INTO ops.sys_role_button VALUES ('019ff015-d962-7d4f-b55b-7bb7b668ab6f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ff015-d957-7349-b988-16e46b953ec7');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550c-7007-9b65-66e039375e86', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('019fd5dc-550c-7503-946a-cf24461f73b0', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7ef3-8c8a-5f05ab44b5bd', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('019ff015-d96f-76a9-b87d-90a211221201', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ff015-d957-7349-b988-16e46b953ec7');
INSERT INTO ops.sys_role_button VALUES ('019ff015-d97e-77b4-b1b5-3e6085ff3a0c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ff015-d974-7d61-816c-48bb179b09a9');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7aa6-b53f-3be7ad32b6d5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('019ff015-d993-7f4e-b801-b4d9189b0a6e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ff015-d974-7d61-816c-48bb179b09a9');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-edda-7362-a681-97b32dcb5b87', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f4-005b-7a4c-8467-a24451d1795c');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-ede2-73b1-87c4-4042de16c8ce', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f4-0057-7b05-a448-4c9eeb2c3f3d');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-ede2-71f3-8d9c-43956fd3c408', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f3-ffcd-7d5f-9090-ab8e6ba5f5b8');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-ede2-749e-becd-3a17cd0bfa49', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f4-005f-7b53-9c2f-5deada294629');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-ede2-7eb9-a576-5ee447e95d27', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f4-0052-776f-8785-17e161c8f635');
INSERT INTO ops.sys_role_button VALUES ('019ff62c-ede2-742a-a419-164dce75a5e7', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f4-0064-7cf3-a5ec-ccec1ec7c48e');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5f7-7d76-8e72-14725d697215', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ed-7c14-9cf4-245d0b19c5db');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5f9-746a-ad20-02f3286a0fa8', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-75af-9d9d-fb4afd65d5c7');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5f9-76b7-8f2e-05d8475c5889', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-79a0-8e3d-60a6a557e8fe');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5f9-7ebb-8fe6-c70935340eb4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7918-8ac1-7c777cf59a9b');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5f9-73c0-a4f8-df4298a284f0', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-77dc-880d-59679e3d1ff8');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7d55-8e83-9265964e1340', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7b49-b04d-d3d5c1237f8d');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7bcc-9a4f-057d95ca2555', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7acd-8127-9958d8b113dd');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7c91-8a3e-447731094377', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003b8-5859-7209-a784-e24d83d51825');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-704d-b9a2-e79809227420', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7456-9948-ad2b11b2d382');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7e83-92c8-62b18d34cc08', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-746d-b7d5-15045e0fc5b6');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7923-855c-2ce081a522ab', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-76d5-b598-3905148f0875');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-73fc-9241-c0b8311c23c1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-76c2-89b6-6ab7176ec64a');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7961-91b6-691bcb94d86b', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7082-b382-4c747ba98e40');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7c36-9643-6797c02350a7', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-72eb-8cba-dfe5b9055f8b');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7cf1-bbb7-b725bcbad43c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-73a1-857b-49bfb1405291');
INSERT INTO ops.sys_role_button VALUES ('019ffe6f-b5fa-7d4e-a4bd-3df77fd9cf97', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5ef-7c32-8bec-3a6f4605be89');
INSERT INTO ops.sys_role_button VALUES ('01a003b8-5862-7dd6-b74b-dd49292f76fe', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003b8-585b-7a4c-aa71-b4690188ac63');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-77e0-8ee7-5a1b76e46561', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7bf3-8b14-86f580bed24e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-799f-91a2-26239abb2e05', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-719d-a235-d5f087f9a32b', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c75-761a-8375-e5a8e2beb0dd');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7306-9d2b-93f8e31af299', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c75-761a-8375-e5a8e2beb0dd');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-733b-9337-13aeb39a22b1', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c75-761a-8375-e5a8e2beb0dd');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7033-9ed3-8a95797afcd8', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c75-750e-a883-941a5dc1f6b8');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-732a-9738-13b174f5108a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c75-750e-a883-941a5dc1f6b8');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-72cc-ac60-1d13a1f4820b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c75-750e-a883-941a5dc1f6b8');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-73d4-b723-599eee27f898', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c75-7697-bfce-996f6bb68114');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7d9e-96af-a67db3ff57ed', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c75-7697-bfce-996f6bb68114');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7747-bd45-ae8839fe1bc0', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c75-7697-bfce-996f6bb68114');
INSERT INTO ops.sys_role_button VALUES ('01a00350-4a73-7346-a5e2-709bf899ef3c', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7221-ba1d-dbcfa6b09fbb', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('01a00350-4a74-719e-9076-91e12ab1611f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142b-7d2e-871b-ea0d539b2095', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7b71-a692-613458801288', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7a72-af3a-ea861fbe7019', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7396-8e1b-f27bb2f59693', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-75c7-a41f-d6b4f3e971ea', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7983-ab25-65f58780e8d5', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7a62-b22f-bc7f09659336', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0029-791d-9a6c-fe94f3e53148', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0022-78b2-b252-7d7b36cdd2bb');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-70df-8ab1-c8acc2daf0e1', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7a8c-8d6c-08f74db09022', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0032-7f97-92fd-71284118484c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-002b-7b41-9482-18b6c25c8531');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-71ef-9037-d6240948e4e6', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7cc0-bf3f-d3ceb494dba1', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-003b-78c4-b632-6c3794bbbce5', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0034-7917-8ce4-74b457ffecc2');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7083-9e11-50702d4cb9ba', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7bd1-9049-a72bb9d13b4f', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7e17-89d4-68bc63e4414a', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7c62-8102-e06222ee9b03', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7732-8492-4772467291d9', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-7d5e-afde-7f56b2b0d708', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-77c5-9364-fe0e43898f0f', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-783a-bf6c-c311edbcc187', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7e84-a42f-bfd44e7f6797', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ed-7c14-9cf4-245d0b19c5db');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-75d0-bef4-1a898a4f1aff', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-75af-9d9d-fb4afd65d5c7');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7d5d-822d-6670fcb0c1cc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-79a0-8e3d-60a6a557e8fe');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-766a-a3e5-8e38a983095e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7918-8ac1-7c777cf59a9b');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-78bc-a114-fa2e4989d1bd', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-77dc-880d-59679e3d1ff8');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7397-a5b9-8305347c3fe6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7b49-b04d-d3d5c1237f8d');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7064-814e-d7015e0d4b0e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7acd-8127-9958d8b113dd');
INSERT INTO ops.sys_role_button VALUES ('01a003bc-142e-75bd-9811-2f76483b92da', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7a1c-9092-d3c52b33a2fd', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7456-9948-ad2b11b2d382');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7fcf-95dd-2725a667a173', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-746d-b7d5-15045e0fc5b6');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7f9b-9b8e-f0e8f37ae799', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-76d5-b598-3905148f0875');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7fcf-b5ec-1e0318b5d47c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-76c2-89b6-6ab7176ec64a');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7a50-a730-a5efa7de2228', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7082-b382-4c747ba98e40');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-700d-a505-b8eafd94cc6b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-72eb-8cba-dfe5b9055f8b');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7d44-acb5-0eeffb7ba749', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-73a1-857b-49bfb1405291');
INSERT INTO ops.sys_role_button VALUES ('019ffe74-bbde-7909-98be-820570aeb776', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5ef-7c32-8bec-3a6f4605be89');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7201-9da5-fe228d2a1566', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-00f9-7984-9445-8e960ae939c3', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-00fb-757c-8995-6b624fadcbca', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-00fd-799e-9ff5-3a110af0504e', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-00ff-75fc-aaa1-d21f6240df4d', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0102-7826-983e-4f8903e66cce', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0103-728d-9fdc-67763eb17f5d', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0106-78df-a341-047ab98131d2', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0109-7cf0-9e2e-794885bf972e', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-010c-7440-b612-cf70422fce01', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-010e-7104-9361-c75f95d49c78', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0115-7fc2-a253-2041352a2a9d', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0117-79e0-896b-876fcf7863f9', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-011a-77f0-b755-2f8092109c02', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-011c-78d2-a25b-07f9f9cf7ef1', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-011e-74b6-83e1-2c8c610c1933', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0120-7403-ae66-e604f75b85e1', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0123-7f8a-9a35-f3921e5582f9', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0125-75a6-853e-e36326d48f58', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0127-7e90-8d47-9ef92e67af88', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-012a-7ab9-8739-9d1d7d3ae560', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0130-7e9b-81d1-90aa310d8a92', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0132-7fd0-a6ee-7ff6c22cb4e1', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0135-7390-a9f1-84fd0377567f', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0136-7892-bcdd-5ecb2c91b85a', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-013a-7677-9772-58bad9415074', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-013d-758f-9a24-c48132c8b39d', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0140-71ab-a849-4695d8ae5a5b', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0143-7d72-83d7-d7e115d5fe65', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0145-7e75-b8c5-e8813cdd510d', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('019fd5f4-0147-7d31-8af9-6b65158bca2e', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7063-bfe7-a1593c35a16f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7785-af15-a2f8fbf984c4', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7e04-acfe-5ac21cc3059c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7c9d-a494-1abe041cf490', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-709a-9f41-1986d1325fcd', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7ffd-97a7-aabe3aba2dc6', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7a00-b17a-1ae4573b9621', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7730-89dd-d826f677e813', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7551-8919-d73f381a980a', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-73f9-b7c0-e78c334ae496', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-76e3-ac98-122580c36322', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-751b-b41a-bbe588255cf9', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7f5b-a879-4d90fa6e22d7', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7c50-a38c-3ebe96c8b8a5', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-725c-bc7f-e903eb525350', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7b8f-8cf8-10955a019ee2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-fff6-7c52-8a06-ddb2f2560518');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-76ea-8e6e-ecb492abaa26', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-fffa-7adb-8669-9ec0b9f6f9b4');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-79d5-836d-d76cf5571171', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c75-7bfe-8b29-9a52069a7c8c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-77a1-970e-ee6b5af042ed', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7009-aa91-669243b9ee47', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00d4-74c2-9ae6-b88d5d11849d');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7a33-8133-5e8681cb4897', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff7e-77ae-bc47-4cda913dd571');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-742f-9ba4-cb97ad69183e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff27-791c-ac1f-b828930d376f');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7854-bc7b-0c6be897b08b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff40-7fb1-a469-31606b5cb432');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7857-8c71-9ad7110edffe', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-74db-bf7c-18a7c2c340bf', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-707c-b7e8-73bab2d49966', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c75-73db-a514-d315d5299b18');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7200-87b4-a81a4ad8b5a6', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7f96-a237-226a4090cf1a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff4b-735c-8a49-4c2476c5a6fb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7b9d-84f9-eaaa1ca2f4d0', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff75-7441-bd4b-a1128a370805');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-717a-9ed7-a4f0c3c8732d', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-7efc-a7c6-b68fc8f0e2b8', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7ac7-83a3-377596c5305f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffdb-7d40-9884-feaa85a7513c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c83-777b-afc9-7780fcb85c1e', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c76-77aa-84c0-24c74a9091f5');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6bda-788b-9838-38a9b6508c6f', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6bda-74d2-9577-911488c389d1', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7c29-a2e1-7d7dd81b32cd', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff21-77ff-8385-c88940604f7f');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6bda-71d6-bbb2-838cbad300c5', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7740-b57a-4ef90626b24f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff71-7de0-a2f1-a5cd0ac1d61d');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-76f8-80c1-c06b5fcf20ea', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7000-9917-014dca3e5b05', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff65-7a3b-8c99-a03184ca4a1e');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc46-71db-9d44-bd05a853a478', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7af8-bdc4-330440991133', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0008-726f-92c2-bccb8cf12b3a');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-78c5-9d1d-76c46e228120', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffa0-7c71-b22c-0356a500a63c');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7401-8a03-76203e8a7ce3', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-000c-7a9a-8c48-c732f3525ccc');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7da4-af0d-c363a05ed227', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7327-aafe-54d6a7f9af14', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7e71-a092-38e8ad5f5207', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff39-745e-b575-bb76833d7004');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7c6e-83b0-33d89da69ab8', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00bf-7a2a-ba1e-a8b9499b619a');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7539-ac35-57a04180eb8b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff84-709a-81fa-931dc0807a66');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7ecc-8e30-7f5c13790a3b', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-788c-a0a3-7a8fc81703c4');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7125-9a08-e0179b31397f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff1b-7864-836d-673b58af90bf');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-760d-92f5-3829957a63e9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-788c-a0a3-7a8fc81703c4');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7716-98c4-eeaa7a9a755e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-014c-7db7-aca2-970b225fd63d');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-786b-b284-5d58bd1ba50a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-788c-a0a3-7a8fc81703c4');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7d91-bd37-3ef570d51ced', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00dd-7242-b415-2375198593a0');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7366-89a8-24f52ac5722a', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-7a13-8de0-3d9c0983d822');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-786b-b722-9252a5827bf2', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-7a13-8de0-3d9c0983d822');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-75b7-95aa-7ace3f11869b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-7a13-8de0-3d9c0983d822');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-75f8-b0a8-e03791bea306', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-7c37-979b-710c2582d515');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-744c-9b1e-e2b2e3ee979c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-7c37-979b-710c2582d515');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-771a-828d-54e3792c894d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffe8-7afa-b1b6-ce9bd45f31dc');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7c0f-b927-b88c27766d18', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-7c37-979b-710c2582d515');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7254-bf7a-1edfd711a43c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0003-7c2b-ab52-a555996c39a1');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7e7c-950c-6fb34e4ce055', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-7e64-b466-9205800b2f44');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7f1b-9210-99dfb9459d95', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0057-7b05-a448-4c9eeb2c3f3d');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-76d1-9d28-bf7cd8953847', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-004d-737d-ac0b-30720f70ebb3');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-730d-8017-a324e803d030', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-7e64-b466-9205800b2f44');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-709c-bbbc-0a0e5b3c934e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00ef-797a-bf10-f3b5198d4bcd');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7ca2-ae5e-c8ebc573e374', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-7e64-b466-9205800b2f44');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-780f-8741-f4ceb7fdac44', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffbb-7a26-950b-d61111f493f4');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7694-b58d-de8e271db2c4', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc40-7d27-aba0-adf8398fa14f');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7dfc-94ab-daa47ba85393', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc40-7d27-aba0-adf8398fa14f');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-78ec-b02b-77244e15d334', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7533-8963-0d46a92125cc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffff-7a2e-8412-af4e7af7449c');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc47-7a56-a713-f353f8868359', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc40-7d27-aba0-adf8398fa14f');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc4a-7135-8527-408adaa83174', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc4a-7068-a2c9-b5dd6d4e7566', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7c31-961c-e82531e86fbb', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0010-733e-8b78-65e2605f21b1');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc4a-78b6-a042-9c506e8fef8f', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-77a6-81f5-9ba1cccc1d92', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffbf-7af3-8ec3-765f6364cfde');
INSERT INTO ops.sys_role_button VALUES ('01a003d3-bc4a-794f-8675-d1f184392ef3', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003d3-bc40-7931-9692-4f51124b4adb');
INSERT INTO ops.sys_role_button VALUES ('01a00621-b8bc-7605-883b-b6591c02a735', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00621-b8b6-7b03-b3ed-354a3f25765b');
INSERT INTO ops.sys_role_button VALUES ('01a00621-b8bd-7765-8835-ee453274b583', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00621-b8b6-7b03-b3ed-354a3f25765b');
INSERT INTO ops.sys_role_button VALUES ('01a00621-b8bd-7b8a-9b35-8cbaa825dd85', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00621-b8b6-7b03-b3ed-354a3f25765b');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-704f-867b-883a1805520c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff6b-72bb-9784-eefbc06103b5');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-789c-b534-0d377c985923', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-005b-7a4c-8467-a24451d1795c');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7c6e-ad8c-e1635746a9da', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00cf-7701-befb-6ee6bf24d01e');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7442-bc44-32eeb282f325', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff7a-7416-9c8d-01850f1f03be');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7943-8931-ac61ae35fac3', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-007f-7d23-8c80-76f5d3498050');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-71c1-9011-62259be656bb', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0049-72bd-9979-c9a2b8ed52f7');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7627-8a0c-78024ec084ee', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7bbb-bbf2-25487360acf6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-006d-7468-8043-9f7cec6e04b3');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-787b-9fd1-35807334fd1a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff88-7269-a594-4517eed05982');
INSERT INTO ops.sys_role_button VALUES ('01a0003f-99a1-7b16-9036-3c1e4cf23992', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a0003f-9999-7acd-9965-5ac52e0ee107');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7591-bd9c-318896aa8d0c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffc8-761a-a630-14b693a34126');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7df1-99d1-110a958f60f5', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffa5-70b1-9e00-378e65b7fa07');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7dbe-9388-0d673a7290ea', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00e2-7ea1-a5e1-2f9894490bce');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7fd7-99ae-d82bcc326c95', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-015b-79f2-a094-85c6ca167ed5');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-78c2-ace5-7c26dc6255d4', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00ea-7516-bab4-5be9b6fef374');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7566-a105-cf794cef7412', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-005f-7b53-9c2f-5deada294629');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7914-a5a2-b472f74ea220', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffe0-7ccc-a40a-7d1913057831');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-7434-ba72-bf44054eda9e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffb6-7ccf-9c33-fce6dc3779da');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-78db-a1d1-e53230d56d62', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffcd-7d5f-9090-ab8e6ba5f5b8');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c1-77f2-9fcc-fc8e8c368eb4', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0077-74ac-94bd-ce2b80b0f4df');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-71db-8d69-709e2b4213df', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0068-7a46-9758-c0103a70bdbb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7857-8c8b-848f0ac1aa00', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0045-769a-9051-eb64d0aca660');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-751d-bd68-a2e1dc842519', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffb0-7366-83d9-70cd090ba9c2');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-70b7-baa5-a6ec6b369786', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7f67-bf03-cbbe89a0a1c4', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00b9-7773-9d85-698623a78690');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7b51-9741-478bc3ad6c0d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0064-7cf3-a5ec-ccec1ec7c48e');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7e0d-b942-affb8460e82b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0156-7d43-a26d-609880b46a3a');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-785b-b6b7-db7798901a6e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5698-76e9-83e3-18dc557b80f4');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-73c0-b073-f728f0d53aac', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff32-7aa0-8a47-cd9bed47737a');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7ce5-ba32-f75eebf42101', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0014-7e29-8cc9-74db7879d5f8');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7c62-a2a1-7a77d52b0106', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff45-79ab-b00b-f1fee1949a19');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7f92-830a-59b7ad5fa5f1', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffaa-7a6a-86fe-d929fb0deec8');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca77-7e76-9069-b57454cf949a', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7166-90da-f38893855d44', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff9a-7f91-8dae-83bd79cf0210');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7ae2-b3bf-6b55f731768f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-79d6-9c81-4316f60b5fb0', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0052-776f-8785-17e161c8f635');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7adf-99fd-e47c816d468a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7adc-b81c-839690f33bfb', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffe4-76fc-97d2-849465f49649');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7798-8ddb-653bb2733536', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7479-8767-6529b5a8b9fd', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffee-7cf7-ab38-026f0c12d6f8');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7d00-8a93-0c47d48fea59', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00d9-7236-84e0-87d653c29592');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7be6-a5eb-3bd5684521b1', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00c4-7411-8606-85dcbb10c89e');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-79a7-8b31-6864c3267fb6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0040-7d83-879b-7ed0704d54de');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-71dc-acf8-bfa8596d13c9', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7b54-898b-18185f219a49', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-77c8-b146-ed9337550cec', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7786-bedf-18573870a9ed', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ffc4-7b56-82a2-d744f9809034');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-7da8-9a3a-5828d0c2b8fb', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-00e6-7f2c-8c83-8415c5f346fb');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-759c-8eb7-43aa22beb40c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff93-764a-98fb-c83aa53a3213');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-74d2-ac0f-0e566eef5002', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0151-7ee9-8992-e8357cbae591');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7e0e-8512-ecf9203b6d78', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca6c-7faf-82a2-c64667cc1854');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7d36-ba01-c6c9ffa316dc', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca6c-7faf-82a2-c64667cc1854');
INSERT INTO ops.sys_role_button VALUES ('019fd606-81c2-77e5-929b-1e95066466b2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f3-ff8e-7914-829b-0e128d00244e');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-761f-8283-8f9614c459a6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca6c-7faf-82a2-c64667cc1854');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7ec6-852c-f6abad77756e', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca6c-75f1-bfbf-39ad6036965d');
INSERT INTO ops.sys_role_button VALUES ('cd658734-e8c8-41f5-97fb-8828a3c02b9d', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019ffe6f-b5ef-7c32-8bec-3a6f4605be89');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7b3c-9e0d-8f7e26a327c6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca6c-75f1-bfbf-39ad6036965d');
INSERT INTO ops.sys_role_button VALUES ('019fd658-1a6c-7f66-b7f2-b4b96f4bc61a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('019fd658-1a6e-703e-ae22-a5fbaa7219cb', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('019fd658-1a71-7fb8-b756-6c3b4f030718', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('019fd658-1a74-7ff9-a3e2-5afc6a6ba4ff', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('019fd658-1a76-7f3e-9997-781dd3935dde', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7945-8fe8-d89593e496ac', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca6c-75f1-bfbf-39ad6036965d');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7079-9ae1-e6785173a948', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca6c-74d4-895b-0a0e9c19c03c');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7cd9-8067-70c2610d033c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca6c-74d4-895b-0a0e9c19c03c');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-78f6-8b6e-bc22b8cee6ee', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca6c-74d4-895b-0a0e9c19c03c');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-70ea-ba4f-d8051182feb6', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca6c-7c82-8e8f-982336b13b34');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7b52-9c2f-273681ca474e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca6c-7c82-8e8f-982336b13b34');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca7e-7eae-b427-d083b3f23a35', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca6c-7c82-8e8f-982336b13b34');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca82-7773-a6cf-fb73138cbdb2', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca83-74ba-801f-213352913b1e', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-77cd-9ba0-feea18fd3281', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-78bf-87d1-d2b4ead221b9', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-75f7-a052-4738f4b0272a', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-7263-a9e3-d50c997b374a', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-7d1e-981a-b51168fa771d', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003f0-ca68-776e-a7c0-042a75e32603');
INSERT INTO ops.sys_role_button VALUES ('01a003f0-ca84-7b7d-bfbb-19c1ba219a5b', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003f0-ca6c-74a6-822e-119da2192d88');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6b7f-744e-9344-bf060f6a2192', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a008f2-6b79-7b44-a51f-415ebe8cdc9c');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6b80-78ca-b997-3c56339c97f9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a008f2-6b79-7b44-a51f-415ebe8cdc9c');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6b80-785a-9363-04c4223fbbe8', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a008f2-6b79-7b44-a51f-415ebe8cdc9c');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6bda-7aa5-8cd1-7884a4b7500a', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00');
INSERT INTO ops.sys_role_button VALUES ('01a008f2-6bda-7864-a142-3ed6f9f91ec7', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00');
INSERT INTO ops.sys_role_button VALUES ('25cfe8a4-c9a7-dfe4-e919-8dd462139270', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('5371d91f-ee20-16a7-7cba-c271f01b9b01', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('4c47ab17-1a69-9577-f4fb-f3fbd6265e83', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('ab4c0ee4-a3ff-a13e-aa77-52f6245f8bc4', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('8ec72a0f-2e5d-86c0-a061-54fc8735f7c2', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('019fdfc7-f39a-72b6-b4ff-966e59aa61d9', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5f4-0049-72bd-9979-c9a2b8ed52f7');
INSERT INTO ops.sys_role_button VALUES ('019fdfc7-f39b-7712-a5ee-e1a1f4fd00d2', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fdf4f-6c08-7ad5-b209-939c22b5428c');
INSERT INTO ops.sys_role_button VALUES ('8314fcd4-0a70-e6df-b8d9-c3d8ef41ee34', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('2d9f6220-b10a-417d-ba5a-97387eb4fdb7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('b4952caf-749b-b841-ad26-317db6ca022c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('d0838db9-fda8-b348-d8ca-07da3d58481c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('1c107190-9ee2-c17f-06a2-1d9d01ab02c9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('4a41bb02-bbfb-2ff8-9c45-7bfc9f8904d9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5698-76e9-83e3-18dc557b80f4');
INSERT INTO ops.sys_role_button VALUES ('c843f935-c2ce-d5de-39e5-6f4756cfb9e5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff1b-7864-836d-673b58af90bf');
INSERT INTO ops.sys_role_button VALUES ('73e23b21-bced-41df-6219-a9d7a981e773', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff21-77ff-8385-c88940604f7f');
INSERT INTO ops.sys_role_button VALUES ('2b89f6d1-2fe3-97fa-3515-e1eb13b864e6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff27-791c-ac1f-b828930d376f');
INSERT INTO ops.sys_role_button VALUES ('af5eda79-f580-ad04-8fe5-b5d382432f40', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff32-7aa0-8a47-cd9bed47737a');
INSERT INTO ops.sys_role_button VALUES ('d8f7a368-5bef-55c7-571c-59dbb6af4fef', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff39-745e-b575-bb76833d7004');
INSERT INTO ops.sys_role_button VALUES ('5dc2e930-05e5-a931-2d62-be1362c18399', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff40-7fb1-a469-31606b5cb432');
INSERT INTO ops.sys_role_button VALUES ('fe49a982-901f-0c6e-edfa-664d7bff9b7d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff45-79ab-b00b-f1fee1949a19');
INSERT INTO ops.sys_role_button VALUES ('56902d16-4c43-4c8e-0c43-f00101497deb', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff4b-735c-8a49-4c2476c5a6fb');
INSERT INTO ops.sys_role_button VALUES ('f416df1f-cd58-793d-79a2-b68db1ea8e27', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('4aad9de3-1c2a-e581-5d74-c779f4373720', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('3226bfa5-107a-c3b8-d97c-85a5d518a5e0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('047c08c2-d3be-5829-d68e-c0357ae26aa6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('4623e6e8-6101-a0d9-b1a8-96a73455be96', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff65-7a3b-8c99-a03184ca4a1e');
INSERT INTO ops.sys_role_button VALUES ('52f3da14-e062-bed2-eb87-faa5c1c7ea9c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff6b-72bb-9784-eefbc06103b5');
INSERT INTO ops.sys_role_button VALUES ('2ae927e9-650b-2f53-7881-72d3d9e05697', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff71-7de0-a2f1-a5cd0ac1d61d');
INSERT INTO ops.sys_role_button VALUES ('3b985c68-401d-ce2e-3a88-4c68c8d4f172', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff75-7441-bd4b-a1128a370805');
INSERT INTO ops.sys_role_button VALUES ('2e4ad889-24e8-32f4-9859-6b744b08f5af', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff7a-7416-9c8d-01850f1f03be');
INSERT INTO ops.sys_role_button VALUES ('21f50217-16a0-7e03-cdf8-f24c22dbcfeb', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff7e-77ae-bc47-4cda913dd571');
INSERT INTO ops.sys_role_button VALUES ('dcfef49c-9e6d-b938-c3af-e410281aedcb', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff84-709a-81fa-931dc0807a66');
INSERT INTO ops.sys_role_button VALUES ('63e26953-7fe9-6fd9-376c-4500ee1e4a00', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff88-7269-a594-4517eed05982');
INSERT INTO ops.sys_role_button VALUES ('d3701603-da5a-a903-a85e-dd97af66df72', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff8e-7914-829b-0e128d00244e');
INSERT INTO ops.sys_role_button VALUES ('71255622-e41b-919f-0fa2-500fac10d419', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff93-764a-98fb-c83aa53a3213');
INSERT INTO ops.sys_role_button VALUES ('51d722f2-8992-8962-35fa-784c08ac454c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ff9a-7f91-8dae-83bd79cf0210');
INSERT INTO ops.sys_role_button VALUES ('7c6e4ac1-1f58-48e6-a5ee-b12775a80d69', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffa0-7c71-b22c-0356a500a63c');
INSERT INTO ops.sys_role_button VALUES ('6aae8d63-d644-db22-e2af-91c5b5a9f7aa', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffa5-70b1-9e00-378e65b7fa07');
INSERT INTO ops.sys_role_button VALUES ('0d69195c-6082-bd1a-e4bd-82a5c04636e7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffaa-7a6a-86fe-d929fb0deec8');
INSERT INTO ops.sys_role_button VALUES ('443ff823-b49b-1489-201d-51ec19f274ee', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffb0-7366-83d9-70cd090ba9c2');
INSERT INTO ops.sys_role_button VALUES ('bb196956-1cfb-87c6-fc87-4ff734f4a02a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffb6-7ccf-9c33-fce6dc3779da');
INSERT INTO ops.sys_role_button VALUES ('f5e3bf7c-b445-fa92-c2a3-e3f41421776e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffbb-7a26-950b-d61111f493f4');
INSERT INTO ops.sys_role_button VALUES ('473e21da-f5bf-43b5-841c-9a8c51491a33', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffbf-7af3-8ec3-765f6364cfde');
INSERT INTO ops.sys_role_button VALUES ('c8f2d1bb-3094-657f-38ba-e72893f5e9fe', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffc4-7b56-82a2-d744f9809034');
INSERT INTO ops.sys_role_button VALUES ('7484cf56-07d7-ae00-fe82-24dc2daabb9d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffc8-761a-a630-14b693a34126');
INSERT INTO ops.sys_role_button VALUES ('2afa93d3-eb39-b0cb-c7a6-6fb28b5477cc', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffcd-7d5f-9090-ab8e6ba5f5b8');
INSERT INTO ops.sys_role_button VALUES ('8eb01c0d-729f-fd6b-aca5-61a4b946d601', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffe0-7ccc-a40a-7d1913057831');
INSERT INTO ops.sys_role_button VALUES ('1b9184ac-f8eb-beb0-e6f4-3ed4234d7ca1', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffe8-7afa-b1b6-ce9bd45f31dc');
INSERT INTO ops.sys_role_button VALUES ('52bf469f-9bf0-ad42-74af-4bc575955339', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffee-7cf7-ab38-026f0c12d6f8');
INSERT INTO ops.sys_role_button VALUES ('ff958cde-6145-089e-cfd1-10df2df42e86', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-fffa-7adb-8669-9ec0b9f6f9b4');
INSERT INTO ops.sys_role_button VALUES ('0c98c1c1-d7a6-487f-c2a0-abbb5c2e05a7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0003-7c2b-ab52-a555996c39a1');
INSERT INTO ops.sys_role_button VALUES ('2e3c8351-3dfb-0869-fdad-8e5621cb5f5b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-000c-7a9a-8c48-c732f3525ccc');
INSERT INTO ops.sys_role_button VALUES ('00312c46-c060-a3e2-2fa0-57e6a6acd1b0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0022-78b2-b252-7d7b36cdd2bb');
INSERT INTO ops.sys_role_button VALUES ('60e5d837-295e-4b49-afc3-e39df67d8759', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-002b-7b41-9482-18b6c25c8531');
INSERT INTO ops.sys_role_button VALUES ('12d5b049-8318-c27c-3097-8ba37f8336f4', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0034-7917-8ce4-74b457ffecc2');
INSERT INTO ops.sys_role_button VALUES ('148b0680-4fa4-5441-f8f6-daed66013e63', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0045-769a-9051-eb64d0aca660');
INSERT INTO ops.sys_role_button VALUES ('cd4b1a3f-c1ce-85a0-ee0f-950a620372f4', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-004d-737d-ac0b-30720f70ebb3');
INSERT INTO ops.sys_role_button VALUES ('d6257309-930d-00a0-3200-d263fe5e8447', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7fa7-8941-260d5b71e81e');
INSERT INTO ops.sys_role_button VALUES ('20781c08-877b-0408-a4c0-d61dcda0b8cc', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-78ac-8ad2-b7566c06c30a');
INSERT INTO ops.sys_role_button VALUES ('71d49765-598f-3ab1-2c91-6bc3a1543578', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-74a1-bce7-9ece67b42a38');
INSERT INTO ops.sys_role_button VALUES ('ef7048c6-a9df-2fe7-a6ca-aeb5dcbfeec3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7fa7-8859-c8861656f069');
INSERT INTO ops.sys_role_button VALUES ('72326cfc-4859-94f2-d247-db896d86db11', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7bd4-8970-b2d3adb560fc');
INSERT INTO ops.sys_role_button VALUES ('5fa98b0f-df4d-dd14-ef4c-456e90533895', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7fc2-8c15-ef42dc44c961');
INSERT INTO ops.sys_role_button VALUES ('fc8c1251-553c-0b7d-5856-071ba0247102', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-78a0-bf1c-86e828260f15');
INSERT INTO ops.sys_role_button VALUES ('57d08d65-20b3-96fe-8917-8a4c26b7a68f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7709-ba15-1ec971d5c38c');
INSERT INTO ops.sys_role_button VALUES ('95b098d6-9498-40a9-b0a0-b23d8b052401', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7060-a83a-c7e046abc1f3');
INSERT INTO ops.sys_role_button VALUES ('89685085-dc7e-8269-830f-56cfd0851edd', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e2-7c3b-8896-b8a7aaa96840');
INSERT INTO ops.sys_role_button VALUES ('9e67ed0c-65dd-fe33-cd90-52ffe678485e', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5698-76e9-83e3-18dc557b80f4');
INSERT INTO ops.sys_role_button VALUES ('c5430fdd-cc00-28bb-6ad4-d169c0f599db', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff1b-7864-836d-673b58af90bf');
INSERT INTO ops.sys_role_button VALUES ('01a000e9-1189-79cb-96bc-0ac05d41710f', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a000e9-1184-728e-83ff-9d5dfdfe2732');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-71fb-a486-21fb8cf6cfc6', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('9d69b278-6b88-dee8-5f4d-59b2964891b4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff21-77ff-8385-c88940604f7f');
INSERT INTO ops.sys_role_button VALUES ('282d6257-72b3-7ac7-0680-f978ac823f66', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff27-791c-ac1f-b828930d376f');
INSERT INTO ops.sys_role_button VALUES ('be64d789-2116-fbea-da1f-575535fdc4b9', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff32-7aa0-8a47-cd9bed47737a');
INSERT INTO ops.sys_role_button VALUES ('4462fb3b-fdbf-2a40-01b7-770bce632fc1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff39-745e-b575-bb76833d7004');
INSERT INTO ops.sys_role_button VALUES ('01a000e9-1189-77da-be36-f9dd7cb273e3', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a000e9-1184-728e-83ff-9d5dfdfe2732');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7c76-b32d-033a9008ba94', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7c70-bde2-5a65da21d626', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c73-7bc7-a4f4-26fdf3a65f9c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7b3c-a36c-b86d41d1a546', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c73-7a99-8a1d-9afeaf0681db');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7bca-bfd2-4d0aa806d9a2', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c73-7a99-8a1d-9afeaf0681db');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7d3c-851d-37864bf96004', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c73-7a99-8a1d-9afeaf0681db');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7f97-892d-22a286f0a4d6', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c73-7648-9abe-95a1635f07f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7398-a0b5-8e4b24d0b58e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c73-7648-9abe-95a1635f07f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7bfb-a029-3219373e5ba2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c73-7648-9abe-95a1635f07f5');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7a75-8974-20c9191e2bff', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-7fb8-bb1a-6b2470470d49');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7988-a48e-882f9db1fd91', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-7fb8-bb1a-6b2470470d49');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7198-9b8c-f5f2539bd73b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-7fb8-bb1a-6b2470470d49');
INSERT INTO ops.sys_role_button VALUES ('9a06c8e8-7856-ed31-2f07-534196776dc6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff40-7fb1-a469-31606b5cb432');
INSERT INTO ops.sys_role_button VALUES ('8e80c2e7-cdf7-1f70-0d9b-3ded0a8307ef', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff45-79ab-b00b-f1fee1949a19');
INSERT INTO ops.sys_role_button VALUES ('52a70346-9aa4-873e-1967-653d9df47fec', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff4b-735c-8a49-4c2476c5a6fb');
INSERT INTO ops.sys_role_button VALUES ('4181f774-b462-9fc2-6176-e9db17299564', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff51-7966-bbb7-893f1f5b5dec');
INSERT INTO ops.sys_role_button VALUES ('73b0e5ec-bb84-7e52-2e6e-b0b65f949ca6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff56-7621-961c-9f4ddbfe80ed');
INSERT INTO ops.sys_role_button VALUES ('a66efe81-6a1d-b75b-8e92-b1e2e27ef611', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff5b-76ca-a968-dd0851703a50');
INSERT INTO ops.sys_role_button VALUES ('03767f1c-2aaa-990b-3597-04536b837ffb', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff61-7350-b674-c45ce345a1c7');
INSERT INTO ops.sys_role_button VALUES ('cfe2a5c3-2bce-88c6-b6dd-eadb3e8194c3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff65-7a3b-8c99-a03184ca4a1e');
INSERT INTO ops.sys_role_button VALUES ('49df1072-166c-1008-32b2-3c8cdf4157fe', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff6b-72bb-9784-eefbc06103b5');
INSERT INTO ops.sys_role_button VALUES ('2df11328-e7c6-d5eb-f727-32fd5eb12960', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff71-7de0-a2f1-a5cd0ac1d61d');
INSERT INTO ops.sys_role_button VALUES ('10f42e38-8bee-bab0-f039-2697c8fa5cc1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff75-7441-bd4b-a1128a370805');
INSERT INTO ops.sys_role_button VALUES ('b4600686-dabf-e45c-f9cd-4e9cb4b932e4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff7a-7416-9c8d-01850f1f03be');
INSERT INTO ops.sys_role_button VALUES ('9a8d39fd-1221-4d6c-953a-91e6c9c4a102', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff7e-77ae-bc47-4cda913dd571');
INSERT INTO ops.sys_role_button VALUES ('d09bf699-a1e2-ee84-0d88-27fa126beeb5', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff84-709a-81fa-931dc0807a66');
INSERT INTO ops.sys_role_button VALUES ('498a6a59-2793-1ba4-9125-813bb405bfeb', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff88-7269-a594-4517eed05982');
INSERT INTO ops.sys_role_button VALUES ('c67ff75e-6b4c-345e-e42a-8fdb000b57cd', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff8e-7914-829b-0e128d00244e');
INSERT INTO ops.sys_role_button VALUES ('f1da4c1d-4cc4-b3f8-56ec-b92cdf10338c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff93-764a-98fb-c83aa53a3213');
INSERT INTO ops.sys_role_button VALUES ('d130f9d0-c52a-6147-20fb-fae6085bcb1b', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ff9a-7f91-8dae-83bd79cf0210');
INSERT INTO ops.sys_role_button VALUES ('b8dd585e-f765-eaa3-4371-a2b3103e2ef3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffa0-7c71-b22c-0356a500a63c');
INSERT INTO ops.sys_role_button VALUES ('7ac5b1d7-7188-945f-fcbb-15f9c9dfb434', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffa5-70b1-9e00-378e65b7fa07');
INSERT INTO ops.sys_role_button VALUES ('868b0758-c584-d0f3-e0ed-63303a6fd95d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffaa-7a6a-86fe-d929fb0deec8');
INSERT INTO ops.sys_role_button VALUES ('5366f96d-649a-27cf-dee5-cab3717e3795', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffb0-7366-83d9-70cd090ba9c2');
INSERT INTO ops.sys_role_button VALUES ('73705120-006c-44f0-0a83-e19ff48e8e0f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffb6-7ccf-9c33-fce6dc3779da');
INSERT INTO ops.sys_role_button VALUES ('0436cf48-334d-4962-eea9-133186b084fb', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffbb-7a26-950b-d61111f493f4');
INSERT INTO ops.sys_role_button VALUES ('83be4a7f-ae76-ef44-4042-4d3f3e63bd25', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffbf-7af3-8ec3-765f6364cfde');
INSERT INTO ops.sys_role_button VALUES ('dc839203-929a-e412-8fcd-6db4f14d2ce9', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffc4-7b56-82a2-d744f9809034');
INSERT INTO ops.sys_role_button VALUES ('fa65bd49-8043-e602-a566-2ae3e5478c37', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffc8-761a-a630-14b693a34126');
INSERT INTO ops.sys_role_button VALUES ('292fc13d-93a3-ba7e-6417-f2ec6cde840b', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffcd-7d5f-9090-ab8e6ba5f5b8');
INSERT INTO ops.sys_role_button VALUES ('44461c61-6fa3-9837-4274-3c38bc361290', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffe0-7ccc-a40a-7d1913057831');
INSERT INTO ops.sys_role_button VALUES ('448761e1-da3f-5ce7-5118-68fa4584b774', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffe8-7afa-b1b6-ce9bd45f31dc');
INSERT INTO ops.sys_role_button VALUES ('2b7374e1-cdc5-fd6a-2b13-83e4777bb92d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffee-7cf7-ab38-026f0c12d6f8');
INSERT INTO ops.sys_role_button VALUES ('38793170-7206-ff09-4ad4-5e80c1fe592d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-fffa-7adb-8669-9ec0b9f6f9b4');
INSERT INTO ops.sys_role_button VALUES ('5fa95e99-39c4-6cac-1732-d02aaeaca454', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0003-7c2b-ab52-a555996c39a1');
INSERT INTO ops.sys_role_button VALUES ('5ff96e85-89ee-acd6-5502-0d24fbd28ddf', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-000c-7a9a-8c48-c732f3525ccc');
INSERT INTO ops.sys_role_button VALUES ('cf47e053-19a8-5dbd-d147-174f9fc01028', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0022-78b2-b252-7d7b36cdd2bb');
INSERT INTO ops.sys_role_button VALUES ('d197e372-5f01-d6a3-2e4c-e72aab680651', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-002b-7b41-9482-18b6c25c8531');
INSERT INTO ops.sys_role_button VALUES ('c5618516-137c-6e32-eed8-590155b2f9e6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0034-7917-8ce4-74b457ffecc2');
INSERT INTO ops.sys_role_button VALUES ('fbe82d3f-5835-8f8d-1227-824aabd5fe3d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0045-769a-9051-eb64d0aca660');
INSERT INTO ops.sys_role_button VALUES ('f28b6b86-152a-da47-41ba-d7dcf25f9d45', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-004d-737d-ac0b-30720f70ebb3');
INSERT INTO ops.sys_role_button VALUES ('05913d61-e667-f411-4adb-2f29177e6582', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0057-7b05-a448-4c9eeb2c3f3d');
INSERT INTO ops.sys_role_button VALUES ('f89c9eb0-ea3d-5652-4fd9-cc04868f9af2', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-005b-7a4c-8467-a24451d1795c');
INSERT INTO ops.sys_role_button VALUES ('2747c2d7-122d-d9c4-3488-6a5841d3ac86', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-005f-7b53-9c2f-5deada294629');
INSERT INTO ops.sys_role_button VALUES ('ab20d5cd-ea68-3170-0e5e-03d3905c6f15', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0064-7cf3-a5ec-ccec1ec7c48e');
INSERT INTO ops.sys_role_button VALUES ('fcf71180-9c92-d172-74e7-5fc3fef593f4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-006d-7468-8043-9f7cec6e04b3');
INSERT INTO ops.sys_role_button VALUES ('8f18f374-ab82-2939-a4bf-8144471c74d2', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0077-74ac-94bd-ce2b80b0f4df');
INSERT INTO ops.sys_role_button VALUES ('badbb617-0a1e-03c0-daf1-8e2982b48408', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00b9-7773-9d85-698623a78690');
INSERT INTO ops.sys_role_button VALUES ('423189a9-b27c-9b79-303d-c236c4f5f6f1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00c4-7411-8606-85dcbb10c89e');
INSERT INTO ops.sys_role_button VALUES ('9b4098a1-7291-e4ab-c730-f97699ca96b5', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00cf-7701-befb-6ee6bf24d01e');
INSERT INTO ops.sys_role_button VALUES ('dde0ca05-95c4-90d2-273e-bc34bdcae04f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00d9-7236-84e0-87d653c29592');
INSERT INTO ops.sys_role_button VALUES ('aab85c37-c379-d689-b7e2-2c75d73310e9', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00e2-7ea1-a5e1-2f9894490bce');
INSERT INTO ops.sys_role_button VALUES ('679be39d-287c-8380-379a-2b06a3493954', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00ea-7516-bab4-5be9b6fef374');
INSERT INTO ops.sys_role_button VALUES ('372fcec2-d1db-f76a-a1c5-8edb1c653e38', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00ef-797a-bf10-f3b5198d4bcd');
INSERT INTO ops.sys_role_button VALUES ('f6843e15-66dc-deff-7b9c-443ea9312b3c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-007f-7d23-8c80-76f5d3498050');
INSERT INTO ops.sys_role_button VALUES ('7f6417a6-6fe9-9d53-e764-7ebcee0f4437', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0151-7ee9-8992-e8357cbae591');
INSERT INTO ops.sys_role_button VALUES ('14692fac-f988-1654-5a54-280ac3496bb1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-015b-79f2-a094-85c6ca167ed5');
INSERT INTO ops.sys_role_button VALUES ('ad6801ef-8d30-01b1-0552-b13a04b8a5e3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('95b6266f-0058-4989-2625-b564e36ed6e2', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdcb0-8a8b-79e5-ac30-29f461a2d4bc');
INSERT INTO ops.sys_role_button VALUES ('10bd12b6-58dd-c196-c3ed-65ed3e3ab5ef', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdcb0-8a91-7aab-887f-9c1d3565c47b');
INSERT INTO ops.sys_role_button VALUES ('90ce6bc6-cd57-3943-9448-2d4eab0fc8c1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00bf-7a2a-ba1e-a8b9499b619a');
INSERT INTO ops.sys_role_button VALUES ('852304a1-3413-01ca-738e-6828f8837273', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('40fd50bc-97e6-f8bc-dc8e-8acfc0f4e4ca', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00e6-7f2c-8c83-8415c5f346fb');
INSERT INTO ops.sys_role_button VALUES ('b9b37464-d666-0e69-48d0-d326987807a6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00d4-74c2-9ae6-b88d5d11849d');
INSERT INTO ops.sys_role_button VALUES ('24c7e2ef-8bc9-3cba-6def-b832ddd0703d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-00dd-7242-b415-2375198593a0');
INSERT INTO ops.sys_role_button VALUES ('a8fa6b11-a93e-4706-fbdc-c4bfda14158e', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0049-72bd-9979-c9a2b8ed52f7');
INSERT INTO ops.sys_role_button VALUES ('a17d885a-c283-9911-6418-efcd1a320953', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0040-7d83-879b-7ed0704d54de');
INSERT INTO ops.sys_role_button VALUES ('69f7f482-cd1d-2da6-1923-59fa9c4e4210', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0052-776f-8785-17e161c8f635');
INSERT INTO ops.sys_role_button VALUES ('c770dd04-f3c7-fcf5-c9f2-17cb0824e487', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0068-7a46-9758-c0103a70bdbb');
INSERT INTO ops.sys_role_button VALUES ('4315551f-fc64-6d49-6b47-f6ee12a106dc', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffe4-76fc-97d2-849465f49649');
INSERT INTO ops.sys_role_button VALUES ('3bd61076-9e18-2bc1-f697-9c18696281cf', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffdb-7d40-9884-feaa85a7513c');
INSERT INTO ops.sys_role_button VALUES ('ac93d178-3d85-6cf6-246e-1c4e1ad6b41a', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-fff6-7c52-8a06-ddb2f2560518');
INSERT INTO ops.sys_role_button VALUES ('d9233ce1-6fd3-623f-764a-bc22d036acc8', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f3-ffff-7a2e-8412-af4e7af7449c');
INSERT INTO ops.sys_role_button VALUES ('8a41e75d-d749-adab-b258-c6f1c261a218', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0008-726f-92c2-bccb8cf12b3a');
INSERT INTO ops.sys_role_button VALUES ('20f52555-adc1-a8e6-7a0b-4381061260e6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0010-733e-8b78-65e2605f21b1');
INSERT INTO ops.sys_role_button VALUES ('70dae3ac-a762-f911-abea-5f7d02b7d22b', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0014-7e29-8cc9-74db7879d5f8');
INSERT INTO ops.sys_role_button VALUES ('5d5250a3-fe9b-8111-2d22-2ff357d7028f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0156-7d43-a26d-609880b46a3a');
INSERT INTO ops.sys_role_button VALUES ('0b49a2ed-2593-eb78-4aa2-8600319c5ef6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-014c-7db7-aca2-970b225fd63d');
INSERT INTO ops.sys_role_button VALUES ('71291ec2-882b-a946-dddc-24a7cb9c65f0', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdf4f-6c05-7606-9d6f-b0361b7a5ddb');
INSERT INTO ops.sys_role_button VALUES ('66fe5bfd-4d12-9173-bbd6-2c9e61f9ace3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdf4f-6c08-7ad5-b209-939c22b5428c');
INSERT INTO ops.sys_role_button VALUES ('a32826a1-5b43-083e-2606-a83a42627e90', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdf4f-6c08-77e4-a43c-2476bcd1f6f4');
INSERT INTO ops.sys_role_button VALUES ('01a00103-ee6e-78c2-a94f-2fb7f101f8ec', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00103-ee67-7ab7-addf-f9bd311c97d2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c7f-7537-a223-374632a8cec0', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00103-ee6e-71e7-99e8-5b20f286ea97', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00103-ee67-7ab7-addf-f9bd311c97d2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7e32-a62e-2d3526e6e80d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-7973-88e7-f52ed40a58fd', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-73b6-b238-15723f530b5b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-7021-a1ac-be07f1a35ec2');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-7f4e-b005-a11fa5baefdc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-728d-82fd-53033c4d31a4', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7a0c-824e-fa49cd5831ee', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-72ef-b969-ba301b61ea89');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-70cf-935f-a4811d883c4f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-7dd1-bd6f-ece0eee1a5f9', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-77f5-97f4-07823b0b6510', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-72ef-b969-ba301b61ea89');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-794e-999e-9f8dd8212841', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-7514-a00b-68fe1cd163cb', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-73ba-8c96-0fa6eaeb6a18', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-72ef-b969-ba301b61ea89');
INSERT INTO ops.sys_role_button VALUES ('01a00327-5a8e-7433-88dc-22f0d74c42eb', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('01a00327-9638-7fb6-91fc-03781312be54', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-9634-719a-bca3-64a5d253fad9');
INSERT INTO ops.sys_role_button VALUES ('688627d5-4a49-9aa4-de52-8ccb961bde95', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ff1b-7864-836d-673b58af90bf');
INSERT INTO ops.sys_role_button VALUES ('350321d7-ae36-a2ea-6e8c-1212e0617aa9', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ff21-77ff-8385-c88940604f7f');
INSERT INTO ops.sys_role_button VALUES ('04fd9f94-8989-6875-5b7e-612298e3c213', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ff27-791c-ac1f-b828930d376f');
INSERT INTO ops.sys_role_button VALUES ('3744ce61-f888-254c-8e85-508c9904e683', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffb0-7366-83d9-70cd090ba9c2');
INSERT INTO ops.sys_role_button VALUES ('59721efc-08d2-929a-bcd9-3eb2b6de582d', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffe0-7ccc-a40a-7d1913057831');
INSERT INTO ops.sys_role_button VALUES ('63d56026-77dd-0ef4-bc60-4e2a3f7766bb', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffe8-7afa-b1b6-ce9bd45f31dc');
INSERT INTO ops.sys_role_button VALUES ('0c8bc0bb-2773-3301-934d-d4ff9037777f', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffee-7cf7-ab38-026f0c12d6f8');
INSERT INTO ops.sys_role_button VALUES ('6b4524d3-aee7-3c2e-1e1d-fd3ffdc01a4d', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fdcb0-8a8b-79e5-ac30-29f461a2d4bc');
INSERT INTO ops.sys_role_button VALUES ('aa5e0abd-7d74-a8ef-9a62-e8ecb5dacbc1', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f4-0049-72bd-9979-c9a2b8ed52f7');
INSERT INTO ops.sys_role_button VALUES ('7949ee26-bcbe-1248-a3aa-840aa2f7f809', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f4-0068-7a46-9758-c0103a70bdbb');
INSERT INTO ops.sys_role_button VALUES ('a40d0c0b-7250-48c8-0c64-8fd9b0489b85', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffe4-76fc-97d2-849465f49649');
INSERT INTO ops.sys_role_button VALUES ('1d6ce742-e343-596d-3a6b-fc54fa18f3fa', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5f3-ffdb-7d40-9884-feaa85a7513c');
INSERT INTO ops.sys_role_button VALUES ('12ba1414-1955-35f5-4717-169f901a85b2', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fdf4f-6c05-7606-9d6f-b0361b7a5ddb');
INSERT INTO ops.sys_role_button VALUES ('496f6950-8818-6452-4e79-5b159b136aef', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fdf4f-6c08-7ad5-b209-939c22b5428c');
INSERT INTO ops.sys_role_button VALUES ('a0c2d5dc-c528-dfba-57bc-c530429dd2e8', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fdf4f-6c08-77e4-a43c-2476bcd1f6f4');
INSERT INTO ops.sys_role_button VALUES ('a2282bc8-1a27-b3c2-1fe8-718e5df7f191', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0057-7b05-a448-4c9eeb2c3f3d');
INSERT INTO ops.sys_role_button VALUES ('c41a8866-657a-c813-24d7-e91e444a49c8', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-005b-7a4c-8467-a24451d1795c');
INSERT INTO ops.sys_role_button VALUES ('dc21c06d-de19-b502-94bf-cbab5776a7f7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-005f-7b53-9c2f-5deada294629');
INSERT INTO ops.sys_role_button VALUES ('0001a647-75eb-631f-072a-1cb4e11ed7e0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0064-7cf3-a5ec-ccec1ec7c48e');
INSERT INTO ops.sys_role_button VALUES ('287c8731-2c90-3654-142a-7744fb881ceb', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-006d-7468-8043-9f7cec6e04b3');
INSERT INTO ops.sys_role_button VALUES ('95e5dd5b-0232-d2cb-47fc-1cf00896bf5d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0077-74ac-94bd-ce2b80b0f4df');
INSERT INTO ops.sys_role_button VALUES ('26c36df7-2ba6-f249-3de9-cdff86656210', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00b9-7773-9d85-698623a78690');
INSERT INTO ops.sys_role_button VALUES ('0c27c4e8-39a1-0453-d4cb-40848661ed77', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00c4-7411-8606-85dcbb10c89e');
INSERT INTO ops.sys_role_button VALUES ('bed69e8f-5d19-9c4b-b094-ec9989d7ead6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00cf-7701-befb-6ee6bf24d01e');
INSERT INTO ops.sys_role_button VALUES ('66eb119e-41db-82bf-d435-34362cfd5895', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00d9-7236-84e0-87d653c29592');
INSERT INTO ops.sys_role_button VALUES ('83dd0f82-e5b8-4982-67ef-84972c6e8362', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00e2-7ea1-a5e1-2f9894490bce');
INSERT INTO ops.sys_role_button VALUES ('889c512c-1d7d-1976-ad63-09c982dbba49', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00ea-7516-bab4-5be9b6fef374');
INSERT INTO ops.sys_role_button VALUES ('6e424377-5021-d322-665b-abee02427361', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00ef-797a-bf10-f3b5198d4bcd');
INSERT INTO ops.sys_role_button VALUES ('44e9e16f-d4c9-730a-bc01-3cb455f4914b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-007f-7d23-8c80-76f5d3498050');
INSERT INTO ops.sys_role_button VALUES ('0318a3a6-91df-b235-0323-9c07af8ab6f9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0151-7ee9-8992-e8357cbae591');
INSERT INTO ops.sys_role_button VALUES ('70091332-bb93-40e6-1dbf-8e3982ff1c0a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-015b-79f2-a094-85c6ca167ed5');
INSERT INTO ops.sys_role_button VALUES ('2f0fe06f-4af1-72ec-feca-e5b13be3d732', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd658-1a3d-7a96-b07b-463a8a518139');
INSERT INTO ops.sys_role_button VALUES ('afdc92b8-e36c-1d3e-97e4-9779574cbea7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00bf-7a2a-ba1e-a8b9499b619a');
INSERT INTO ops.sys_role_button VALUES ('47f82a17-6247-81cf-95eb-3c5c21343676', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00ca-7bc9-a277-55947aff32bd');
INSERT INTO ops.sys_role_button VALUES ('fb7c91a1-a2d1-db0b-50cb-7fa13a3b597c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00e6-7f2c-8c83-8415c5f346fb');
INSERT INTO ops.sys_role_button VALUES ('ffdcd387-aa1c-e324-db70-89bd3ef0412d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00d4-74c2-9ae6-b88d5d11849d');
INSERT INTO ops.sys_role_button VALUES ('267328cc-fbad-676f-774f-f3391cfbdd99', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-00dd-7242-b415-2375198593a0');
INSERT INTO ops.sys_role_button VALUES ('db9240db-b966-f773-a3fd-f4a124a5a4e7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0049-72bd-9979-c9a2b8ed52f7');
INSERT INTO ops.sys_role_button VALUES ('830beef3-acf7-11f7-2ea8-042f3e0e6a7c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0040-7d83-879b-7ed0704d54de');
INSERT INTO ops.sys_role_button VALUES ('1f343d42-ea8d-5c7a-f91c-062604f136ca', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0052-776f-8785-17e161c8f635');
INSERT INTO ops.sys_role_button VALUES ('abb24b10-5cd3-b6ca-fe28-c1800964eab3', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0068-7a46-9758-c0103a70bdbb');
INSERT INTO ops.sys_role_button VALUES ('b485e1cf-b56e-e21b-0021-c6b195741315', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffe4-76fc-97d2-849465f49649');
INSERT INTO ops.sys_role_button VALUES ('c28b8c4e-74bc-0c76-c08e-c062ce96cebf', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffdb-7d40-9884-feaa85a7513c');
INSERT INTO ops.sys_role_button VALUES ('5db11f2f-f2c5-05f0-bf3c-b207dda4c2cb', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-fff6-7c52-8a06-ddb2f2560518');
INSERT INTO ops.sys_role_button VALUES ('8d9877ec-757a-e327-28da-77410e217810', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f3-ffff-7a2e-8412-af4e7af7449c');
INSERT INTO ops.sys_role_button VALUES ('8a248436-1eb8-809f-a0df-ee99c79cfd43', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0008-726f-92c2-bccb8cf12b3a');
INSERT INTO ops.sys_role_button VALUES ('797805d3-e2fd-d5b0-a44f-8a29cbde611b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0010-733e-8b78-65e2605f21b1');
INSERT INTO ops.sys_role_button VALUES ('21409d96-973c-c3ef-04e1-f83394b14d98', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0014-7e29-8cc9-74db7879d5f8');
INSERT INTO ops.sys_role_button VALUES ('4739dd46-777c-7059-c464-e45e26a4e3a0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0156-7d43-a26d-609880b46a3a');
INSERT INTO ops.sys_role_button VALUES ('281f6484-ad0f-4ee7-8b90-4cfc47c36461', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-014c-7db7-aca2-970b225fd63d');
INSERT INTO ops.sys_role_button VALUES ('d04e6c5b-ccf6-2e67-f880-4a8c96f6ea0c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ff015-d957-7349-b988-16e46b953ec7');
INSERT INTO ops.sys_role_button VALUES ('11a05dc1-8384-282a-e02d-2fd5da01c362', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ff015-d974-7d61-816c-48bb179b09a9');
INSERT INTO ops.sys_role_button VALUES ('0e739f36-7fba-ee94-c8fa-273e50cfeecd', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ed-7c14-9cf4-245d0b19c5db');
INSERT INTO ops.sys_role_button VALUES ('11e6b090-5a0c-afac-f1c8-b18c8800ec7a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-75af-9d9d-fb4afd65d5c7');
INSERT INTO ops.sys_role_button VALUES ('0546fea6-fbbf-90dd-c648-d5e022c7b43e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-79a0-8e3d-60a6a557e8fe');
INSERT INTO ops.sys_role_button VALUES ('1617b81a-52d7-41ee-642c-78777f0deb24', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7918-8ac1-7c777cf59a9b');
INSERT INTO ops.sys_role_button VALUES ('47c4208d-b089-b110-c161-73808dc32a90', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-77dc-880d-59679e3d1ff8');
INSERT INTO ops.sys_role_button VALUES ('6b5f483d-0826-4361-5484-94abfa646e67', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7b49-b04d-d3d5c1237f8d');
INSERT INTO ops.sys_role_button VALUES ('81272ffd-9a16-4d1c-418c-82379cca002b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7acd-8127-9958d8b113dd');
INSERT INTO ops.sys_role_button VALUES ('e3337962-c367-c613-5354-4e945b9037d6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7456-9948-ad2b11b2d382');
INSERT INTO ops.sys_role_button VALUES ('fbfdba52-3cad-522c-ea98-d6bcbada879d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-746d-b7d5-15045e0fc5b6');
INSERT INTO ops.sys_role_button VALUES ('9b36674d-3738-69e9-cb44-23663aed0b4c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-76d5-b598-3905148f0875');
INSERT INTO ops.sys_role_button VALUES ('41e31b2c-74cb-7af2-2732-e7c0e642079d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-76c2-89b6-6ab7176ec64a');
INSERT INTO ops.sys_role_button VALUES ('ebf8b808-1221-c297-4b0d-a498eb7e2499', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7082-b382-4c747ba98e40');
INSERT INTO ops.sys_role_button VALUES ('15c1d30d-fec6-9714-95b9-b4aa07d89301', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-72eb-8cba-dfe5b9055f8b');
INSERT INTO ops.sys_role_button VALUES ('837a4306-4506-ed9c-500f-c51a2fc25312', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-73a1-857b-49bfb1405291');
INSERT INTO ops.sys_role_button VALUES ('836c94ec-789a-5beb-492a-f57ca5ea5c28', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5ef-7c32-8bec-3a6f4605be89');
INSERT INTO ops.sys_role_button VALUES ('db3800e7-876a-2608-959a-7c40772a8974', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a000e9-1184-728e-83ff-9d5dfdfe2732');
INSERT INTO ops.sys_role_button VALUES ('79db3ee9-ec3f-d015-e7b8-e781455831af', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00103-ee67-7ab7-addf-f9bd311c97d2');
INSERT INTO ops.sys_role_button VALUES ('2f608df2-cdd1-7d96-0b16-eef0a8ebc3b5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-5a8a-7005-bb15-6c0a6a24f4c6');
INSERT INTO ops.sys_role_button VALUES ('b9367bd3-c2ca-4230-a027-3bd79ab30ecf', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-5a8a-761f-a10c-4b64b580ee29');
INSERT INTO ops.sys_role_button VALUES ('e27bc67c-edf8-3232-1b03-3d7660c0d349', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-5a8a-709a-9715-40759b05e0e4');
INSERT INTO ops.sys_role_button VALUES ('fab3c11b-00aa-49e8-3526-dedc946e0e49', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-5a8a-7c37-ab16-caaa07fd4f21');
INSERT INTO ops.sys_role_button VALUES ('03bbf255-9efb-dfb3-4272-9e9aa618844c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00350-4a6f-775a-8fd2-e1e33dfbd911');
INSERT INTO ops.sys_role_button VALUES ('037a40b2-71e9-e990-ba27-9588af55c3ce', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fdf4f-6c08-7ad5-b209-939c22b5428c');
INSERT INTO ops.sys_role_button VALUES ('a018222c-f004-8c18-83af-fe22260f299f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fdf4f-6c08-77e4-a43c-2476bcd1f6f4');
INSERT INTO ops.sys_role_button VALUES ('dab23134-10b3-052f-ce11-9e03fa30c31e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fdf4f-6c05-7606-9d6f-b0361b7a5ddb');
INSERT INTO ops.sys_role_button VALUES ('5ae9fcc3-b170-42a8-e95f-a53bc0d3fa49', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-9634-719a-bca3-64a5d253fad9');
INSERT INTO ops.sys_role_button VALUES ('7f95fb87-eaf0-2531-e6b5-cc80a3df5717', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a0003f-9999-7acd-9965-5ac52e0ee107');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7bc8-b566-e699443059a8', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-79a4-ace3-9c78ec267c4c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7367-be6f-335a2d2f83c4', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-79a4-ace3-9c78ec267c4c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7bcf-a652-5f0704cfc6e8', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-79a4-ace3-9c78ec267c4c');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7738-bc42-e392a104e13d', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-7440-9330-a4ea83c084e2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7f4a-bb74-38b44b1aac88', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-7440-9330-a4ea83c084e2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7444-92e8-b644fb9bfc01', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-7440-9330-a4ea83c084e2');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7266-9268-b6895040894b', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c74-79df-8330-a906ed9d5b22');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-7be8-bd4c-af938796c5b9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c74-79df-8330-a906ed9d5b22');
INSERT INTO ops.sys_role_button VALUES ('01a00441-5c80-78a0-93c3-9af050a093f8', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c74-79df-8330-a906ed9d5b22');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_role_menu; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7503-9fbd-946ddbb8b13f', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7933-896e-4cf39a942346', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7daa-9ff4-a6fa14d3b530', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7f36-9d3a-66c627a1f29a', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7da4-ad33-bf090bf5782e', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7e0c-9c91-9b87d3767d12', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7127-98da-7afd50f52f54', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-72bb-8834-b9baff4ecf88', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-79ad-832d-97c2cb268828', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7759-ba1a-baaa68f9d0d4', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7bd0-a628-bdd662931991', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-73a9-88f6-f83e7306f2c3', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7b76-9c1d-b650cd17ee7a', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7512-91ad-12a28b851f85', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7368-a46a-00d44a01e2e6', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7ffc-b76d-27d31c7c8839', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc43-7bac-ae44-dd0df10b500b', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-724a-8b89-e25683c821e5', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7920-9c3a-ac3191182545', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-79d9-bf66-c51c6818696c', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-75f2-bdd0-bfff404c9fee', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-71a1-b12d-5477d7ade876', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7fde-8925-a0a44c4dc8dc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550a-7d76-99e9-7fb9057fed63', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('019ff015-d941-7a87-91ba-65d5b3ff6643', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ff015-d932-7e5a-afc7-e96442821270');
INSERT INTO ops.sys_role_menu VALUES ('019fd5dc-550c-7034-b423-beafda63493a', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7171-8088-acb5233c4f8f', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7685-80f4-d347b5ad9875', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-1861-7027-bdff-32c5262885e4', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-1861-7730-8302-49d547dad9d7', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-1861-7465-a34a-3b0b2f2015a5', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-1861-7991-ae90-55fcbb4d3088', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-1861-7131-b65a-0cab28353fb1', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-18bb-7ee1-b511-2b8e56c0346c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-18bb-7bc2-8435-021051ffeace', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-18bb-7870-8465-ee705b72875c', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7d3c-bd8f-185246e57497', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('019ff015-d953-78c6-872a-1668197139f6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ff015-d932-7e5a-afc7-e96442821270');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-18bb-7a50-8a44-48c7be609e7d', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f2-18bb-7488-b7e4-4a18a730069c', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6811-7a68-a8dd-7d76912a80a7', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-72ae-af93-d97e369e9c04', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7512-81c9-f1e1e9ff3a48', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7a19-a36c-125284c99d76', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-0021-765c-8830-91807cd53628', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5f4-0019-7526-b0c0-f03a30f69e2b');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7bd1-bf2d-e42e1f5f09a6', '019fd5dc-5509-7de5-8ad9-4e085677e466', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7b78-afd9-d28d92e35fd5', '019fd5dc-5509-7536-9965-e4a923dd33b6', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-73b6-aa2a-1c39f53f5322', '019fd5dc-5509-7108-b319-c61ea6ca0682', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-00f5-7aa6-96c1-160eaf7bbba9', '019fd5dc-550a-74af-b487-c605e0524dc2', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-00f7-7881-871b-ff2bc4f97ad0', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-0111-7dc0-9b9e-29150b4783d5', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-0113-773b-8190-92cb0d588772', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-012c-7826-9ca5-2f301fe07479', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd5f4-012d-7447-884a-b5e2d748122c', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-739d-9e86-1654aec9bb43', '019fd5dc-5509-7ee1-97f5-9f54bcadc895', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-79eb-a3af-569ec3f4eec5', '019fd5dc-5509-7d38-af2d-721cd77d0df8', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7810-ab55-c5ef643cc48d', '019fd5dc-5509-7cfe-b814-0d76c14013cd', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7e75-ae28-0a7373d110d2', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7b6b-9ec1-bbd82b9fe1b7', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-70f2-862e-f62dbe6a9e06', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7312-a5f4-611065fc0a2d', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7774-9b81-3ded00baa6de', '019fd5dc-550a-74af-b487-c605e0524dc2', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-77dd-8d92-6fb919e85f1f', '019fd5dc-550a-7e00-bf48-9bdad1801952', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7861-8bf1-2bd52e0f5025', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-7f65-a938-ead11c3c738a', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7f62-b63c-efa1573ce155', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('019ff58d-6813-707d-bdbe-57e6fad1cdb0', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('019fdf60-2727-7cf6-97ed-58b6b89c1b28', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5dc-51d8-7e86-9643-08356660d83c');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f2-71b7-814c-3f4682f55756', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7c5e-9e46-77bef30de767', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019fe029-d89c-7081-bc17-42571d45e32b', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fe029-d89a-7416-9260-f9ab51194f91');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7531-a616-f63dce47f1a9', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7e9b-9792-a9886de40577', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7425-88b6-2dd08a44ba9e', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7791-ab22-89039d4c211f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7f32-937a-bbdc682809f4', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7bd1-945c-a8d53af1471a', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7abb-91fb-d90d8e5c9a9f', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7b15-8aec-e2458714e0e6', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7de1-a77a-bd04ffcf4298', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7f65-990d-822ff01242e8', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7ff0-bb0c-1b1fe5c9e9f1', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7a38-96e9-971b01aad1b7', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7317-b7c0-f1f8a743d70c', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7801-946b-5323ab815289', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-71b4-8566-d14d6666b061', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7862-8de3-73d00926f72f', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-7d1a-b71b-69897f4f7157', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('019ffe6f-b5f4-77c5-87b2-efca6c33e32d', '019fd5dc-5509-753b-91cb-03baaaa2517b', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('01a000ba-ac8c-750e-ba29-22b4c9b6101d', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a000ba-ac7d-7a91-8d53-00bed2c4b73a');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7827-a1bd-0f037ccef8b2', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-71fc-8bde-322e23752dc4', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7774-9e91-0ed5b1a37dbd', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-7e54-89a3-fc2dfdecf590', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc38-7749-9a97-f62f7b6471a7');
INSERT INTO ops.sys_role_menu VALUES ('01a003d3-bc44-78fb-a660-f29b9167df47', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7c-76f6-b17f-f1e69b5d7e0d', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-7233-a52a-07caf545f620', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-7132-b2ac-3dcab244cae6', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-70dc-823e-86708b32bb21', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-78af-b363-7aa798c1115d', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-7466-90a2-88a952ad1d1b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6b7d-7753-9db7-d115ea4065a2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a008f2-6b72-7609-abe9-0dae1e9572c0');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bc-7619-b0c0-6e853e390d5a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-7eec-9055-4f5b6aaaa29d');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-7e3a-8660-e7be1f1533aa', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bc-779b-8cac-113aa74ccb52', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-7192-bb54-c37eb0729def');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bc-7393-a811-aaf8b887512b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-7aa1-bb1c-5532bac6fc97');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-7525-8250-dbd4ccc3d6d7', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca70-7097-bdba-c7b268f1b709', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7ab3-a817-32d98e268fa7', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51da-7686-8064-c7c8019205b3');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7cae-ae35-2ce35e1cdb51', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7485-8d81-fd1314ebcaae', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-7668-93fe-89b57b6f90b3');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7872-b50b-f377ff284231', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-73b2-9a3e-5074f82249ed', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7f2d-9d00-1499d25e5e4e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7408-97d3-e6ed0a92ecc4');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-79b3-808e-07d4bc20101f', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7a61-87f0-e47eb010c76a', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7560-9e19-24ad35f995d2', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7ede-9f84-e674cb61474b', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-72c9-b7ad-bf3953b5e052', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-779a-802a-65fa11a5498d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-55b7-7f05-be96-93f5ef62a685');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7c87-9912-76ce52268f6d', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-72fd-80b9-64a0e406efad', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d7-754a-839d-7f7265c42c50');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-716b-aeda-afca217bff97', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-78f4-90f5-9c252d21b401', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-776c-8cb6-74903e6501d6');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-759a-85c3-cbbaeedbfd1a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7836-919a-2ba5a0a9465a', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-73e5-8cb9-0d4d8694d3d9');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7908-9872-28b033cba872', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7d65-b591-d1ab8a90dfde', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-556e-73cb-a804-9862819894b3');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-73c5-a8db-254f52bb8121', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca57-79a9-ad2a-231407159c98');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7bf8-bac5-f762db3c7c7e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-7794-aee4-b73d0c73b3d0');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7545-a6bb-385cc3b41db6', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7eaa-bd22-b3ca386fb084');
INSERT INTO ops.sys_role_menu VALUES ('01a003f0-ca71-7bb2-ad61-e90c8339d9ae', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a003f0-ca5e-7d7f-9f09-a53d1828e380');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-779d-8264-7b6051e7e4ae', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-7fd0-95e0-f78ccfe23ae6', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-7825-863b-b7f246664439', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-7293-b1a2-188f550901e7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7633-b1c6-4709097daf52', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7977-96dd-ff09b640a869');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-703c-92f2-baf7423bb466', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-7aab-ad65-eb8bb85a40f2');
INSERT INTO ops.sys_role_menu VALUES ('01a008f2-6bd9-728c-a76b-54f0d3d10009', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a008f2-6bd6-71f7-9014-82fbcdf47619');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7230-a6a4-d791de3ccc13', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-55f1-7702-b666-f4431fa21c2a');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7efd-9534-2e7e3b6f67cc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-7e86-9643-08356660d83c');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-78c6-9f51-cf0872c1f87d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7588-8192-5601499e6b9e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-7c4c-8700-999b9e498a58');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7b3d-9b35-be928a346566', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d4-758c-80f7-0221d64ec698');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-729b-b25c-bd9d44ed575c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7f2f-80c1-2bba8d5d1592');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7242-a890-20ecc90d9895', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5527-7837-b567-8b4b2e21ba59');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-735f-9f0c-9cdfa31cc881', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7e0e-a59c-b2e6adda7747');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-79d5-ac32-0c47eba50342', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51db-7e02-9a2f-9c209fd28445');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-77f6-bbfc-465446947296', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5527-7bd0-8fad-057ac3513b9c');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7596-a3e2-55bf0f8846ca', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d8-72c6-a20a-013c193412ff');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7844-8f36-9a53458f6119', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-73ab-b38d-ea4c880f7fa1');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-75ea-baa1-7316f325b8e4', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-780c-be52-dc2f774aadd9');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7ff2-9cea-54d4f5aadd93', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7ee6-a120-45b73eda04f3');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7731-843a-40406dd78fc9', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d6-799e-8336-a148f5fa3109');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7367-b2db-86e52efb7a7f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7fbe-b35c-c9a772bbc268');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7375-bc22-7f95da1cb8cc', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5591-71f5-9e46-03cba2242165');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7bb8-b5de-a5226e11db14', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-713d-9770-7262e3cc836e');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7c25-9387-725b3e845b15', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5559-79fe-95bc-f9e59bbab203');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-79f6-80e3-ac3f58c1029f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5773-7c2b-a8df-80ba0650d3ab');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-72b9-b644-ecfe05011d57', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d9-7701-8c2c-835ecfff3e60');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7178-ba6d-b0dce7c4e906', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-7810-b631-aaf31236f6af');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7e02-b2b5-431d9c660ac2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-7fca-92b3-22448bd8d8db');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7f7a-8356-7279ef7bec95', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51da-77e7-a55d-16bea9adab3b');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7d40-b6ed-a3d2fad33651', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5527-7ec4-ac74-f8ded022d695');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7de3-a2d9-54f3d5b76e3d', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5527-7a99-b861-9a8a10988fbc');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7091-b173-5886cef776ab', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-77b6-af02-f3d418647ce7');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7efe-8fa8-483f5a60d1af', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-5527-7cb5-86b8-8cc9741427a1');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81bd-7281-87b2-77a99c597d2e', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03');
INSERT INTO ops.sys_role_menu VALUES ('019fd606-81be-795c-82c8-71e7f6aeab53', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd5dc-51d5-7a32-ae2d-bfdf7d34066b');
INSERT INTO ops.sys_role_menu VALUES ('019fd657-e0f3-7401-869e-8f2c5916d9bd', '019fd5dc-5509-788e-b885-f37e68d31ddc', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('019fd657-e0f5-7093-a654-2b25719edb55', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('019fd657-e0f5-7fcd-8335-329feb805a77', '019fd5dc-550a-71c8-8224-10b5ceb6d875', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('019fd657-e0f5-710d-9e0c-3dc67f8a231a', '019fd5dc-5509-78ec-ba54-c221b77dc53f', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('019fd657-e0f5-700f-8c6b-7b5e6278b69c', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('019ff87b-1683-75d7-b203-a7b6ca259e46', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ff87b-1682-738e-9900-b655a1f6bfb0');
INSERT INTO ops.sys_role_menu VALUES ('019ff87b-1683-7669-9585-91eb1b2b323f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019ff87b-1682-738e-9900-b655a1f6bfb0');
INSERT INTO ops.sys_role_menu VALUES ('019ffe74-bbd9-754e-b2a0-33c39087c808', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('019ffe74-bbd9-7c2b-9405-cbc80f0d3260', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe74-bbd9-78fa-80d3-4b5feda6cbd9', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('019ffe74-bbd9-71e8-9df4-c532dc977068', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-7575-83da-34a94e87490c', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-7be9-986c-ed0a84c7ff4c', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-7052-8e91-14bb863e82f4', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-7269-9b10-ac7d161cafbc', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-798f-af0e-139089402377', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('019ff99d-d937-7e25-a9e8-12e80ed57578', '019fd5dc-5508-7434-9963-6ae87dff963c', '14fd60c2-2d74-400e-b68e-283cccf51649');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c79-7db9-9ce8-f234f13c6557', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('019ff99d-d93c-7dc4-b2bc-65e1b4620337', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '14fd60c2-2d74-400e-b68e-283cccf51649');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7b99-992d-4de41a42a629', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a000e9-1187-7fab-9404-9c2b95040fc3', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7721-b97a-edf4b991cdf1', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7d3c-a8dc-ee069d4d4cb0', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-789b-a7f4-492f866d24ec', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-70c0-a35b-d4dc994de688', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7fd1-b426-9dbc645605c7', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c66-743a-ac33-db8b353c65e6');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-796c-9fc1-821739a9775e', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('ac09cf3c-a7bd-2664-22c1-bb61e072bc35', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5591-71f5-9e46-03cba2242165');
INSERT INTO ops.sys_role_menu VALUES ('d74ec9a5-959c-2e6f-bda5-47d22b9fd1c8', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-799e-8336-a148f5fa3109');
INSERT INTO ops.sys_role_menu VALUES ('1cc8c468-a1d6-a9c8-b463-aabdb8787c60', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7fbe-b35c-c9a772bbc268');
INSERT INTO ops.sys_role_menu VALUES ('019fdf4f-6c09-7ccf-b53c-7272b41fdf8b', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03');
INSERT INTO ops.sys_role_menu VALUES ('e9a3a762-5b13-f405-ff28-c9c4dd102b77', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-713d-9770-7262e3cc836e');
INSERT INTO ops.sys_role_menu VALUES ('01e81790-833c-a712-819e-172a08e8687c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('d5d173b4-cd68-c1d5-a787-74d93dbbabb5', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d4-758c-80f7-0221d64ec698');
INSERT INTO ops.sys_role_menu VALUES ('700aee48-f84a-60c3-d4a5-073df529876f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-7794-aee4-b73d0c73b3d0');
INSERT INTO ops.sys_role_menu VALUES ('1753efd1-700a-c51e-0d0c-0d11c55103f0', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-7a32-ae2d-bfdf7d34066b');
INSERT INTO ops.sys_role_menu VALUES ('a418bb82-8f1b-87fc-780d-9f92da40db67', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-77b6-af02-f3d418647ce7');
INSERT INTO ops.sys_role_menu VALUES ('0b4b2961-bd7a-f761-e0e5-3433db1624a6', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-780c-be52-dc2f774aadd9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7164-828e-c0b2553d0f2e', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7f52-a288-3531857b9240', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7ccb-a504-7ff4b3a85e75', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-70ba-a13d-5173557b4d1a', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-73d4-8ae8-5629aaa4365a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('06c6a464-b01a-dd69-9492-d03e38f5ec10', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('7565a0c1-9ebb-ce4a-13e9-a17c4e9d5f9e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('502a117d-5a99-a2e5-43b1-1a34a406d5b0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('ae2c0dee-6117-bdac-b4dc-0a55fe158501', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('13dad0c5-66fc-9289-1b43-8dd04931d4ec', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('a7fc5994-11fc-60a0-73db-b173e5fd835b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ff015-d932-7e5a-afc7-e96442821270');
INSERT INTO ops.sys_role_menu VALUES ('afa41167-1aba-1ded-ffa7-7392bace855d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-7810-b631-aaf31236f6af');
INSERT INTO ops.sys_role_menu VALUES ('be4b8a4e-ced8-0aa7-6511-ae9466620485', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5559-79fe-95bc-f9e59bbab203');
INSERT INTO ops.sys_role_menu VALUES ('c1311b89-8ad9-698a-49aa-bb8ab6ded9b4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-55f1-7702-b666-f4431fa21c2a');
INSERT INTO ops.sys_role_menu VALUES ('53a33c59-3b79-5d75-c427-6a165f2f4ce4', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-556e-73cb-a804-9862819894b3');
INSERT INTO ops.sys_role_menu VALUES ('73abad5c-b298-ff70-c1e3-12d3b6d22fb3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fdb0b-641b-7876-b1d1-2c53cf0c40a3');
INSERT INTO ops.sys_role_menu VALUES ('727d3e3c-dcdb-3faa-efe0-e5a328013eb7', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fe029-d89a-7416-9260-f9ab51194f91');
INSERT INTO ops.sys_role_menu VALUES ('2900d313-a37d-84c9-bcea-edc4406e878e', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f2-185d-7f1a-854d-056ee195acd8');
INSERT INTO ops.sys_role_menu VALUES ('81ada09a-e087-3ef3-9db6-c2bf57a2b630', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('32f78489-3a5e-5240-f0ae-b008efc83c18', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-7192-bb54-c37eb0729def');
INSERT INTO ops.sys_role_menu VALUES ('2e721093-a589-5666-476f-40a31e553e7f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5f4-0019-7526-b0c0-f03a30f69e2b');
INSERT INTO ops.sys_role_menu VALUES ('abecdc52-bfb1-2f60-4569-0b86926ce3b5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f2-18ba-734e-8acc-4367490fcf80');
INSERT INTO ops.sys_role_menu VALUES ('997c864d-bf52-bcc7-9024-180c5b25d226', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5f4-0019-7526-b0c0-f03a30f69e2b');
INSERT INTO ops.sys_role_menu VALUES ('f4b9e2f2-8d0b-567f-eb69-ddc5c3cd58e7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ff58d-680f-717d-a25c-f40a7a6cf78f');
INSERT INTO ops.sys_role_menu VALUES ('9128fddd-dff5-1785-0e0e-b98fe136f12f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-7c4c-8700-999b9e498a58');
INSERT INTO ops.sys_role_menu VALUES ('032538a1-d50d-c9a5-6d9b-686154ae9ce1', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d4-758c-80f7-0221d64ec698');
INSERT INTO ops.sys_role_menu VALUES ('c79655ec-f907-67f5-73e7-4fe93c980dc0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7f2f-80c1-2bba8d5d1592');
INSERT INTO ops.sys_role_menu VALUES ('a6c6516f-90dd-6e98-bb3e-938ccc169563', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51db-7e02-9a2f-9c209fd28445');
INSERT INTO ops.sys_role_menu VALUES ('42c02062-579a-8144-f60a-70ae624fea01', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5527-7bd0-8fad-057ac3513b9c');
INSERT INTO ops.sys_role_menu VALUES ('1842f638-78e8-53e2-7a64-8e193e1000ee', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-72c6-a20a-013c193412ff');
INSERT INTO ops.sys_role_menu VALUES ('557a7ce4-747e-7ed6-b776-31e560a43c0e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5527-7837-b567-8b4b2e21ba59');
INSERT INTO ops.sys_role_menu VALUES ('96ac7b3b-f98d-68c7-6922-72dd9dfdf489', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7e0e-a59c-b2e6adda7747');
INSERT INTO ops.sys_role_menu VALUES ('3b836f61-8d47-c6ca-065b-9a72af1009a6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7ee6-a120-45b73eda04f3');
INSERT INTO ops.sys_role_menu VALUES ('d796ebd8-459b-ae80-237a-1ccb89e9ea86', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-73ab-b38d-ea4c880f7fa1');
INSERT INTO ops.sys_role_menu VALUES ('e7423272-4df7-eb92-38cc-d8a7b7d1a43a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-780c-be52-dc2f774aadd9');
INSERT INTO ops.sys_role_menu VALUES ('3b501287-8519-740f-523a-8c0a83d1d4cd', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-713d-9770-7262e3cc836e');
INSERT INTO ops.sys_role_menu VALUES ('6237c05f-d437-ea9b-0dbb-c0e3aefc3e73', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-799e-8336-a148f5fa3109');
INSERT INTO ops.sys_role_menu VALUES ('1b3cd186-67a2-c899-b0f4-4df804d51a8f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7fbe-b35c-c9a772bbc268');
INSERT INTO ops.sys_role_menu VALUES ('9f41e384-3218-e147-2dcc-4ba58a32fd03', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5591-71f5-9e46-03cba2242165');
INSERT INTO ops.sys_role_menu VALUES ('fe0e9393-c896-09e3-1307-79a87d4c80fd', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7701-8c2c-835ecfff3e60');
INSERT INTO ops.sys_role_menu VALUES ('1aee82dc-e2e0-dddd-545e-67a6d228eefa', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-7810-b631-aaf31236f6af');
INSERT INTO ops.sys_role_menu VALUES ('2e54e721-dbb2-8a44-e527-26b8cd2ccecf', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5559-79fe-95bc-f9e59bbab203');
INSERT INTO ops.sys_role_menu VALUES ('6d601431-6a9b-66a6-256a-38cf6a0afb07', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5773-7c2b-a8df-80ba0650d3ab');
INSERT INTO ops.sys_role_menu VALUES ('5568d528-dad7-a9c8-1b83-0c87fa11e0ac', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-77b6-af02-f3d418647ce7');
INSERT INTO ops.sys_role_menu VALUES ('51aba6ab-8658-56f9-762c-95ec44e7ca6f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-7fca-92b3-22448bd8d8db');
INSERT INTO ops.sys_role_menu VALUES ('edc3d4d3-cc84-4ac9-d7c6-d50cf537a2d1', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51da-77e7-a55d-16bea9adab3b');
INSERT INTO ops.sys_role_menu VALUES ('fe947d6a-e75a-5c11-144f-b392ed7f15f5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5527-7ec4-ac74-f8ded022d695');
INSERT INTO ops.sys_role_menu VALUES ('a3914122-99d3-9776-86ec-7cee5b9772e7', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5527-7a99-b861-9a8a10988fbc');
INSERT INTO ops.sys_role_menu VALUES ('dc9852f7-d6b3-438d-4131-8a9bbe465f19', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-7a32-ae2d-bfdf7d34066b');
INSERT INTO ops.sys_role_menu VALUES ('26ae227a-7fc5-7109-71d7-a5ec6da91329', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-5527-7cb5-86b8-8cc9741427a1');
INSERT INTO ops.sys_role_menu VALUES ('18116a7c-34e8-65f0-1bb8-440e1edc3258', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03');
INSERT INTO ops.sys_role_menu VALUES ('79606e82-9d07-9763-70ff-1b5f9b2d99b5', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d7-754a-839d-7f7265c42c50');
INSERT INTO ops.sys_role_menu VALUES ('7f5d461f-6b12-82f0-eb05-2ee4c9db26c9', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-7eec-9055-4f5b6aaaa29d');
INSERT INTO ops.sys_role_menu VALUES ('e47f42ec-52ba-f863-47c2-0c4865757ad3', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('d54ecbb4-e38c-9c36-be09-0adcbb26d036', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51da-77e7-a55d-16bea9adab3b');
INSERT INTO ops.sys_role_menu VALUES ('28a6f620-e8c8-ea09-fb56-0a967bc88dff', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51da-7686-8064-c7c8019205b3');
INSERT INTO ops.sys_role_menu VALUES ('bc1b7d6b-d36f-7a21-5ea8-7f319c695768', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-7aa1-bb1c-5532bac6fc97');
INSERT INTO ops.sys_role_menu VALUES ('ea006406-2c22-2e15-9dd7-cf50dfc14119', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7408-97d3-e6ed0a92ecc4');
INSERT INTO ops.sys_role_menu VALUES ('c40bee3e-f43a-779c-b7d2-58bf31b9f198', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-7668-93fe-89b57b6f90b3');
INSERT INTO ops.sys_role_menu VALUES ('c42317a4-71bf-4e84-8a83-c28b42684fe8', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-7e86-9643-08356660d83c');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7f58-9854-e28d294147f9', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c6d-7273-b552-36d597ef7989');
INSERT INTO ops.sys_role_menu VALUES ('74496139-77b1-3802-8912-b3e2d75f6517', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7977-96dd-ff09b640a869');
INSERT INTO ops.sys_role_menu VALUES ('e7e845f5-f11a-843f-21ce-d15c955ef2da', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-7aab-ad65-eb8bb85a40f2');
INSERT INTO ops.sys_role_menu VALUES ('ba2c20dc-466e-acbf-90eb-737296a40e9a', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e1-77c6-a607-95b99bc63155');
INSERT INTO ops.sys_role_menu VALUES ('3f19cf5a-d86b-6fe4-b5c5-84f6c15f282e', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7eaa-bd22-b3ca386fb084');
INSERT INTO ops.sys_role_menu VALUES ('ce7dbc13-903c-526e-944b-fdd7b28ef146', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-55b7-7f05-be96-93f5ef62a685');
INSERT INTO ops.sys_role_menu VALUES ('27bd2d67-c19d-c490-9ad1-721af1238c33', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e');
INSERT INTO ops.sys_role_menu VALUES ('2cdbc81a-02c7-5ae8-5fdc-91758e44429e', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-73e5-8cb9-0d4d8694d3d9');
INSERT INTO ops.sys_role_menu VALUES ('258b8c69-155e-fd01-9a06-d33c121b1403', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-776c-8cb6-74903e6501d6');
INSERT INTO ops.sys_role_menu VALUES ('4a739827-2f02-6a34-9e53-4efecb9c9a51', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d6-73ab-b38d-ea4c880f7fa1');
INSERT INTO ops.sys_role_menu VALUES ('a15de0d9-3b61-89eb-548c-4ca3c2a253cf', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7ee6-a120-45b73eda04f3');
INSERT INTO ops.sys_role_menu VALUES ('4215e20c-6a58-fa80-234a-bd6c0ad26108', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7e0e-a59c-b2e6adda7747');
INSERT INTO ops.sys_role_menu VALUES ('daeeedbf-7d7a-91c0-2134-79b1d59b9472', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5527-7837-b567-8b4b2e21ba59');
INSERT INTO ops.sys_role_menu VALUES ('5f69a58c-1a80-91e5-02a7-36f139725604', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-72c6-a20a-013c193412ff');
INSERT INTO ops.sys_role_menu VALUES ('93fdd2ea-076a-6150-3e4e-9f1c18efbb16', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5527-7bd0-8fad-057ac3513b9c');
INSERT INTO ops.sys_role_menu VALUES ('a7684a4f-81a7-9fac-cf38-2897807da135', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51db-7e02-9a2f-9c209fd28445');
INSERT INTO ops.sys_role_menu VALUES ('ab93faab-6cff-ee44-b42c-31bb1915a03f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394');
INSERT INTO ops.sys_role_menu VALUES ('5d8f38d0-fb0c-d4f5-7fb9-09c5ec4d9e72', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7f2f-80c1-2bba8d5d1592');
INSERT INTO ops.sys_role_menu VALUES ('b2eeb284-a384-9417-a02f-0d942af9efbf', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0');
INSERT INTO ops.sys_role_menu VALUES ('643a1280-fcd1-7456-afdb-352deef32ef2', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d8-7c4c-8700-999b9e498a58');
INSERT INTO ops.sys_role_menu VALUES ('5fd226e9-d611-794c-d8f3-eed6b439c1d0', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03');
INSERT INTO ops.sys_role_menu VALUES ('cffd8846-01a4-293b-4894-a7117e84293c', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5527-7cb5-86b8-8cc9741427a1');
INSERT INTO ops.sys_role_menu VALUES ('9a2cfa5e-f0cf-15c5-67e1-760750b9d826', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5527-7ec4-ac74-f8ded022d695');
INSERT INTO ops.sys_role_menu VALUES ('59cac4f7-ace5-00fa-fd61-5730e3c7978f', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5527-7a99-b861-9a8a10988fbc');
INSERT INTO ops.sys_role_menu VALUES ('16f97832-ac5b-7570-5b55-eabbce6a383d', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d5-7fca-92b3-22448bd8d8db');
INSERT INTO ops.sys_role_menu VALUES ('6f9cdc53-7160-0dce-805d-31086cc1488b', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-5773-7c2b-a8df-80ba0650d3ab');
INSERT INTO ops.sys_role_menu VALUES ('4762bc5e-3a3d-73cf-2a72-7c36081279c1', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-51d9-7701-8c2c-835ecfff3e60');
INSERT INTO ops.sys_role_menu VALUES ('575163b4-51da-c9a8-9cdf-5e1b3e419944', '019fd5dc-5508-7434-9963-6ae87dff963c', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-761f-8db0-f137d5cddb80', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7e21-9854-18102af2901f', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7844-8695-2fd9ca0d0742', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7011-ab30-8617558dba44', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-79d7-995b-aa298a50bf33', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-751c-b2b2-f1485fb2a251', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7edb-8ac1-a04848ae4784', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c6e-7f0a-ad28-54743f700ff8');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7875-9083-e8345f2f0319', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('a6ab9705-e947-8ab1-ff49-655696107fa9', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fe029-d89a-7416-9260-f9ab51194f91');
INSERT INTO ops.sys_role_menu VALUES ('76f82a93-fb25-cb5f-0d16-8651baa31a2f', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d8-7e86-9643-08356660d83c');
INSERT INTO ops.sys_role_menu VALUES ('50b1cef9-1539-e566-e04d-3fc99902697e', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d4-758c-80f7-0221d64ec698');
INSERT INTO ops.sys_role_menu VALUES ('836a1223-390d-0147-75f6-51c24ee56acd', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fdb0b-641b-7876-b1d1-2c53cf0c40a3');
INSERT INTO ops.sys_role_menu VALUES ('4e1dec25-ea4f-286a-bafc-2b7230788749', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('f2820c10-d226-dde5-02e8-938d3826832f', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d5-7794-aee4-b73d0c73b3d0');
INSERT INTO ops.sys_role_menu VALUES ('be7abc7b-d25f-9730-2546-596493033dce', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d5-7fca-92b3-22448bd8d8db');
INSERT INTO ops.sys_role_menu VALUES ('be8dc232-58c4-9e8e-8242-658f8b77fa80', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e');
INSERT INTO ops.sys_role_menu VALUES ('4e252fae-8f6f-94c6-e538-dd48776b4bec', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03');
INSERT INTO ops.sys_role_menu VALUES ('d1e22783-0b94-9691-85d3-33845ea02909', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d7-754a-839d-7f7265c42c50');
INSERT INTO ops.sys_role_menu VALUES ('82de7a28-3f26-11c2-e6a8-564959922456', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51d8-776c-8cb6-74903e6501d6');
INSERT INTO ops.sys_role_menu VALUES ('e011d716-55a7-48ed-cb54-c4227152cbef', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-54e1-7c92-815b-0dece11f1f79');
INSERT INTO ops.sys_role_menu VALUES ('c3c0b4d3-5906-7407-0843-24ea0b37403c', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '019fd5dc-51db-7e02-9a2f-9c209fd28445');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7e02-a1dc-062455f4933a', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-79a8-bd74-f24795029a89', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-71cc-a1e7-8b2bdd1eda5e', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7044-a4d0-8a2eb03645bf', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-72d8-bed6-3cb65ad03983', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7820-b8c4-3253af524bd7', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c6e-759e-b369-f35454a11edc');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-7981-bfa1-5169ff4ea374', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-76dc-8e08-e327352f663c', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-7626-bcbb-ece1d2068a4d', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-7112-bd30-f9385ed56c1f', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-75ce-884b-07c665af76a9', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-71da-8fc3-8081be9c7844', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00327-5a8c-71ff-884b-b7a6358a0d91', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('39e7e0b5-7193-52f2-3685-240cb5ee4148', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-7192-bb54-c37eb0729def');
INSERT INTO ops.sys_role_menu VALUES ('b89670e3-d5d7-22f0-83c8-2bc40472e37d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-7eec-9055-4f5b6aaaa29d');
INSERT INTO ops.sys_role_menu VALUES ('abd273fa-68b1-23be-2c18-5442a177bd20', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-7668-93fe-89b57b6f90b3');
INSERT INTO ops.sys_role_menu VALUES ('f76f723e-3a31-1c6c-4e53-9940d433713f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7408-97d3-e6ed0a92ecc4');
INSERT INTO ops.sys_role_menu VALUES ('d4f44cb7-7ea3-fd08-bd2a-aa47b7b3d61c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-7aa1-bb1c-5532bac6fc97');
INSERT INTO ops.sys_role_menu VALUES ('e82a3f73-5110-5dd9-b0c7-b3e7ba100b46', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51da-7686-8064-c7c8019205b3');
INSERT INTO ops.sys_role_menu VALUES ('48bb2cdb-9d0b-1c7d-253d-4a6f0abc8b1a', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d7-754a-839d-7f7265c42c50');
INSERT INTO ops.sys_role_menu VALUES ('633adb77-f5e3-af94-461c-26a45254369f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-776c-8cb6-74903e6501d6');
INSERT INTO ops.sys_role_menu VALUES ('bef594e6-c014-686a-873b-af5672b0d286', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-73e5-8cb9-0d4d8694d3d9');
INSERT INTO ops.sys_role_menu VALUES ('d74653fd-4760-8b73-308c-b68867bd0e3b', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d2-7324-8a3e-16bd032a2b5e');
INSERT INTO ops.sys_role_menu VALUES ('9af7ee00-1cdb-3854-f2dc-f7f72bbcab86', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-55b7-7f05-be96-93f5ef62a685');
INSERT INTO ops.sys_role_menu VALUES ('32496426-59a9-5351-cb03-6db6db59199d', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7eaa-bd22-b3ca386fb084');
INSERT INTO ops.sys_role_menu VALUES ('17d24db6-a7d8-1314-1700-6fd68f90cdb5', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d5-7794-aee4-b73d0c73b3d0');
INSERT INTO ops.sys_role_menu VALUES ('e13614a2-10fb-237f-4f73-f5bcb3f8a1ed', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-556e-73cb-a804-9862819894b3');
INSERT INTO ops.sys_role_menu VALUES ('da05153f-6f4d-5b9c-07bf-94962b93795f', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d9-7977-96dd-ff09b640a869');
INSERT INTO ops.sys_role_menu VALUES ('c2496b21-9363-9f81-27c9-d548c1a54c99', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d6-7aab-ad65-eb8bb85a40f2');
INSERT INTO ops.sys_role_menu VALUES ('dd51e637-f9f3-2acb-c0c5-ee2c4a66b3c0', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-55f1-7702-b666-f4431fa21c2a');
INSERT INTO ops.sys_role_menu VALUES ('2e7eb803-0ebd-bdff-c23a-0d2d4b3d79e9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-7e86-9643-08356660d83c');
INSERT INTO ops.sys_role_menu VALUES ('80d770d1-4c4b-c2ca-799f-fcde5f8bc962', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae');
INSERT INTO ops.sys_role_menu VALUES ('4deb0524-03fd-5c6c-98d8-3d909db632df', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ff87b-1682-738e-9900-b655a1f6bfb0');
INSERT INTO ops.sys_role_menu VALUES ('ec61ac17-7144-5948-51c5-cfa1435cd094', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fd657-e0f1-7efa-b193-d90aa187a4de');
INSERT INTO ops.sys_role_menu VALUES ('71189bad-4af0-f39a-0442-93eaa2acd1b8', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5e8-738d-ba9e-366909b4094f');
INSERT INTO ops.sys_role_menu VALUES ('fd7541b8-1b27-822e-26e9-9303ba8b4451', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5eb-7fef-8899-134a6bfc164b');
INSERT INTO ops.sys_role_menu VALUES ('51e00d86-ab58-03d9-2c25-92784164d818', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b');
INSERT INTO ops.sys_role_menu VALUES ('db5442d3-82f2-4ddb-c71c-6826befc92d6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019ffe6f-b5eb-75dc-b569-a02d47dd9fca');
INSERT INTO ops.sys_role_menu VALUES ('5e3b891a-4c93-90dc-1e7f-cde465e1cbb9', '019fd5dc-550a-79df-b659-45ae9a203ec5', '14fd60c2-2d74-400e-b68e-283cccf51649');
INSERT INTO ops.sys_role_menu VALUES ('8d11a479-8ae7-a0fe-1efc-312a75b2d7db', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a000e9-117b-7526-9c17-7c79cf2a0ebe');
INSERT INTO ops.sys_role_menu VALUES ('1f6203af-7111-177f-9b68-354fa99db37c', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00327-5a87-7dc3-ac27-29b4702f190d');
INSERT INTO ops.sys_role_menu VALUES ('f6148ebb-96c2-34b0-aaab-b1be71dc0b5e', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a000ba-ac7d-7a91-8d53-00bed2c4b73a');
INSERT INTO ops.sys_role_menu VALUES ('efadc749-9313-38da-1a8d-5310c37c0aa6', '019fd5dc-550a-79df-b659-45ae9a203ec5', '019fdb0b-641b-7876-b1d1-2c53cf0c40a3');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-77b8-b31f-97d2cb6093b8', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7be5-91f1-fe7489489c8f', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-718a-93ac-cc8401e1b1a9', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-717e-94ce-4433f83d8116', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-732c-92ee-e1406d1b5363', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-788e-9c96-48a40af63a8f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c6e-771a-a4cc-e605d0ff97da');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-79a5-8edb-aa72449aad07', '019fd5dc-5508-7434-9963-6ae87dff963c', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7cd9-b372-e2038163c903', '019fd5dc-5509-7bd7-8f04-fec45f22420e', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7a2c-9cd6-a36bbf5ed8b2', '019fd5dc-5509-7099-9dd7-05d9eee93e92', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7f5c-bf87-7ec354ae240f', '019fd5dc-5509-7cb6-b760-c1da652ab9a0', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7010-ba70-1f511e222d98', '019fd5dc-5509-753b-91cb-03baaaa2517b', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-78ae-b66d-28a017f335ab', '019fd5dc-550a-79df-b659-45ae9a203ec5', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');
INSERT INTO ops.sys_role_menu VALUES ('01a00441-5c7a-7377-95df-ecf8ef50726f', '019fd5dc-550a-77d4-a06c-f38fbaf957f7', '01a00441-5c6e-7554-be66-f8efb6cdc2d9');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_user_role; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f8a6-78b7-bef0-90f1b1716ce8', 'a1fb9215-2ab0-2f96-7fec-e2f795283b9b', '019fd5dc-5509-7bd7-8f04-fec45f22420e');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f8e6-7da3-9a66-3d2c573126b2', 'bc74d5a2-e920-3284-9a9e-b9c638df80ef', '019fd5dc-5509-7de5-8ad9-4e085677e466');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f925-738a-9f55-468294665da2', '79eb8a6d-9676-515f-3ad9-696e3a0fa301', '019fd5dc-5509-7108-b319-c61ea6ca0682');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f965-731d-bf65-693dc8d1349d', 'f28ef82a-f574-8916-41e6-bdd04ead7725', '019fd5dc-5509-7d38-af2d-721cd77d0df8');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f9a7-7fd9-8461-ecbc702ed453', '118db5e2-3f9e-b0fc-17b1-40a7be92aa33', '019fd5dc-5509-7cb6-b760-c1da652ab9a0');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-f9ec-7c05-903a-81d4ad5d1177', 'a9d66060-9fa3-d966-d945-9a3b1337b2fc', '019fd5dc-5509-78ec-ba54-c221b77dc53f');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fa2d-7d47-922f-77e0b87b8227', 'd6f3a0a0-2b81-9957-e293-a2c7696d3fac', '019fd5dc-550a-74af-b487-c605e0524dc2');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fa6c-7593-99dc-6146d88766c0', '0be4a44d-d0f8-5a89-af6a-090f7d3af5f1', '019fd5dc-550a-79df-b659-45ae9a203ec5');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-faab-7282-984c-e975a191e21d', '0cdbda8a-77e4-236f-a10c-b52bbbd410d4', '019fd5dc-5509-7099-9dd7-05d9eee93e92');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-faeb-7fad-be34-3092748af87c', '565ceb7d-062e-de6a-a0bb-c06e163821c3', '019fd5dc-5509-7536-9965-e4a923dd33b6');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fb28-768b-9c35-cdeb9f8baddb', '55b888fc-b57e-130e-d33f-95f37deb6611', '019fd5dc-5509-7ee1-97f5-9f54bcadc895');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fb67-79c7-8199-2988ceeb0d86', '1e7a138b-a099-c1f1-b209-ed9ed54be803', '019fd5dc-5509-7cfe-b814-0d76c14013cd');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fba4-7d73-99b2-1f9a07249a68', 'dd9a2016-38af-8a88-8c68-7927d2201531', '019fd5dc-5509-753b-91cb-03baaaa2517b');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fbe3-75a0-9d6d-39ecc507244a', '5586f3b1-12d8-78b1-3383-cbc51203ec68', '019fd5dc-5509-788e-b885-f37e68d31ddc');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fc23-7357-b900-65b17927a499', 'e9d426be-a4b3-4a6b-648c-7df334c82eb6', '019fd5dc-550a-7e00-bf48-9bdad1801952');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fc62-72f8-970b-de82e002e997', '42ea2264-1ba7-85af-4370-78365f779ca2', '019fd5dc-550a-77d4-a06c-f38fbaf957f7');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fc9e-703e-a7c1-9bb45c8755e3', '81138dd8-e3b1-53dd-fae3-99836f0de225', '019fd5dc-550a-7d2b-b55c-f583dec4d8a9');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fcdc-7266-8ea7-de57fc898ffc', 'eea2d9c0-edcf-2de8-43f1-6ade752f23ec', '019fd5dc-550a-71c8-8224-10b5ceb6d875');
INSERT INTO ops.sys_user_role VALUES ('019fd5f3-fd1b-7def-bb40-f07d74271524', 'bf33d3bd-902c-1d3d-406c-4b5939483b8f', '019fd5dc-5508-7434-9963-6ae87dff963c');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_dict; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7b4a-8d3b-6c776b74a6ea', 'fia_task_status', '待检', '待检', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-77e7-88f8-ff2e8860de47', 'fia_task_status', '进行中', '进行中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-76c9-8a0c-8f2db0e22f33', 'fia_task_status', '已完成', '已完成', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7466-a6c0-fa00ef4591b6', 'fia_task_status', '超时', '超时', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-759b-a5d2-cd8cf7ff53c0', 'fia_task_status', '已作废', '已作废', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7227-aa74-f4869ef7b696', 'fia_trigger_event', '换模具', '换模具', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7aa1-9b05-ddc7a891eb89', 'fia_trigger_event', '换批次', '换批次', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-777e-8bd8-e5810f926fc1', 'fia_trigger_event', '材料批次变更', '材料批次变更', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-773b-b350-a28ddc83df23', 'fia_trigger_event', '换设备', '换设备', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-72f0-b242-3264c2fcb657', 'fia_trigger_event', '系统升级', '系统升级', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7c90-986c-fbde58ba9f4a', 'fia_approval_type', '豁免开工', '豁免开工', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-70fc-b26c-c14664defabb', 'fia_approval_type', '紧急放行', '紧急放行', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-75dc-aaed-8fd3fed67cf4', 'fia_approval_type', '让步接收', '让步接收', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7d39-bb31-d991e2295f6b', 'fia_sign_method', 'password', '密码', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-77ce-b797-749a27e3f9e2', 'fia_sign_method', 'handwriting', '手写', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-738c-b47c-db710cb2b77f', 'fia_sign_method', 'ca', 'CA证书', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-757b-be65-07c3c7d5c61a', 'fia_intercept_mode', '硬阻断', '硬阻断', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7e15-96e8-5e935f524c11', 'fia_intercept_mode', '软阻断', '软阻断', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7f73-a8ba-b4584be5efd0', 'fia_sign_nodes', '两级', '两级(检验+复核)', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7f25-969f-ee5d979dc37e', 'fia_sign_nodes', '三级', '三级(加批准)', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7003-bdf7-07ee9736ff36', 'fia_sign_granularity', '整单签名', '整单签名', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7ffe-85d1-bc5203143c55', 'fia_sign_granularity', '逐项签名', '逐项签名', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-7e7a-a6e8-e8c9921095a0', 'fia_multi_trigger', '合并一张校验单', '合并一张校验单', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b4-79d6-aad5-108fe25cdb94', 'fia_multi_trigger', '各自生成', '各自生成', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7a49-8619-7cd5676c43c5', 'spc_alarm_level', '预警', '预警', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-734c-8885-4cf8a310154f', 'spc_alarm_level', '报警', '报警', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-70ee-bf1f-26661adb1f41', 'spc_capability_level', '充足', '充足(CPK>=1.33)', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-735e-bb48-285683a2f529', 'spc_capability_level', '尚可', '尚可(1.0-1.33)', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7b9a-8a5d-0eea995fdb18', 'spc_capability_level', '不足', '不足(<1.0)', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-76f7-9d08-1a66d5ff7d54', 'spc_chart_type', 'Xbar-R', 'Xbar-R', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-789a-9fe6-5b6ab27ce95f', 'spc_chart_type', 'Xbar-s', 'Xbar-s', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7582-92d9-5fc48143a290', 'spc_chart_type', 'I-MR', 'I-MR', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7d3a-a2b1-33390aa831b8', 'spc_collect_status', '待采集', '待采集', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7d80-9a12-5a14c241f651', 'spc_collect_status', '已完成', '已完成', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b5-7720-bdda-53e95308be58', 'spc_collect_status', '缺失', '缺失', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7cc5-9aa8-14efc4f94414', 'ncm_defect_category', '尺寸类', '尺寸类', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-749c-a329-347cca1bcc95', 'ncm_defect_category', '外观类', '外观类', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7ce8-94ca-584b9b8ea2f4', 'ncm_defect_category', '功能类', '功能类', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7ad7-9741-b0cb5e896c57', 'ncm_defect_category', '装配类', '装配类', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7c7a-8ff7-64942b4194b1', 'ncm_defect_category', '包装类', '包装类', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7f38-978a-b3a384bd8b49', 'ncm_disposition', '让步', '让步', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-773d-8dd5-b16b724d811c', 'ncm_disposition', '返工', '返工', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7c9e-a801-c298fb536dec', 'ncm_disposition', '报废', '报废', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7416-8843-b24fa0a10ab6', 'ncm_disposition', '降级', '降级', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7312-af07-e1aecf63327c', 'ncm_fishbone_5m1e', '人', '人', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7d1f-a5eb-cc7b3bc1b2a4', 'ncm_fishbone_5m1e', '机', '机', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7949-8dcb-d327027d85b6', 'ncm_fishbone_5m1e', '料', '料', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-72b7-8c23-c826272a9b3d', 'ncm_fishbone_5m1e', '法', '法', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7599-93ef-44c715d55f61', 'ncm_fishbone_5m1e', '环', '环', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7cd9-a042-90a33724a4ef', 'ncm_fishbone_5m1e', '测', '测', 6, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-710e-a751-33f50b11a793', 'capa_status', '待启动', '待启动', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7c4b-87cb-6b26d9d0d240', 'capa_status', '进行中', '进行中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7ea5-b36c-f51caef6871a', 'capa_status', '已验证', '已验证', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7b8f-b386-d2fd899567b7', 'capa_status', '已关闭', '已关闭', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-70eb-beb3-d1c5d1a45cb6', 'esc_level', 'L1', 'L1 班组长(30min)', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7e6d-8502-5a95585c1e86', 'esc_level', 'L2', 'L2 质量主管(60min)', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b6-7e2b-bfa9-3ea36f45a3ed', 'esc_level', 'L3', 'L3 质量经理(120min)', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7641-a38f-ce1be2f774cc', 'audit_type', '年度复审', '年度复审', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-757f-a6c9-c663498b0f24', 'audit_type', '过程审核', '过程审核', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7eca-947b-c24da750e7ac', 'audit_type', '专项审核', '专项审核', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7c98-9327-694d4d8cd646', 'audit_type', '飞行检查', '飞行检查', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-70b5-b203-da48725f386e', 'audit_type', '初次审核', '初次审核', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7d95-98eb-98819eded4d4', 'audit_type', '年度审核', '年度审核', 6, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7b66-97d7-b94e13937d8c', 'audit_type', '附加审核', '附加审核', 7, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-73ce-8a85-87c8f5f46973', 'audit_type', '重新审核', '重新审核', 8, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-7016-83a6-1f7e6b5febab', 'audit_nc_status', '待整改', '待整改', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b7-758b-bd7a-9cd7147bb9f1', 'audit_nc_status', '整改中', '整改中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7c5a-8f9f-76d8912479d9', 'audit_nc_status', '已整改', '已整改', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7403-842a-36a939779edd', 'audit_nc_status', '已关闭', '已关闭', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7502-aa52-8fa27de93985', 'change_status', '待申请', '待申请', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-710d-a323-39235f0dc80c', 'change_status', '审批中', '审批中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7b99-b121-eed2b570481c', 'change_status', '已批准', '已批准', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7da6-af5b-c1cbbd7571d7', 'change_status', '已执行', '已执行', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7022-b712-702fd0961376', 'change_status', '已关闭', '已关闭', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7586-ba6e-e89bc45a8445', 'change_status', '已驳回', '已驳回', 6, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7b5d-9296-3f9fa20b0e57', 'change_status', '已回滚', '已回滚', 7, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7e87-be44-c2a2e1118136', 'abnormal_status', '待处理', '待处理', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-76cc-a7a6-46b938229f3d', 'abnormal_status', '整改中', '整改中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7129-8cb9-351ed724899d', 'abnormal_status', '待验证', '待验证', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7aa6-80f2-b0214186dec2', 'abnormal_status', '三批验证', '三批验证', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7302-b4d7-3a36ee8064f1', 'abnormal_status', '已关闭', '已关闭', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7095-9748-1fd33a205d50', 'trace_node_type', 'incoming', '来料', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-75dd-9c31-45ec13c06bca', 'trace_node_type', 'raw', '原料', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7d9a-a757-1efe2a5786d6', 'trace_node_type', 'semi', '半成品', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7aaa-8e7a-584321554415', 'trace_node_type', 'ship', '发货', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7e50-9a0f-5f630414b8dd', 'trace_node_type', 'customer', '客户', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7c20-9b30-d82f4fa5f052', 'trace_node_type', 'process', '工序', 6, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7431-9c94-e82e80f6eebd', 'supplier_level', 'A', 'A', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7c65-9893-e4dd1a88c462', 'supplier_level', 'B', 'B', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7786-bd66-e2419c172658', 'supplier_level', 'C', 'C', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7e22-9682-39173b0f4202', 'supplier_level', 'D', 'D', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7c41-9859-b3b194b179e2', 'supplier_status', '合格', '合格', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7e30-8973-1e3aab0d208b', 'supplier_status', '观察', '观察', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7894-83d3-eb60f2c31578', 'supplier_status', '整改中', '整改中', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-79ec-8af0-11f7eacf3d61', 'supplier_status', '暂停', '暂停', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7d8b-9864-56e4210f3d93', 'supplier_status', '淘汰', '淘汰', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7d26-a1dd-40d8e4681f1f', 'sqm_nc_level', '严重', '严重', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-729b-9d8e-d46272043bf1', 'sqm_nc_level', '一般', '一般', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-769c-babd-0aae70a15672', 'sqm_nc_level', '观察项', '观察项', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7b9c-ba9d-8bc9dbf3265c', 'sqm_abnormal_disposal', '退货', '退货', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-75d7-b376-111f2816bb95', 'sqm_abnormal_disposal', '特采', '特采', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7893-bcda-d2d2d67c6004', 'sqm_abnormal_disposal', '挑选使用', '挑选使用', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7d6f-9bac-d56b81e69fc0', 'sqm_abnormal_disposal', '报废', '报废', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-712b-b95d-38bace2bf940', 'sqm_change_role', 'quality', '质量', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-79fe-8e8b-4b1fd39ce3b1', 'sqm_change_role', 'purchase', '采购', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-7d7b-aaf9-f033666b62e4', 'sqm_change_role', 'rd', '研发', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b8-714b-82bf-28e5226cf053', 'sqm_change_role', 'trial', '试产', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-72b3-9798-9bd463265de3', 'fmea_status', '待闭环', '待闭环', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-7df7-a192-eab21c8da59f', 'fmea_status', '进行中', '进行中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-7749-9437-9ea2a36d4bcf', 'fmea_status', '已闭环', '已闭环', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-79ca-a41e-6311b0ae05cf', 'fmea_risk_level', '高', '高(>=150)', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-73d0-aec8-6704af1394f0', 'fmea_risk_level', '中高', '中高(100-149)', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-7098-9753-5cd14d64b7dd', 'fmea_risk_level', '中', '中', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50b9-7c33-8e91-85aa75fb5b2e', 'fmea_risk_level', '低', '低', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-74ca-a26b-4a095a39b6ef', 'severity', '严重', '严重', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-71ee-ba61-d1c7d1e461a3', 'severity', '一般', '一般', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7d9c-9743-ef6d320c2fd6', 'severity', '轻微', '轻微', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-72f3-9bb1-6dbd2ff013f5', 'user_status', '启用', '启用', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7c1c-8de2-84c3d316353a', 'user_status', '停用', '停用', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-72a3-b1ab-41926704ed1e', 'user_status', '锁定', '锁定', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-713b-812e-43afcdbca2bd', 'org_type', '公司', '公司', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7071-9e4a-e480ed5cb4f8', 'org_type', '工厂', '工厂', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7ff0-bd95-06ce7c8ea782', 'org_type', '车间', '车间', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-70c1-a1ed-e1de578df9e5', 'org_type', '产线', '产线', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-77f4-8323-c7adc986d24b', 'org_type', '工位', '工位', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-782f-967b-7250ffd471eb', 'common_status', 'pending', '待处理', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-798e-a66b-40268f1fad92', 'common_status', 'processing', '进行中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-78a7-bcc4-ac70532f375d', 'common_status', 'done', '已完成', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7ae2-927f-96c7e49c10c0', 'common_status', 'rejected', '已驳回', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50ba-7cd5-a643-035f5090d44e', 'common_status', 'overdue', '已超期', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-71d6-972e-194844f1aabd', 'tlm_tooling_status', '在用', '在用', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7832-a4ef-688768ed5407', 'tlm_tooling_status', '维修中', '维修中', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7bce-8005-6ed0e3f7c5cd', 'tlm_tooling_status', '停用', '停用', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-71d4-b061-8fbc22deb6e1', 'tlm_tooling_status', '报废', '报废', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7d66-bdea-585c98089c35', 'tlm_repair_result', '修复合格', '修复合格', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7a8b-bf92-5838df1a32ce', 'tlm_repair_result', '无法修复', '无法修复', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7ac5-a80c-fd30e0000954', 'asm_low_score_cause', '响应慢', '响应慢', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-759b-8a6b-bb3f54de58ed', 'asm_low_score_cause', '维修不彻底', '维修不彻底', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7683-a5af-1d7d27d2989a', 'asm_low_score_cause', '服务态度', '服务态度', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7f57-8100-f435acf25865', 'asm_low_score_cause', '配件缺货', '配件缺货', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7d4f-a343-5b17632f8af3', 'qsm_health_dimension', '质量目标', '质量目标', 1, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-79aa-8555-4f7640a84fda', 'qsm_health_dimension', '内审整改', '内审整改', 2, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7886-8441-70ffad0acc35', 'qsm_health_dimension', '不良事件', '不良事件', 3, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7bc8-a73e-f37a5836460c', 'qsm_health_dimension', '顾客满意', '顾客满意', 4, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);
INSERT INTO ops.sys_dict VALUES ('019fd5dc-50bb-7edf-ac29-a5ae1cb25c96', 'qsm_health_dimension', '合规率', '合规率', 5, true, '2026-08-06 14:56:53.416184+08', NULL, '2026-08-06 14:56:53.416184+08', NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_config; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_config VALUES ('019fd5dc-5674-7aac-9339-67c6bea167bb', 'org.switch.affectsWrite', 'false', '组织切换是否影响新建/修改数据的归属组织(true=归属所选分公司,false=仅查询过滤)', '2026-08-06 14:56:54.884034+08', '2026-08-06 14:56:54.884034+08', NULL, NULL, false, 0);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_data_scope; Type: TABLE DATA; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_delegation; Type: TABLE DATA; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_button; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7fa7-8941-260d5b71e81e', '019fd5dc-54e1-7c92-815b-0dece11f1f79', 'system.org.delete', '删除组织');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-78ac-8ad2-b7566c06c30a', '019fd5dc-54e1-7c92-815b-0dece11f1f79', 'system.org.create', '新增组织');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-74a1-bce7-9ece67b42a38', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394', 'system.menu.delete', '删除菜单');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7fa7-8859-c8861656f069', '019fd5dc-54e1-76b5-9e51-ac80d1c1f394', 'system.menu.create', '新增菜单');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7bd4-8970-b2d3adb560fc', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0', 'system.role.assign', '分配权限');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7fc2-8c15-ef42dc44c961', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0', 'system.role.delete', '删除角色');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-78a0-bf1c-86e828260f15', '019fd5dc-54e1-77a7-8f28-798acd1ad7b0', 'system.role.create', '新增角色');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7709-ba15-1ec971d5c38c', '019fd5dc-54e1-77c6-a607-95b99bc63155', 'system.role.assign', '分配角色');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7060-a83a-c7e046abc1f3', '019fd5dc-54e1-77c6-a607-95b99bc63155', 'system.user.delete', '删除用户');
INSERT INTO ops.sys_button VALUES ('019fd5dc-54e2-7c3b-8896-b8a7aaa96840', '019fd5dc-54e1-77c6-a607-95b99bc63155', 'system.user.create', '新增用户');
INSERT INTO ops.sys_button VALUES ('019fd5dc-5698-76e9-83e3-18dc557b80f4', '019fd5dc-54e1-7c92-815b-0dece11f1f79', 'system.org.switch', '切换分公司');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff1b-7864-836d-673b58af90bf', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.sign.inspector', '检验员签名');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff21-77ff-8385-c88940604f7f', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.sign.reviewer', '复核人签名');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff27-791c-ac1f-b828930d376f', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.sign.approver', '批准人签名');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff32-7aa0-8a47-cd9bed47737a', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.record.delete', '记录/过滤方案删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff39-745e-b575-bb76833d7004', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.capa.close', 'CAPA关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff40-7fb1-a469-31606b5cb432', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.capa.approve', 'CAPA审批');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff45-79ab-b00b-f1fee1949a19', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.capa.reset', 'CAPA重置');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff4b-735c-8a49-4c2476c5a6fb', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.corrective.close', '纠正措施关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff51-7966-bbb7-893f1f5b5dec', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.submit', '变更提交');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff56-7621-961c-9f4ddbfe80ed', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.approve', '变更审批');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff5b-76ca-a968-dd0851703a50', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.close', '变更关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff61-7350-b674-c45ce345a1c7', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.rollback', '变更回退');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff65-7a3b-8c99-a03184ca4a1e', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.verify-sign', '变更签名验证');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff6b-72bb-9784-eefbc06103b5', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea.close', 'FMEA闭环');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff71-7de0-a2f1-a5cd0ac1d61d', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea.reopen', 'FMEA重开');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff75-7441-bd4b-a1128a370805', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea.scan-overdue', 'FMEA超期扫描');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff7a-7416-9c8d-01850f1f03be', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.abnormal.close', '异常关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff7e-77ae-bc47-4cda913dd571', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.abnormal.escalation-check', '异常升级检查');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff88-7269-a594-4517eed05982', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.plan.start', '审核开始');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff8e-7914-829b-0e128d00244e', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.nc.close', 'NC关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff93-764a-98fb-c83aa53a3213', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.archive', '审核归档');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ff9a-7f91-8dae-83bd79cf0210', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.approve', '审核会签');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffa0-7c71-b22c-0356a500a63c', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.alarm.launch-8d', '告警发起8D');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffa5-70b1-9e00-378e65b7fa07', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patl.task.record', '巡检记录');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffaa-7a6a-86fe-d929fb0deec8', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patl.task.close', '巡检任务关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffb0-7366-83d9-70cd090ba9c2', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.std.delete', '标准删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffb6-7ccf-9c33-fce6dc3779da', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patl.route.delete', '路线删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffbb-7a26-950b-d61111f493f4', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.delete', '审核删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffbf-7af3-8ec3-765f6364cfde', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.supplier.delete', '供应商删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffc4-7b56-82a2-d744f9809034', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.param.delete', '参数删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffc8-761a-a630-14b693a34126', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.defect.delete', '字典删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffcd-7d5f-9090-ab8e6ba5f5b8', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d.delete', '8D删除');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffe0-7ccc-a40a-7d1913057831', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.std.create', '标准管理');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffe8-7afa-b1b6-ce9bd45f31dc', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.task.create', '任务录入');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffee-7cf7-ab38-026f0c12d6f8', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.task.submit', '签名提交');
INSERT INTO ops.sys_button VALUES ('019fd5f3-fffa-7adb-8669-9ec0b9f6f9b4', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.param.create', '参数管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0003-7c2b-ab52-a555996c39a1', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.subgroup.create', '子组录入');
INSERT INTO ops.sys_button VALUES ('019fd5f4-000c-7a9a-8c48-c732f3525ccc', '019fd5dc-51d5-7192-bb54-c37eb0729def', 'spc.alarm.close', '告警关闭');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0022-78b2-b252-7d7b36cdd2bb', '019fd5f4-0019-7526-b0c0-f03a30f69e2b', 'spc.process.list', '工序查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-002b-7b41-9482-18b6c25c8531', '019fd5f4-0019-7526-b0c0-f03a30f69e2b', 'spc.process.create', '工序管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0034-7917-8ce4-74b457ffecc2', '019fd5f4-0019-7526-b0c0-f03a30f69e2b', 'spc.process.delete', '工序删除');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0045-769a-9051-eb64d0aca660', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.defect.create', '字典管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-004d-737d-ac0b-30720f70ebb3', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.record.create', '记录录入');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0057-7b05-a448-4c9eeb2c3f3d', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d.create', '8D创建/编辑');
INSERT INTO ops.sys_button VALUES ('019fd5f4-005b-7a4c-8467-a24451d1795c', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d.advance', '8D阶段推进');
INSERT INTO ops.sys_button VALUES ('019fd5f4-005f-7b53-9c2f-5deada294629', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d.approve', '8D审批(D3/D5/D7)');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0064-7cf3-a5ec-ccec1ec7c48e', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.8d.reopen', '8D重开');
INSERT INTO ops.sys_button VALUES ('019fd5f4-006d-7468-8043-9f7cec6e04b3', '019fd5dc-51d7-754a-839d-7f7265c42c50', 'ncm.capa.create', 'CAPA管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0077-74ac-94bd-ce2b80b0f4df', '019fd5dc-51d8-7eec-9055-4f5b6aaaa29d', 'ncm.trend.generate', '报表生成');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00b9-7773-9d85-698623a78690', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.supplier.create', '供应商管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00c4-7411-8606-85dcbb10c89e', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.audit.create', '审核管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00cf-7701-befb-6ee6bf24d01e', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.change.create', '变更管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00d9-7236-84e0-87d653c29592', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.trace.create', '追溯录入');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00e2-7ea1-a5e1-2f9894490bce', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.abnormal.create', '异常管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00ea-7516-bab4-5be9b6fef374', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea.create', 'FMEA管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00ef-797a-bf10-f3b5198d4bcd', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.fmea.edit', 'FMEA编辑');
INSERT INTO ops.sys_button VALUES ('019fd5f4-007f-7d23-8c80-76f5d3498050', '019fd5dc-51d9-7eaa-bd22-b3ca386fb084', 'sqm.supplier.list', '供应商查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0151-7ee9-8992-e8357cbae591', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patl.route.create', '路线管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-015b-79f2-a094-85c6ca167ed5', '019fd5dc-51da-77e7-a55d-16bea9adab3b', 'patl.task.create', '任务管理');
INSERT INTO ops.sys_button VALUES ('019fd658-1a3d-7a96-b07b-463a8a518139', '019fd5dc-51d8-7a2f-9b21-7369bf2077ae', 'sqm.abnormal.rule.config', '异常严重度规则配置');
INSERT INTO ops.sys_button VALUES ('019fdcb0-8a8b-79e5-ac30-29f461a2d4bc', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.sign.disposition', '处置签名');
INSERT INTO ops.sys_button VALUES ('019fdcb0-8a91-7aab-887f-9c1d3565c47b', '019fd5dc-51db-7e02-9a2f-9c209fd28445', 'system.delegation.manage', '代班管理');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00bf-7a2a-ba1e-a8b9499b619a', '019fd5dc-51d9-7408-97d3-e6ed0a92ecc4', 'sqm.audit.list', '审核查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00ca-7bc9-a277-55947aff32bd', '019fd5dc-51d9-7701-8c2c-835ecfff3e60', 'sqm.change.list', '变更查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00e6-7f2c-8c83-8415c5f346fb', '019fd5dc-51d9-7e0e-a59c-b2e6adda7747', 'sqm.fmea.list', 'FMEA查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00d4-74c2-9ae6-b88d5d11849d', '019fd5dc-51d9-7ee6-a120-45b73eda04f3', 'sqm.trace.list', '追溯查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-00dd-7242-b415-2375198593a0', '019fd5dc-51d9-7977-96dd-ff09b640a869', 'sqm.abnormal.list', '异常查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0049-72bd-9979-c9a2b8ed52f7', '019fd5dc-51d8-776c-8cb6-74903e6501d6', 'ncm.record.list', '记录查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0040-7d83-879b-7ed0704d54de', '019fd5dc-51d8-7c4c-8700-999b9e498a58', 'ncm.defect.list', '字典查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0052-776f-8785-17e161c8f635', '019fd5dc-51d8-7e86-9643-08356660d83c', 'ncm.8d.list', '8D查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0068-7a46-9758-c0103a70bdbb', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03', 'ncm.capa.list', 'CAPA查询');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffe4-76fc-97d2-849465f49649', '019fd5dc-51d5-7794-aee4-b73d0c73b3d0', 'fia.task.list', '任务查询');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffdb-7d40-9884-feaa85a7513c', '019fd5dc-51d5-7fca-92b3-22448bd8d8db', 'fia.std.list', '标准查询');
INSERT INTO ops.sys_button VALUES ('019fd5f3-fff6-7c52-8a06-ddb2f2560518', '019fd5dc-51d6-73ab-b38d-ea4c880f7fa1', 'spc.param.list', '参数查询');
INSERT INTO ops.sys_button VALUES ('019fd5f3-ffff-7a2e-8412-af4e7af7449c', '019fd5dc-51d6-73e5-8cb9-0d4d8694d3d9', 'spc.subgroup.list', '子组查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0008-726f-92c2-bccb8cf12b3a', '019fd5dc-51d6-7aa1-bb1c-5532bac6fc97', 'spc.alarm.list', '告警查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0010-733e-8b78-65e2605f21b1', '019fd5dc-51d6-7668-93fe-89b57b6f90b3', 'spc.capability.list', '能力分析');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0014-7e29-8cc9-74db7879d5f8', '019fd5dc-5591-71f5-9e46-03cba2242165', 'spc.rule.list', '判异规则');
INSERT INTO ops.sys_button VALUES ('019fd5f4-0156-7d43-a26d-609880b46a3a', '019fd5dc-5527-7837-b567-8b4b2e21ba59', 'patl.task.list', '任务查询');
INSERT INTO ops.sys_button VALUES ('019fd5f4-014c-7db7-aca2-970b225fd63d', '019fd5dc-5527-7cb5-86b8-8cc9741427a1', 'patl.route.list', '路线查询');
INSERT INTO ops.sys_button VALUES ('019fdf4f-6c05-7606-9d6f-b0361b7a5ddb', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03', 'ncm.ca.list', '纠正措施查询');
INSERT INTO ops.sys_button VALUES ('019fdf4f-6c08-7ad5-b209-939c22b5428c', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03', 'ncm.ca.create', '纠正措施管理');
INSERT INTO ops.sys_button VALUES ('019fdf4f-6c08-77e4-a43c-2476bcd1f6f4', '019fd5dc-557e-70c7-a36f-f7c7f61c6a03', 'ncm.ca.close', '纠正措施关闭');
INSERT INTO ops.sys_button VALUES ('019fe663-3e5a-7664-87ea-76e5e7b5c889', '019fd5dc-51d4-758c-80f7-0221d64ec698', 'fia.task.disposition', '处置路径');
INSERT INTO ops.sys_button VALUES ('019ff015-d957-7349-b988-16e46b953ec7', '019ff015-d932-7e5a-afc7-e96442821270', 'spc.sample-task.list', '抽样任务查询');
INSERT INTO ops.sys_button VALUES ('019ff015-d974-7d61-816c-48bb179b09a9', '019ff015-d932-7e5a-afc7-e96442821270', 'spc.sample-task.create', '抽样任务创建');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ed-7c14-9cf4-245d0b19c5db', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.create', '工装新增');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-75af-9d9d-fb4afd65d5c7', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.edit', '工装编辑');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-79a0-8e3d-60a6a557e8fe', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.delete', '工装删除');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7918-8ac1-7c777cf59a9b', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.scrap', '工装报废');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-77dc-880d-59679e3d1ff8', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.repair', '工装送修');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7b49-b04d-d3d5c1237f8d', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.lock', '工装锁定');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7acd-8127-9958d8b113dd', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.bind', '工装绑定');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7456-9948-ad2b11b2d382', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', 'tlm.maint.plan.create', '保养计划新建');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-746d-b7d5-15045e0fc5b6', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', 'tlm.maint.plan.edit', '保养计划编辑');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-76d5-b598-3905148f0875', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', 'tlm.maint.plan.delete', '保养计划删除');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-76c2-89b6-6ab7176ec64a', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', 'tlm.maint.record.create', '保养记录登记');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7082-b382-4c747ba98e40', '019ffe6f-b5eb-7ec9-b219-a39ca0ad2a4b', 'tlm.maint.record.delete', '保养记录删除');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-72eb-8cba-dfe5b9055f8b', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.repair.create', '维修工单新建');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-73a1-857b-49bfb1405291', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.repair.complete', '维修完成');
INSERT INTO ops.sys_button VALUES ('019ffe6f-b5ef-7c32-8bec-3a6f4605be89', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.scrap.approve', '报废审批');
INSERT INTO ops.sys_button VALUES ('01a0003f-9999-7acd-9965-5ac52e0ee107', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.first', '创建首件');
INSERT INTO ops.sys_button VALUES ('01a000e9-1184-728e-83ff-9d5dfdfe2732', '01a000e9-117b-7526-9c17-7c79cf2a0ebe', 'tlm.repair.list', '维修工单查看');
INSERT INTO ops.sys_button VALUES ('01a00103-ee67-7ab7-addf-f9bd311c97d2', '01a000e9-117b-7526-9c17-7c79cf2a0ebe', 'tlm.repair.approve', '工装维修审批');
INSERT INTO ops.sys_button VALUES ('01a00327-5a8a-7005-bb15-6c0a6a24f4c6', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.view', '计量查看');
INSERT INTO ops.sys_button VALUES ('01a00327-5a8a-761f-a10c-4b64b580ee29', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.calib', '校准录入');
INSERT INTO ops.sys_button VALUES ('01a00327-5a8a-709a-9715-40759b05e0e4', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.repair', '计量送修');
INSERT INTO ops.sys_button VALUES ('01a00327-5a8a-7c37-ab16-caaa07fd4f21', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.scrap', '计量报废');
INSERT INTO ops.sys_button VALUES ('01a00327-9634-719a-bca3-64a5d253fad9', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.export', '台账导出');
INSERT INTO ops.sys_button VALUES ('01a00350-4a6f-775a-8fd2-e1e33dfbd911', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.lock', '计量锁定');
INSERT INTO ops.sys_button VALUES ('01a003b8-5859-7209-a784-e24d83d51825', '019ffe6f-b5eb-7fef-8899-134a6bfc164b', 'tlm.tooling.list', '工装台账查看');
INSERT INTO ops.sys_button VALUES ('01a003b8-585b-7a4c-aa71-b4690188ac63', '01a00327-5a87-7dc3-ac27-29b4702f190d', 'tlm.metro.list', '计量管理查看');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-7931-9692-4f51124b4adb', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.list', '工单查看');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-788c-a0a3-7a8fc81703c4', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.create', '工单新建');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-7a13-8de0-3d9c0983d822', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.edit', '工单编辑');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-7c37-979b-710c2582d515', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.delete', '工单删除');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-7e64-b466-9205800b2f44', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.assign', '工单派单');
INSERT INTO ops.sys_button VALUES ('01a003d3-bc40-7d27-aba0-adf8398fa14f', '01a003d3-bc3d-7cd1-bbcb-2cb4cb7d70b9', 'cs.workorder.close', '工单评价闭环');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca68-776e-a7c0-042a75e32603', '01a003f0-ca57-79a9-ad2a-231407159c98', 'cs.satisfaction.list', '满意度查看');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca6c-74a6-822e-119da2192d88', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.list', '反馈查看');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca6c-7faf-82a2-c64667cc1854', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.create', '反馈登记');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca6c-75f1-bfbf-39ad6036965d', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.edit', '反馈编辑');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca6c-74d4-895b-0a0e9c19c03c', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.delete', '反馈删除');
INSERT INTO ops.sys_button VALUES ('01a003f0-ca6c-7c82-8e8f-982336b13b34', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.handle', '反馈处理');
INSERT INTO ops.sys_button VALUES ('01a00441-5c73-7bc7-a4f4-26fdf3a65f9c', '01a00441-5c6d-7273-b552-36d597ef7989', 'qms-mgmt.goal.list', '目标查看');
INSERT INTO ops.sys_button VALUES ('01a00441-5c73-7a99-8a1d-9afeaf0681db', '01a00441-5c6d-7273-b552-36d597ef7989', 'qms-mgmt.goal.create', '目标新建');
INSERT INTO ops.sys_button VALUES ('01a00441-5c73-7648-9abe-95a1635f07f5', '01a00441-5c6d-7273-b552-36d597ef7989', 'qms-mgmt.goal.edit', '目标编辑');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-7fb8-bb1a-6b2470470d49', '01a00441-5c6d-7273-b552-36d597ef7989', 'qms-mgmt.goal.delete', '目标删除');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-7021-a1ac-be07f1a35ec2', '01a00441-5c6e-7f0a-ad28-54743f700ff8', 'qms-mgmt.audit.list', '内审查看');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-72ef-b969-ba301b61ea89', '01a00441-5c6e-7f0a-ad28-54743f700ff8', 'qms-mgmt.audit.create', '内审新建');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-79a4-ace3-9c78ec267c4c', '01a00441-5c6e-7f0a-ad28-54743f700ff8', 'qms-mgmt.audit.edit', '内审编辑');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-7440-9330-a4ea83c084e2', '01a00441-5c6e-7f0a-ad28-54743f700ff8', 'qms-mgmt.audit.delete', '内审删除');
INSERT INTO ops.sys_button VALUES ('01a00441-5c74-79df-8330-a906ed9d5b22', '01a00441-5c6e-7f0a-ad28-54743f700ff8', 'qms-mgmt.audit.nc', '不符合项维护');
INSERT INTO ops.sys_button VALUES ('01a00441-5c75-7bfe-8b29-9a52069a7c8c', '01a00441-5c6e-759e-b369-f35454a11edc', 'qms-mgmt.adverse.list', '事件查看');
INSERT INTO ops.sys_button VALUES ('01a00441-5c75-761a-8375-e5a8e2beb0dd', '01a00441-5c6e-759e-b369-f35454a11edc', 'qms-mgmt.adverse.create', '事件登记');
INSERT INTO ops.sys_button VALUES ('01a00441-5c75-750e-a883-941a5dc1f6b8', '01a00441-5c6e-759e-b369-f35454a11edc', 'qms-mgmt.adverse.edit', '事件编辑');
INSERT INTO ops.sys_button VALUES ('01a00441-5c75-7697-bfce-996f6bb68114', '01a00441-5c6e-759e-b369-f35454a11edc', 'qms-mgmt.adverse.delete', '事件删除');
INSERT INTO ops.sys_button VALUES ('01a00441-5c75-73db-a514-d315d5299b18', '01a00441-5c6e-771a-a4cc-e605d0ff97da', 'qms-mgmt.feedback.list', '反馈分析查看');
INSERT INTO ops.sys_button VALUES ('01a00441-5c76-77aa-84c0-24c74a9091f5', '01a00441-5c6e-7554-be66-f8efb6cdc2d9', 'qms-mgmt.dashboard.list', '看板查看');
INSERT INTO ops.sys_button VALUES ('01a00621-b8b6-7b03-b3ed-354a3f25765b', '01a003f0-ca5e-7d7f-9f09-a53d1828e380', 'cs.feedback.link', '反馈联动');
INSERT INTO ops.sys_button VALUES ('01a008f2-6b79-7b44-a51f-415ebe8cdc9c', '01a008f2-6b72-7609-abe9-0dae1e9572c0', 'tlm.repair.analysis', '维修根因分析查看');
INSERT INTO ops.sys_button VALUES ('01a008f2-6bd8-74a4-bf1a-94ff3bc5dc00', '01a008f2-6bd6-71f7-9014-82fbcdf47619', 'tlm.metro.collect', '计量数据采集录入');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: sys_attachment; Type: TABLE DATA; Schema: ops; Owner: -
--



--
-- PostgreSQL database dump complete
--



