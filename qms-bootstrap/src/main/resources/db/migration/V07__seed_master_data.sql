-- V07: 主数据种子 - sys_dict(枚举字典) + spc_rule(WECO 8) + qms_8d_stage_config(D1-D8)
-- sys_dict 按 DB 设计§18/§19 + 开工前 DDL V1.1 的 E1-E27 综合种子。

-- ============ sys_dict 通用字典表(DB 设计§19,UUID/ops 化) ============
CREATE TABLE ops.sys_dict (
  id          UUID PRIMARY KEY DEFAULT ops.gen_uuid_v7(),
  dict_type   VARCHAR(32) NOT NULL,
  dict_key    VARCHAR(32) NOT NULL,          -- 存 DB 值
  dict_value  VARCHAR(64) NOT NULL,          -- 显示文本
  sort_order  SMALLINT NOT NULL DEFAULT 0,
  enabled     BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), created_by UUID,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_by UUID,
  is_deleted BOOLEAN NOT NULL DEFAULT false, version INTEGER NOT NULL DEFAULT 0,
  UNIQUE(dict_type, dict_key)
);
COMMENT ON TABLE ops.sys_dict IS '通用字典(枚举集中管理)';

-- FIA
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('fia_task_status','待检','待检',1),('fia_task_status','进行中','进行中',2),('fia_task_status','已完成','已完成',3),('fia_task_status','超时','超时',4),('fia_task_status','已作废','已作废',5),
  ('fia_trigger_event','换模具','换模具',1),('fia_trigger_event','换批次','换批次',2),('fia_trigger_event','材料批次变更','材料批次变更',3),('fia_trigger_event','换设备','换设备',4),('fia_trigger_event','系统升级','系统升级',5),
  ('fia_approval_type','豁免开工','豁免开工',1),('fia_approval_type','紧急放行','紧急放行',2),('fia_approval_type','让步接收','让步接收',3),
  ('fia_sign_method','password','密码',1),('fia_sign_method','handwriting','手写',2),('fia_sign_method','ca','CA证书',3),
  ('fia_intercept_mode','硬阻断','硬阻断',1),('fia_intercept_mode','软阻断','软阻断',2),
  ('fia_sign_nodes','两级','两级(检验+复核)',1),('fia_sign_nodes','三级','三级(加批准)',2),
  ('fia_sign_granularity','整单签名','整单签名',1),('fia_sign_granularity','逐项签名','逐项签名',2),
  ('fia_multi_trigger','合并一张校验单','合并一张校验单',1),('fia_multi_trigger','各自生成','各自生成',2);

-- SPC
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('spc_alarm_level','预警','预警',1),('spc_alarm_level','报警','报警',2),
  ('spc_capability_level','充足','充足(CPK>=1.33)',1),('spc_capability_level','尚可','尚可(1.0-1.33)',2),('spc_capability_level','不足','不足(<1.0)',3),
  ('spc_chart_type','Xbar-R','Xbar-R',1),('spc_chart_type','Xbar-s','Xbar-s',2),('spc_chart_type','I-MR','I-MR',3),
  ('spc_collect_status','待采集','待采集',1),('spc_collect_status','已完成','已完成',2),('spc_collect_status','缺失','缺失',3);

-- NCM
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('ncm_defect_category','尺寸类','尺寸类',1),('ncm_defect_category','外观类','外观类',2),('ncm_defect_category','功能类','功能类',3),('ncm_defect_category','装配类','装配类',4),('ncm_defect_category','包装类','包装类',5),
  ('ncm_disposition','让步','让步',1),('ncm_disposition','返工','返工',2),('ncm_disposition','报废','报废',3),('ncm_disposition','降级','降级',4),
  ('ncm_fishbone_5m1e','人','人',1),('ncm_fishbone_5m1e','机','机',2),('ncm_fishbone_5m1e','料','料',3),('ncm_fishbone_5m1e','法','法',4),('ncm_fishbone_5m1e','环','环',5),('ncm_fishbone_5m1e','测','测',6),
  ('capa_status','待启动','待启动',1),('capa_status','进行中','进行中',2),('capa_status','已验证','已验证',3),('capa_status','已关闭','已关闭',4),
  ('esc_level','L1','L1 班组长(30min)',1),('esc_level','L2','L2 质量主管(60min)',2),('esc_level','L3','L3 质量经理(120min)',3);

-- SQM
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('audit_type','年度复审','年度复审',1),('audit_type','过程审核','过程审核',2),('audit_type','专项审核','专项审核',3),('audit_type','飞行检查','飞行检查',4),('audit_type','初次审核','初次审核',5),('audit_type','年度审核','年度审核',6),('audit_type','附加审核','附加审核',7),('audit_type','重新审核','重新审核',8),
  ('audit_nc_status','待整改','待整改',1),('audit_nc_status','整改中','整改中',2),('audit_nc_status','已整改','已整改',3),('audit_nc_status','已关闭','已关闭',4),
  ('change_status','待申请','待申请',1),('change_status','审批中','审批中',2),('change_status','已批准','已批准',3),('change_status','已执行','已执行',4),('change_status','已关闭','已关闭',5),('change_status','已驳回','已驳回',6),('change_status','已回滚','已回滚',7),
  ('abnormal_status','待处理','待处理',1),('abnormal_status','整改中','整改中',2),('abnormal_status','待验证','待验证',3),('abnormal_status','三批验证','三批验证',4),('abnormal_status','已关闭','已关闭',5),
  ('trace_node_type','incoming','来料',1),('trace_node_type','raw','原料',2),('trace_node_type','semi','半成品',3),('trace_node_type','ship','发货',4),('trace_node_type','customer','客户',5),('trace_node_type','process','工序',6),
  ('supplier_level','A','A',1),('supplier_level','B','B',2),('supplier_level','C','C',3),('supplier_level','D','D',4),
  ('supplier_status','合格','合格',1),('supplier_status','观察','观察',2),('supplier_status','整改中','整改中',3),('supplier_status','暂停','暂停',4),('supplier_status','淘汰','淘汰',5),
  ('sqm_nc_level','严重','严重',1),('sqm_nc_level','一般','一般',2),('sqm_nc_level','观察项','观察项',3),
  ('sqm_abnormal_disposal','退货','退货',1),('sqm_abnormal_disposal','特采','特采',2),('sqm_abnormal_disposal','挑选使用','挑选使用',3),('sqm_abnormal_disposal','报废','报废',4),
  ('sqm_change_role','quality','质量',1),('sqm_change_role','purchase','采购',2),('sqm_change_role','rd','研发',3),('sqm_change_role','trial','试产',4);

-- FMEA
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('fmea_status','待闭环','待闭环',1),('fmea_status','进行中','进行中',2),('fmea_status','已闭环','已闭环',3),
  ('fmea_risk_level','高','高(>=150)',1),('fmea_risk_level','中高','中高(100-149)',2),('fmea_risk_level','中','中',3),('fmea_risk_level','低','低',4);

-- 公共/系统
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('severity','严重','严重',1),('severity','一般','一般',2),('severity','轻微','轻微',3),
  ('user_status','启用','启用',1),('user_status','停用','停用',2),('user_status','锁定','锁定',3),
  ('org_type','公司','公司',1),('org_type','工厂','工厂',2),('org_type','车间','车间',3),('org_type','产线','产线',4),('org_type','工位','工位',5),
  ('common_status','pending','待处理',1),('common_status','processing','进行中',2),('common_status','done','已完成',3),('common_status','rejected','已驳回',4),('common_status','overdue','已超期',5);

-- 二期(TLM/MSM/QSM/ASM)
INSERT INTO ops.sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
  ('tlm_tooling_status','在用','在用',1),('tlm_tooling_status','维修中','维修中',2),('tlm_tooling_status','停用','停用',3),('tlm_tooling_status','报废','报废',4),
  ('tlm_repair_result','修复合格','修复合格',1),('tlm_repair_result','无法修复','无法修复',2),
  ('msm_gauge_status','合格','合格',1),('msm_gauge_status','限用','限用',2),('msm_gauge_status','超期','超期',3),('msm_gauge_status','失效','失效',4),('msm_gauge_status','锁定','锁定',5),('msm_gauge_status','报废','报废',6),('msm_gauge_status','维修中','维修中',7),
  ('msm_calib_plan_status','计划中','计划中',1),('msm_calib_plan_status','待执行','待执行',2),('msm_calib_plan_status','超期','超期',3),('msm_calib_plan_status','已完成','已完成',4),
  ('asm_low_score_cause','响应慢','响应慢',1),('asm_low_score_cause','维修不彻底','维修不彻底',2),('asm_low_score_cause','服务态度','服务态度',3),('asm_low_score_cause','配件缺货','配件缺货',4),
  ('qsm_health_dimension','质量目标','质量目标',1),('qsm_health_dimension','内审整改','内审整改',2),('qsm_health_dimension','不良事件','不良事件',3),('qsm_health_dimension','顾客满意','顾客满意',4),('qsm_health_dimension','合规率','合规率',5);

-- ============ spc_rule WECO 8 判异规则(默认启用①②③⑤) ============
INSERT INTO ops.spc_rule (rule_code, rule_name, level, is_enabled, sort_no) VALUES
  ('①','1点超出3σ','报警',true,1),
  ('②','连续3点中2点在A区(同侧2σ外)','报警',true,2),
  ('③','连续5点中4点在B区外(1σ外)','预警',true,3),
  ('④','连续8点在中心线一侧','预警',false,4),
  ('⑤','连续6点递增或递减','预警',true,5),
  ('⑥','连续14点交替上下','预警',false,6),
  ('⑦','连续15点在C区(1σ内)','预警',false,7),
  ('⑧','连续8点在B区外(1σ外)','预警',false,8);
COMMENT ON TABLE ops.spc_rule IS 'WECO 判异规则(①-⑧,默认启用①②③⑤)';

-- ============ qms_8d_stage_config 8D 阶段(D3/D5/D7 需审批;SLA: D3=24h D4-D5=7天 D6-D7=30天) ============
INSERT INTO ops.qms_8d_stage_config (stage_code, stage_name, sort_order, need_approval, sla_duration) VALUES
  ('D1','团队组建',1,false,NULL),
  ('D2','问题描述(5W2H)',2,false,NULL),
  ('D3','临时措施',3,true,'24 hours'),
  ('D4','根因分析',4,false,'7 days'),
  ('D5','永久纠正措施',5,true,'7 days'),
  ('D6','实施与验证',6,false,'30 days'),
  ('D7','预防再发',7,true,'30 days'),
  ('D8','闭环归档',8,false,NULL);
