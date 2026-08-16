-- V46: 回填演示数据的 ext_json（按审核类型差异化的特有字段）
-- 与 DataInitializer 种子数据（15 种审核类型）一一对应。
-- 仅当 ext_json 为空时回填，避免覆盖用户/接口写入的数据。

UPDATE ops.sqm_audit_plan SET ext_json = '{"changeNo":"BG-2026-031","changeTitle":"电芯正极材料升级","affectedMaterial":"正极极片 CL-220","pilotBatches":3}'::jsonb
 WHERE plan_no = 'AUD-2026-0001' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"pilotResult":"试产合格","verificationBatches":3,"riskLevel":"高"}'::jsonb
 WHERE record_no = 'REC-2026-0001' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"certTypes":"营业执照/ISO9001/IATF16949","validTo":"2026-12-31","scope":"体系证书复核"}'::jsonb
 WHERE plan_no = 'AUD-2026-0002' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"certStatus":"资质有效","renewalPlan":"到期前3个月换证"}'::jsonb
 WHERE record_no = 'REC-2026-0002' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"auditYear":"2025","auditScope":"体系/过程/交付绩效","assessedGrade":"A"}'::jsonb
 WHERE plan_no = 'AUD-2026-0003' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"grade":"A(有条件)","score":"81.50","capaCount":2}'::jsonb
 WHERE record_no = 'REC-2026-0003' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"quarter":"2026-Q3","kpiFocus":"交付准时率/来料合格率","reviewCycle":"季度"}'::jsonb
 WHERE plan_no = 'AUD-2026-0004' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"batchNo":"LOT-20260605-07","defectDesc":"尺寸超差0.05mm","abnormalSource":"来料检验"}'::jsonb
 WHERE plan_no = 'AUD-2026-0005' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"disposition":"退货+停线整改","carNo":"CAR-2026-0021","verifiedBatches":3}'::jsonb
 WHERE record_no = 'REC-2026-0005' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"trigger":"客户端投诉","customer":"BYD","urgency":"高"}'::jsonb
 WHERE plan_no = 'AUD-2026-0006' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"stopLine":true,"triggerSystem":"来料检验系统","severity":"重大"}'::jsonb
 WHERE plan_no = 'AUD-2026-0007' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"targetGrade":"A","category":"结构件","preSelect":"预选目标等级"}'::jsonb
 WHERE plan_no = 'AUD-2026-0008' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"lastAuditNo":"AUD-2025-0009","reviewFocus":"纠正措施有效性","scopeYear":"2025"}'::jsonb
 WHERE plan_no = 'AUD-2026-0009' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"capaEffective":"有效","reviewGrade":"A","score":"88.00"}'::jsonb
 WHERE record_no = 'REC-2026-0009' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"processName":"正极涂布工序","vdaElement":"P3-P4","keyProcess":true}'::jsonb
 WHERE plan_no = 'AUD-2026-0010' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"cpk":"1.33","processCapability":"能力充足","ranking":"B"}'::jsonb
 WHERE record_no = 'REC-2026-0010' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"specialTopic":"RoHS限用物质","regulation":"GB/T 26572","sampleScope":"成品+在制品"}'::jsonb
 WHERE plan_no = 'AUD-2026-0011' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"noticeFree":true,"scope":"总装与仓储","leadTeam":"质量,SQE"}'::jsonb
 WHERE plan_no = 'AUD-2026-0012' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"applyNo":"APL-2026-013","systemStandard":"IATF16949","category":"电子元件"}'::jsonb
 WHERE plan_no = 'AUD-2026-0013' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"admissionEligible":"具备准入条件","ncCount":1,"score":"86.50"}'::jsonb
 WHERE record_no = 'REC-2026-0013' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"customerReq":"客户碳足迹核查","addTopic":"碳足迹合规","standard":"ISO14067"}'::jsonb
 WHERE plan_no = 'AUD-2026-0014' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);

UPDATE ops.sqm_audit_plan SET ext_json = '{"prevAuditNo":"AUD-2026-0005","reauditReason":"重大异常整改后复评","scope":"整改项复核"}'::jsonb
 WHERE plan_no = 'AUD-2026-0015' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
UPDATE ops.sqm_audit_record SET ext_json = '{"recoveryStatus":"可恢复批量供货","reauditScore":"90.00","verifiedBatches":3}'::jsonb
 WHERE record_no = 'REC-2026-0015' AND (ext_json IS NULL OR ext_json = 'null'::jsonb);
