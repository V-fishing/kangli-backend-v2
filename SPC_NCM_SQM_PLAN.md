# SPC/NCM/SQM 三模块实现方案

## 总体策略

复用 FIA 模板(entity/mapper/service/controller + @PreAuthorize + dataScope + 状态机)。
分 3 轮:本轮 SPC,后续 NCM、SQM。

## 本轮:SPC 过程能力(12 表,V04 已建)

### 实体+Mapper(qms-domain/spc,6 组)
- `SpcParam`(extends BaseEntity, orgId)— 参数配置
- `SpcSubgroup`(plain, orgId, 分区表)— 子组数据(id ASSIGN_UUID + subgroup_time 复合 PK,仅 insert+select)
- `SpcMeasurement`(plain, orgId)— 测量值
- `SpcAlarm`(extends BaseEntity, orgId)— 告警
- `SpcCapability`(plain, orgId)— 能力快照
- `SpcRule`(plain, orgId nullable)— WECO 判异规则(V07 已种子)

### Service(qms-service/spc,5 个)
- `SpcParamService`:list/get/create/update/delete
- `SpcSubgroupService`:list/get/**create**(录 values[] -> 自动算 xbar/rangeR/n/judge + 创建 measurements + 超限生成告警)
- `SpcAlarmService`:list/**close**(closeReason + disposition)
- `SpcCapabilityService`:list/**calc**(按 param+period 查子组 -> 算 CPK/PPK/level -> 存快照)
- `SpcRuleService`:list/**toggle**(enable/disable)

### Controller(qms-api/spc,5 个)
- `SpcParamController` `/api/v1/spc/params` — CRUD,@PreAuthorize spc.param.*
- `SpcSubgroupController` `/api/v1/spc/subgroups` — list/create,@PreAuthorize spc.subgroup.*
- `SpcAlarmController` `/api/v1/spc/alarms` — list/close,@PreAuthorize spc.alarm.*
- `SpcCapabilityController` `/api/v1/spc/capability` — list/calc/trend,@PreAuthorize spc.capability.list
- `SpcRuleController` `/api/v1/spc/rules` — list/toggle,@PreAuthorize spc.rule.list

### DTO
- `CreateSubgroupRequest`(paramId, subgroupTime, shift, woNo, batchNo, values[])
- `CloseAlarmRequest`(closeReason, disposition)

### 关键业务逻辑
- **子组录入**:values[] -> xbar=avg, rangeR=max-min, n=count。judge=正常(简化;WECO 判异待后续接 spc_rule)。若 xbar 超控制限(spc_control_limit) -> judge=异常 + 创建 spc_alarm。
- **CPK 计算**:按 paramId+period 查子组 -> 组内均值+组内标准差(CPK)+ 总标准差(PPK) -> CPK=min(USL-mean,mean-LSL)/(3σ_within), level: 充足(≥1.33)/尚可(1.0-1.33)/不足(<1.0)。存 spc_capability 快照。

### 权限种子(DataInitializer seedSpcPerms)
spc 菜单 + 按钮:spc.param.list/create、spc.subgroup.list/create、spc.alarm.list/close、spc.capability.list、spc.rule.list -> sysadmin

### 暂不做(后续深化)
批量导入、采集任务、看板、供应商能力、控制限自动计算(前25子组)、WECO 判异引擎(LiteFlow)

## 后续轮次(概要)

### NCM 不良管理(9 表,V05 已建)
- 实体:NcmDefectDict/Record/CorrectiveAction + Qms8dReport/StageDetail + QmsCapa/Action(7 组)
- 核心:不良字典 CRUD、不良记录 CRUD+6维查询、8D D1->D8 流转(含审批)、CAPA 创建+进度+关闭
- 权限:ncm.defect.*/ncm.record.*/ncm.8d.*/ncm.capa.*

### SQM 供应商质量(38 表,V06 已建)
- 实体:SqmSupplier + SqmAuditPlan/Record/Nc + SqmChangeOrder/Approval + SqmIncomingLot/TraceNode + SqmIncomingAbnormal + QmsFmeaRisk(~10 组)
- 核心:供应商 CRUD、审核(计划/执行/NC)、物料变更(会签+一票否决)、来料追溯(递归树)、异常整改、FMEA
- 权限:sqm.supplier.*/sqm.audit.*/sqm.change.*/sqm.trace.*/sqm.abnormal.*/sqm.fmea.*

## 验证(本轮 SPC)
1. 建参数(注塑压力,USL/LSL)
2. 录子组(5 个值)-> 自动算 xbar/range -> 查
3. 关闭告警(如有)
4. 算 CPK -> 查快照
5. @PreAuthorize(admin 全量,mzuser 403)
6. dataScope(admin 跨公司,mzuser 仅梅州)
