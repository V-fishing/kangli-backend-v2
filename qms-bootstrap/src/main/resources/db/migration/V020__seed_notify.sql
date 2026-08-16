-- ============================================================================
-- 种子数据 - 通知配置种子 (SEED)
-- ----------------------------------------------------------------------------
-- 内容 : notify_config, notify_channel, spc_notify_channel
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
-- Data for Name: notify_config; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7a17-9aed-43e0b0dfefcf', 'fia', 'fia_task_released', '首件判定合格放行', 'inspector,shiftleader,qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-75a3-bb7c-78fdb2e422c1', 'spc', 'spc_collect_missing', 'SPC采集缺失告警', 'shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-790a-85e1-3667ee8f0514', 'patrol', 'patrol_task_created', '巡检任务已创建', 'inspector,shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7728-b620-5f1295d384be', 'schedule', 'patrol_overdue', '巡检任务超期', 'inspector,shiftleader,qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7d82-8a19-9e9bd3e999d9', 'spc', 'spc_collect_due_soon', 'SPC采集临期提醒', 'shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7403-99c2-f5ec4c332037', 'sqm', 'sqm_fmea_overdue_escalate', 'FMEA措施超期升级(≥14天)', 'qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-70b8-8174-c20da9d7dce2', 'sqm', 'sqm_audit_nc_closed', '审核不符合项已闭环', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7236-bde0-67cb0d5ed931', 'spc', 'spc_collect_done', 'SPC采集完成回执', 'shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7c4d-ab5e-9f23704a4bd3', 'fia', 'fia_task_created', '首件任务创建待检', 'inspector,shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7e1a-a1cb-99ae30fd0a15', 'spc', 'spc_collect_assigned', 'SPC采集任务下发', 'shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7fb9-a559-b83c84917a47', 'sqm', 'sqm_change_submitted', '物料变更待审批', 'purchaser,rd,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7af3-ad3d-7040f41e5d3f', 'sqm', 'sqm_audit_nc_major', '审核严重不符合项', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7716-8eae-68dbfb55f4b0', 'schedule', 'audit_nc_overdue', '审核NC整改超期', 'qmanager,purchaser', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7c6d-8fb4-ef5a1aba3883', 'ncm', 'ncm_capa_created', '纠正措施已创建', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-729d-926d-bea874e442a6', 'ncm', 'ncm_capa_closed', '纠正措施已关闭', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-761a-a337-1beebaee31a6', 'patrol', 'patrol_task_closed', '巡检任务手动关闭', 'inspector,shiftleader,qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-743c-84b7-4aa7ace0f0da', 'patrol', 'patrol_task_abnormal', '巡检点检查异常', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7a3f-91b1-82bc9f913eea', 'fia', 'fia_task_rejected', '首件判定不合格拦截', 'qmanager,shiftleader', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-741f-b545-5058e2fb3d5f', 'sqm', 'sqm_abnormal_overdue_escalate', '来料异常超期升级(≥14天)', 'qmanager,purchaser', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7f1e-a298-3fc99f94f828', 'schedule', 'capa_overdue', '纠正措施超期', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7362-951c-8e255998ba76', 'schedule', 'fia_overdue', '首件检验超期', 'inspector,shiftleader,qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7166-80e3-48777fc9b28b', 'patrol', 'patrol_task_completed', '巡检任务全部完成', 'inspector,shiftleader,qmanager', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-77ab-b1af-c54914fa160b', 'sqm', 'sqm_abnormal_overdue', '来料异常超期提醒', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7be9-ae26-3538f07f0691', 'sqm', 'sqm_fmea_overdue', 'FMEA措施超期提醒', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7865-8433-387a33146124', 'sqm', 'sqm_audit_nc_overdue', '审核NC整改超期', 'qmanager,purchaser', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7c54-ade6-100505fc76cc', 'sqm', 'sqm_audit_plan_created', '审核计划已创建', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7ec2-b6be-05a2b26f342f', 'ncm', 'ncm_8d_status', '8D 报告状态变更', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7937-afdd-c77b53f5c1d7', 'ncm', 'ncm_capa_completed', '纠正措施已完成', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7426-9f39-96a6d5f13045', 'sqm', 'sqm_change_result', '物料变更审批结果', 'purchaser,rd,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1849-7c20-996d-3cff6d219664', 'sqm', 'sqm_audit_plan_started', '审核计划已开始', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019fd5f2-1871-716d-8730-226977ce2ac7', 'ncm', 'ncm_assign', '不良/来料异常发起指派', 'qmanager,sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019ffe6f-b61b-759c-a880-40c10553aff9', 'tlm', 'tlm_calib_expiry', '测量设备校准到期预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019ffe6f-b61b-7a6e-9995-d8b031cb61a8', 'tlm', 'tlm_maint_due', '工装保养到期预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019ffe6f-b61b-7239-ac1a-1a908b1de0d0', 'tlm', 'tlm_life_over', '工装寿命超限预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019ffe6f-b61b-70dc-8547-3a398019b806', 'tlm', 'tlm_repair_created', '工装送修待处理', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('019ffe6f-b61b-738f-89a5-26a7b4f408fa', 'tlm', 'tlm_scrap_applied', '工装报废待审批', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00327-5a90-7cb9-bad4-e1f8619d4b66', 'tlm', 'tlm_calib_plan_created', '计量器具校准计划生成', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00350-4a67-7864-8b00-1f36d0c1ac3d', 'tlm', 'tlm_repair_done', '计量器具维修完成待校准', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00350-4a6c-7db9-b195-179675ccbf17', 'tlm', 'tlm_calib_due', '计量器具校准临期预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a0041a-3f3c-7d0c-95fe-5a5f3fdfb004', 'cs', 'cs_wo_created', '售后新工单待处理', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a0041a-3f3e-77b8-82aa-5f9360605aa4', 'cs', 'cs_wo_assigned', '售后工单已指派', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a0041a-3f3e-7eb7-993f-6d566829f019', 'cs', 'cs_fb_created', '客户反馈登记', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00422-c048-7428-9c3c-5c37680fd9e2', 'cs', 'cs_wo_overdue', '售后工单超时预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00441-5ca4-7a0e-8baa-b3ced94d0a62', 'qms-mgmt', 'qms_audit_nc_created', '内审不符合项新增', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00441-5ca4-7612-bac2-7ecaa58ba2b6', 'qms-mgmt', 'qms_adverse_created', '不良事件登记', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00621-b8c1-78e8-b791-6ca58fa0077d', 'cs', 'cs_fb_lowscore', '低分反馈自动流转', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00621-b8c1-7cd2-9267-eecbbc4a8755', 'cs', 'cs_fb_ncm', '反馈联动质量改进', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00621-b8e4-7a46-9beb-b888b262435d', 'qms-mgmt', 'qms_health_warn', '体系健康度预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00621-b8e4-7180-bb76-4743615ffa64', 'qms-mgmt', 'qms_goal_warn', '质量目标未达标预警', 'sqe', '站内弹窗', true, NULL, NULL);
INSERT INTO ops.notify_config VALUES ('01a00621-b919-7eae-b165-7ba4596ee5d4', 'tlm', 'tlm_repair_verify_fail', '工装验证不通过已锁定', 'sqe', '站内弹窗', true, NULL, NULL);


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: notify_channel; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.notify_channel VALUES ('019fd5f2-1886-7bab-add5-d406cc58d9b2', '邮件', '', false, '普通', '邮件(SMTP, 点对点)', 'direct', '{"type":"mail","host":"smtp.test.com","port":465,"username":"notify@test.com","password":"mailpass_test_001","from":"notify@test.com","ssl":true}');
INSERT INTO ops.notify_channel VALUES ('019fd5f2-1885-7171-b3d2-8da22e239378', '企业微信应用消息', '', false, '普通', '企业微信应用消息(点对点)', 'direct', '{"type":"wecom","corpId":"","agentId":"","secret":""}');
INSERT INTO ops.notify_channel VALUES ('019fd5f2-1887-7558-b324-cb391322047f', '短信', '', false, '普通', '短信(预留适配器, 未配置服务商时发送记录失败)', 'direct', '{"type":"sms","provider":"","appKey":"","appSecret":"","signName":"","templateCode":""}');
INSERT INTO ops.notify_channel VALUES ('019fd5f2-183d-76d1-82db-b1a5ee87a388', '企业微信', 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=08c8fd58-a786-4c84-9499-f0ca3cc3c3b9', true, '普通', '企业微信机器人 webhook', 'webhook', NULL);
INSERT INTO ops.notify_channel VALUES ('019fd5f2-183e-729b-9f66-2aaccc1c1f92', '钉钉', '', false, '普通', '钉钉机器人 webhook', 'webhook', NULL);
INSERT INTO ops.notify_channel VALUES ('019fd5f2-183f-7acd-a250-4f92ecc1f69f', '自定义Webhook', '', false, '普通', '自定义 webhook 地址', 'webhook', NULL);
INSERT INTO ops.notify_channel VALUES ('019fd5f2-1884-7f20-a8f9-9d8a2b26b844', '钉钉应用消息', '', true, '普通', '钉钉工作通知(应用消息, 点对点)', 'direct', '{"type":"dingtalk","appKey":"dingappkey_test_001","appSecret":"dingappsecret_test_001","agentId":"123456789"}');
INSERT INTO ops.notify_channel VALUES ('019fd5f2-183c-787a-9444-986173de3d09', '站内弹窗', '', true, '普通', '系统站内信(默认开启)', 'inapp', '{"type":"inapp"}');


--
-- PostgreSQL database dump complete
--




--
-- PostgreSQL database dump
--



-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Data for Name: spc_notify_channel; Type: TABLE DATA; Schema: ops; Owner: -
--

INSERT INTO ops.spc_notify_channel VALUES ('019fd5dc-532b-7b7e-b64a-9d40e78562fe', NULL, '站内弹窗', true, '{}');
INSERT INTO ops.spc_notify_channel VALUES ('019fd5dc-532d-75d7-8710-4e1c5ce9f081', NULL, '自定义Webhook', false, '{"webhook": ""}');
INSERT INTO ops.spc_notify_channel VALUES ('019fd5dc-532b-7e15-a6a4-73e8d82da066', NULL, '企业微信', false, '{"webhook": ""}');
INSERT INTO ops.spc_notify_channel VALUES ('019fd5dc-532c-7810-8145-ef01cdaddfeb', NULL, '钉钉', false, '{"webhook": ""}');


--
-- PostgreSQL database dump complete
--



