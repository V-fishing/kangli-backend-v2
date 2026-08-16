-- V169: 工装管理(TLM)初始数据导入。
-- 两份甲方台账: 工装夹具台账(TOOL) + 监视测量设备总表(GAUGE)。
-- org 默认 MZ(019f701f-0411-71ed-9eac-ab9440335832)。
-- 中文名(领用人/设备管理员/供应商)按 sys_user.real_name 反查 id 回填, 查不到则 NULL(不强行造用户)。
-- 幂等: 按 (org_id, tool_no) 先查后插。
DO $$
DECLARE v_org uuid;
BEGIN
  SELECT id INTO v_org FROM ops.sys_org WHERE org_code = 'MZ';
  IF v_org IS NULL THEN
    SELECT '019f701f-0411-71ed-9eac-ab9440335832'::uuid INTO v_org;
  END IF;

  -- ===== 1) 工装夹具台账(TOOL) =====
  -- 风险分类 II/III/IV -> risk_class; 软件版本 -> software_ver; 存放地点 -> location; 工装类型 -> tool_type
  INSERT INTO ops.tlm_tooling (id, org_id, tool_no, tool_name, tool_category, risk_class, software_ver, location, tool_type, status, created_at, inbound_date)
  SELECT ops.gen_uuid_v7(), v_org, v.tool_no, v.tool_name, 'TOOL', v.risk_class, v.software_ver, v.location, v.tool_type, 'IN_USE', now(), CURRENT_DATE
  FROM (VALUES
    ('SKL-J-20230001','气血平衡仪','II',NULL,'技术部','研发工装'),
    ('SKL-J-20230002','V5E乳酸电极膜定位工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20230004','BG3000试剂简易焊接工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20230005','比色皿压圈工装','II',NULL,'技术部','研发工装'),
    ('SKL-J-20230006','免疫模块调试工装','I','V1.0.0.230801','技术部','研发工装'),
    ('SKL-J-20230008','免疫试剂卡检具','III',NULL,'技术部','研发工装'),
    ('SKL-J-20240001','葡萄糖、乳酸电极检验工装','I','V2021.09.12.01','技术部','研发工装'),
    ('SKL-J-20240002','滴眼工装','I',NULL,'技术部','研发工装'),
    ('SKL-J-20250001','电极PCB大板清洗工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20250002','电极PCB大板清洗工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20250003','点胶托盘工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20250004','点胶托盘工装','III',NULL,'技术部','研发工装'),
    ('SKL-J-20250005','电极卡测试工装','II',NULL,'技术部','研发工装'),
    ('SKL-J-20260001','V5血气电解质分析仪活动门老化工装','II',NULL,'技术部','研发工装'),
    ('SKL-J-20260002','V5血气电解质分析仪活动门老化工装','II',NULL,'技术部','研发工装'),
    ('SKL-J-20260003','V8电极膜裁切工具','IV',NULL,'技术部','研发工装'),
    ('SKL-J-20260004','V8电极膜芯电焊夹具','III',NULL,'技术部','研发工装'),
    ('SKL-J-20260005','Q80000试剂盒切管夹具','III',NULL,'技术部','研发工装'),
    ('SKL-J-20260006','（截图未显示，编号SKL-J-20260006）',NULL,NULL,'技术部','研发工装'),
    ('SKL-J-20260007','（截图未显示，编号SKL-J-20260007）',NULL,NULL,'技术部','研发工装'),
    ('SKL-J-20260009','多通道GL电极检验工装','I','DT04DJJYG005 V1.0.0.20260430','技术部','研发工装'),
    ('SKL-J-20260010','多通道GL电极活化工装','I','DT04GLDJHHGZ030 V1.0.0.20260623','技术部','研发工装'),
    ('SKL-J-20260011','多通道GL电极活化工装','I','DT04GLDJHHGZ030 V1.0.0.20260623','技术部','研发工装'),
    ('SKL-J-20260012','电极卡带阻抗板测试工装','II',NULL,'技术部','研发工装')
  ) AS v(tool_no, tool_name, risk_class, software_ver, location, tool_type)
  WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tooling WHERE org_id = v_org AND tool_no = v.tool_no);

  -- ===== 2) 监视测量设备总表(GAUGE) =====
  -- 状态: 正常使用->IN_USE / 停用或备用->DISABLED / 97%报废->SCRAPPED
  -- 校准日期/下次校准日期/校准周期(年->天数) / 保养周期(月->天数) / 领用人->owner_id / 设备管理员->admin_id / 供应商->supplier_id
  INSERT INTO ops.tlm_tooling (id, org_id, tool_no, tool_name, tool_category, spec, precision_val, measure_point, software_ver,
       supplier_id, calib_date, calib_due_date, calib_cycle, maint_cycle, location, owner_id, admin_id, status, created_at, inbound_date)
  SELECT ops.gen_uuid_v7(), v_org, v.tool_no, v.tool_name, 'GAUGE', v.spec, v.precision_val, v.measure_point, v.software_ver,
       (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name = v.supplier AND o.org_code='MZ' LIMIT 1),
       v.calib_date::date, v.calib_due_date::date, v.calib_cycle, v.maint_cycle, v.location,
       (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name = v.owner AND o.org_code='MZ' LIMIT 1),
       (SELECT u.id FROM ops.sys_user u JOIN ops.sys_org o ON u.org_id=o.id WHERE u.real_name = v.admin AND o.org_code='MZ' LIMIT 1),
       v.status, now(), CURRENT_DATE
  FROM (VALUES
    ('A241101-597','弹簧全启式安全阀','A28X6T/8','0.25MPa',NULL,NULL,'宁波某医疗器械','2026-07-01','2027-06-30',365,30,'微生物室准备间','莫珍弟','莫珍弟','IN_USE'),
    ('MKL-A-20210024','1000L不锈钢反应釜','1000L',NULL,NULL,NULL,'深圳市汇兴源科技','2025-10-30','2026-10-29',365,30,'试剂洁净车间','黎智勇','黎智勇','IN_USE'),
    ('MKL-C-20210001','立式压力蒸汽灭菌器','TS-30B',NULL,NULL,NULL,'上海博讯实业有限','2022-04-24','2023-04-23',365,30,'品管部',NULL,'莫珍弟','SCRAPPED'),
    ('MKL-C-20210008','绝缘电阻测试仪','CS2675CX-1','±2%MΩ',NULL,NULL,'南京长盛仪器有限','2021-01-13',NULL,365,NULL,'品管部',NULL,'温桂萍','DISABLED')
  ) AS v(tool_no, tool_name, spec, precision_val, measure_point, software_ver, supplier, calib_date, calib_due_date, calib_cycle, maint_cycle, location, owner, admin, status)
  WHERE NOT EXISTS (SELECT 1 FROM ops.tlm_tooling WHERE org_id = v_org AND tool_no = v.tool_no);

  RAISE NOTICE 'TLM seed done: tooling rows = %', (SELECT count(*) FROM ops.tlm_tooling WHERE org_id = v_org);
END $$;
