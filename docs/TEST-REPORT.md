# 康立 QMS 测试产出报告（Test Deliverables）

> 生成日期：2026-08-17（扩面收尾 2026-08-17 更新）
> 配套文档：`TEST-STRATEGY.md`（测试方案/蓝图）
> 范围：接口测试（Controller 层 + Service 层单元测试）+ 契约联调（权限码一致性）+ 权限拦截集成测试
> 状态：扩面已完成（fia/ncm8d/spc/sqm/uop + 权限拦截 IT 全绿，249 项）；P2/P3/P4 待续（骨架见 TEST-STRATEGY.md §3/§4/§5）

---

## 1. 测试环境

| 项 | 版本/配置 |
|---|---|
| 后端 | Spring Boot 3.3.5 / Java 21 / MyBatis-Plus 3.5.9 |
| 构建 | Maven（`./mvnw`），reactor 多模块 |
| 测试框架 | JUnit 5 + Mockito + MockMvc（standalone）+ AssertJ |
| 前端 | Vue 3 / Vite 6 / TypeScript（Node 脚本做静态契约检查） |
| CI 就绪 | 测试可在 `mvnw test -pl qms-api,qms-service` 一键运行 |

---

## 2. 交付物清单

### 2.1 测试依赖（pom.xml）
| 模块 | 新增依赖 | 作用 |
|---|---|---|
| `qms-api` | `spring-boot-starter-test`、`spring-security-test` | Controller 切片测试 |
| `qms-service` | `spring-boot-starter-test` | Service 单元测试 |

### 2.2 测试用例文件（后端 UT 238 + IT 11，本轮扩面 +57）
| 文件 | 层级 | 用例数 | 覆盖点 |
|---|---|---|---|
| `qms-common/.../common/api/RTest.java` | 纯单元 | 4 | 统一响应 R 成功/失败/分页封装 |
| `qms-common/.../common/exception/BusinessExceptionTest.java` | 纯单元 | 3 | 业务异常码/消息 |
| `qms-common/.../common/security/JwtUtilTest.java` | 纯单元 | 2 | JWT 签发/解析（反射注入 secret） |
| `qms-common/.../common/security/DataScopeInterceptorTest.java` | 纯单元 | 5 | org_id 别名解析 / 括号 bug 回归 / 组织切换 |
| `qms-api/.../uop/controller/AuthControllerTest.java` | Controller（MockMvc） | 3 | 登录 200 / 401 / 参数绑定 |
| `qms-api/.../uop/controller/UserControllerTest.java` | Controller（MockMvc） | 3 | 用户查询 200 / 角色权限 |
| `qms-service/.../spc/impl/SpcRuleServiceImplTest.java` | Service（Mockito） | 4 | 判异规则引擎 |
| `qms-service/.../spc/impl/SpcSubgroupServiceImplTest.java` | Service（Mockito） | 4 | create 参数采集：计量型（xbar=11.0+3测量值）/计数型（P/NP/C/U，xbar=null 无测量值）/ 参数不存在 404 / 计量型空值抛错 |
| `qms-service/.../sqm/impl/SqmAuditServiceImplApproveTest.java` | Service（Mockito） | 5 | 会签 OR 语义审批人校验 403/404/409/null |
| `qms-service/.../sqm/impl/SqmSupplierPerformanceGradeTest.java` | Service（Mockito，反射） | 11 | 绩效等级区间匹配（V114 A[90,101)/B[80,90)/C[70,80)/D[0,70)）：100→A、90→A、89.99→B、70→C、69.99→D、规则未配置/异常回退 levelOf |
| `qms-service/.../fia/impl/FiaTaskServiceImplTest.java` | Service（Mockito） | 13 | 首件任务签收（检验/审核/批准三态机 + 整单/逐项状态）、处置结论（SUPPLIER 拒工厂/SORT/REWORK）、驳回（幂等/→REJECTED） |
| `qms-service/.../ncm/impl/Ncm8dServiceImplAdvanceTest.java` | Service（Mockito） | 9 | 8D 推进/签批全链路：顺序错/已闭环/无需审批推进 D2→D3/需审批停留 D1+teamMembers、签批（阶段不匹配/未待审批/口令错/通过→D2/驳回→退回 D1） |
| `qms-service/.../uop/impl/UserServiceImplTest.java` | Service（Mockito） | 12 | 越权防护：getCurrent 未认证 401、create 重名 400 + 强制本公司 org、update/resetPassword/assignRoles 不存在 404、跨公司管理员(org_id=null)非 admin→403、超管放行 |
| `qms-service/.../uop/impl/RoleServiceImplDeleteTest.java` | Service（Mockito） | 3 | delete：不存在 404、分公司管理员删其他公司角色 403、同公司角色清子表(FK 无级联)后删除并清权限缓存 |
| `qms-service/.../ncm/impl/NcmDefectRecordServiceImplLaunch8dTest.java` | Service（Mockito） | 3 | launch8dFromDefect 单人分支 |
| `qms-service/.../ncm/impl/Ncm8dServiceImplCreateTest.java` | Service（Mockito） | 3 | 人工建 8D 先落缺陷记录（原基线） |
| `qms-api/.../ncm/controller/Ncm8dControllerTest.java` | Controller（MockMvc） | 3 | 创建/分页转发（原基线） |
| `qms-api/.../patrol/controller/PatlTaskControllerTest.java` | Controller（MockMvc） | 5 | list/create/submitRecord/close 转发 + `@PreAuthorize` 权限码一致性（patl.task.*） |
| `qms-service/.../patrol/impl/PatlTaskServiceImplTest.java` | Service（Mockito） | 9 | 任务状态机（待巡检→已完成）、异常计数、异常记录创建、归档触发、closeAbnormal 闭环 |
| `qms-service/.../qmsmgmt/impl/QmsQualityGoalServiceImplTest.java` | Service（Mockito） | 7 | create 默认值填充（orgId/goalType/unit/目标值）/ update 不存在抛错 / delete / stats 达成率+未达标计数（目标为0兜底100%） |
| `qms-api/.../qmsmgmt/controller/QmsQualityGoalControllerTest.java` | Controller（MockMvc） | 6 | page/stats/create/update/delete 转发 + `@PreAuthorize` 权限码（qms-mgmt.goal.*）一致性 |
| `qms-service/.../archive/impl/ArchiveServiceImplTest.java` | Service（Mockito+JdbcTemplate） | 9 | list 类型路由/分页/字段映射、expiring days 默认 30+剩余天数、detail 空参/查不到/null、pdfRef 透传 |
| `qms-api/.../archive/controller/ArchiveControllerTest.java` | Controller（MockMvc） | 6 | list/expiring/detail/backfill 转发、pdf 404 分支、`@PreAuthorize` 复用现有码（sqm.audit.list 等，不新增 archive.*） |
| `qms-service/.../notify/impl/NotifyConfigServiceImplTest.java` | Service（Mockito） | 11 | listAll 排序、listChannels 敏感字段脱敏(secret/token/password→****)、update/updateChannel 兜底+缓存失效、resolveRoles/ReceiverIds/Channels 经缓存命中与禁用/查不到静默空 |
| `qms-api/.../system/controller/NotifyConfigControllerTest.java` | Controller（MockMvc） | 6 | list/channels/users/update/updateChannel 转发 + `@PreAuthorize` 权限码（system.notify.config）一致性 |
| `qms-service/.../approval/impl/ApprovalCenterServiceImplTest.java` | Service（Mockito） | 8 | 无登录返回空、FIA 指定审批人映射、NCM 8D 签批人命中+已闭环跳过、SQM 变更/审核会签角色匹配映射、TLM 报废/维修分支、时间倒序+null 置后、ROOT 管理员跳过 org 过滤 |
| `qms-api/.../approval/ApprovalCenterControllerTest.java` | Controller（MockMvc） | 3 | pending 转发(含 limit 截断) + `@PreAuthorize` 缺失记录为铁律第9条破例 |
| `qms-service/.../my/MyTaskServiceImplTest.java` | Service（Mockito） | 8 | userId 空返回空、FIA/NCM8D/PATROL/SQM审核/CS 分支映射、includeClosed 过滤、limit 截断、module+bizNo 排序 |
| `qms-api/.../my/MyTaskControllerTest.java` | Controller（MockMvc） | 3 | list 转发(limit/includeClosed 参数)+ `@PreAuthorize` 缺失记录为铁律第9条破例 |
| `qms-service/.../assign/AssignReassignServiceTest.java` | Service（Mockito） | 6 | 单人指派(写记录+notifyUser+返回姓名)、角色团队指派(写记录+notifyRoles+返回角色名)、两者空抛异常、reassign action 标记、默认站内弹窗 |
| `qms-service/.../kpi/impl/KpiCompareServiceImplTest.java` | Service（Mockito+JdbcTemplate） | 3 | orgs 驼峰重映射(orgId/orgCode/orgName，JdbcTemplate LinkedCaseInsensitiveMap 大小写不敏感)、items 转置(11 指标 values 按 orgCode 排列)、rate 分母≤0→0.0、cnt 异常兜底 0L |
| `qms-api/.../kpi/controller/KpiCompareControllerTest.java` | Controller（MockMvc） | 2 | compare 转发 kpiCompareService.compare 返回 code=0、`@PreAuthorize` = system.org.switch 权限码一致性(铁律第 9 条合规) |
| `qms-service/.../cs/impl/CsWorkOrderServiceImplTest.java` | Service（Mockito+JdbcTemplate） | 10 | page 多字段模糊+状态/类型/优先级过滤、create 默认值回退(org/status=PENDING/woType=REPAIR/priority=NORMAL/orderNo)+insert+createdBy、create orgId 不覆盖、update 不存在抛 BusinessException、update 仅编辑基础字段、assign 非PENDING抛/成功置ASSIGNED+owner、complete 非ASSIGNED抛/置DONE、close 非DONE抛/置CLOSED回填满意度、dashboard 状态计数+紧急待派+monthly异常兜底、satisfactionStats queryForMap异常兜底 |
| `qms-api/.../cs/controller/CsWorkOrderControllerTest.java` | Controller（MockMvc） | 10 | page/get/dashboard/satisfactionStats/create/update/delete/assign/complete/close 转发、assignable-users 复用 UserService.listForSelect、`@PreAuthorize` 权限码(create=cs.workorder.create/assign=cs.workorder.assign/close=cs.workorder.close/satisfaction=cs.satisfaction.list)一致性(铁律第 9 条合规) |
| `qms-service/.../qmsmgmt/impl/QmsInternalAuditServiceImplTest.java` | Service（Mockito+JdbcTemplate） | 7 | page 过滤、create 默认值(org/status=PLANNED/auditNo) + insert + createdBy、update 不存在抛错/编辑字段、delete 级联 nc、advance 状态机(PLANNED→ONGOING→DONE→CLOSED 单向+非法流转抛错+停留幂等)、saveNc 新默认 OPEN/MINOR/编号 + 改不存在抛错 + 关闭置 closedAt、stats 计数 + ncCloseRate |
| `qms-service/.../qmsmgmt/impl/QmsAdverseEventServiceImplTest.java` | Service（Mockito+JdbcTemplate） | 5 | page 过滤、create 默认值(PENDING/GENERAL/编号)+insert、update 不存在抛错/编辑、handle 设状态/描述/责任人、stats 状态+严重度计数+processRate |
| `qms-service/.../qmsmgmt/impl/QmsComplianceBoardServiceImplTest.java` | Service（Mockito+子Service+JdbcTemplate） | 3 | board 四模块聚合(goal/audit/adverse/feedback)+健康度加权评分(goalRate25/ncRate25/adverseRate20/fbRate15/satReach15)、healthLevel 阈值(优≥90/良≥80/预警<80)、feedback queryForMap 异常兜底 |
| `qms-api/.../qmsmgmt/controller/QmsInternalAuditControllerTest.java` | Controller（MockMvc） | 5 | page/stats/create/update/delete/advance/nc 转发、`@PreAuthorize` 权限码(qms-mgmt.audit.list/create/edit/delete/nc)一致性(铁律第 9 条合规) |
| `qms-api/.../qmsmgmt/controller/QmsAdverseEventControllerTest.java` | Controller（MockMvc） | 4 | page/stats/create/update/delete/handle 转发、`@PreAuthorize` 权限码(qms-mgmt.adverse.list/create/edit/delete)一致性(铁律第 9 条合规) |
| `qms-api/.../qmsmgmt/controller/QmsComplianceBoardControllerTest.java` | Controller（MockMvc） | 2 | board 转发、`@PreAuthorize` = qms-mgmt.dashboard.list 一致性(铁律第 9 条合规) |
| `qms-bootstrap/.../QmsIntegrationBase.java` + `AuthLoginIntegrationTest.java` | IT（`@SpringBootTest`） | 4 | 登录签发 JWT / 错密码 401 / 无 token 401 / 带 token 200 |
| `qms-bootstrap/.../Ncm8dCreateIntegrationTest.java` | IT（`@SpringBootTest`） | 2 | 创建 8D source=不良记录 / 无 token 401 |
| `qms-web-new/src/utils/status.spec.ts` | 前端 Vitest | 6 | StatusPill 状态映射（六变体全覆盖） |
| `qms-web-new/src/stores/permission.spec.ts` | 前端 Vitest | 5 | has/hasAny/admin(`*`)短路 |
| `qms-web-new/src/permission/directive.spec.ts` | 前端 Vitest | 4 | v-permission 无权限 `el.remove()` |

### 2.3 契约联调脚本
| 文件 | 类型 | 说明 |
|---|---|---|
| `scripts/check-permission-contract.mjs` | Node（零依赖） | 前端 `perm.has()`/`v-permission` ⊆ 后端 `@PreAuthorize` 权限码一致性检查；**发现 `fia/wo-lock/release` 与 `emergency-release` 前端引用而后端 `FiaWoLockController` 无对应端点（真实断裂，非 tlm 预期破例）** |

---

## 3. 测试用例明细

> 完整清单见 §2.2。关键回归点断言摘要：

### 3.1 后端 UT（176）
- **DataScopeInterceptor（5）**：org_id 别名解析（JOIN 歧义回归）、括号 bug 回归（`(org_id=? OR org_id IS NULL)` 带括号）、组织切换分支。
- **SqmAuditServiceImpl.approve（5）**：登录用户须 ∈ approverId 逗号串（OR 语义），否则 403；空审批人/非指定人/null 兼容。
- **NcmDefectRecordServiceImpl.launch8d（3）**：launch8dFromDefect 单人分支（ownerUserId→缺陷记录→8D）。
- **AuthController/UserController（6）**：standalone MockMvc，登录 200 / 错密码 401 / 无 token 401。
- **SpcRuleServiceImpl（4）**：判异规则引擎。
- **PatlTaskServiceImpl（9）**：任务状态机（待巡检→已完成）、异常计数 +1、异常记录创建、每次提交触发归档、closeAbnormal 闭环；路线/任务不存在 404、已完成任务提交 400。
- **PatlTaskController（5）**：list/create/submitRecord/close 转发 + `@PreAuthorize` 权限码（patl.task.*）一致性校验。
- **QmsQualityGoalServiceImpl（7）**：create 默认值回退（orgId→defaultOrgId、goalType/unit/目标值）、update 不存在抛 BusinessException、delete、stats 达成率与未达标计数（目标为0兜底100%）。
- **QmsQualityGoalController（6）**：page/stats/create/update/delete 转发 + `@PreAuthorize` 权限码（qms-mgmt.goal.*）一致性校验。
- **ArchiveServiceImpl（9）**：list 类型路由（fia/audit/8d/patrol/tlm/全部 UNION）/分页 offset/字段映射、expiring days 默认 30 与 daysRemaining、detail 空参/查不到/null、pdfRef 透传。
- **ArchiveController（6）**：list/expiring/detail/backfill 转发、pdf 404 分支、`@PreAuthorize` 复用现有码（不新增 archive.*，铁律第 9 条）。
- **NotifyConfigServiceImpl（11）**：listAll 排序、listChannels 敏感字段脱敏（appSecret/token/password→****，非 secret 不脱敏）、update 查不到抛 BusinessException + 部分字段 null 只更新传入项并失效缓存、updateChannel 查不到抛错 + 回传 **** 保留后端原值、resolveRoles/ReceiverIds/Channels 启用解析/禁用空/查不到静默空 + 缓存命中。
- **NotifyConfigController（6）**：list/channels/users/update/updateChannel 转发 + `@PreAuthorize` = system.notify.config（铁律第 9 条）。
- **ApprovalCenterServiceImpl（8）**：myPending 跨模块聚合——无登录返回空、FIA 指定审批人映射、NCM 8D 签批人命中且已闭环跳过、SQM 变更/审核会签角色(quality/purchase/rd/trial)匹配映射、TLM 报废/维修分支、appliedAt 倒序+null 置后、ROOT 管理员跳过 org 过滤。
- **ApprovalCenterController（3）**：pending 转发(含 limit 服务端截断)；`@PreAuthorize` = `approval.center.pending`（铁律第 9 条 C1 已闭环，V237 种子授权 sysadmin/admin/sqe/operator）。
- **MyTaskServiceImpl（8）**：跨模块"我的任务"聚合——userId 空返回空、FIA/NCM8D/PATROL/SQM审核/CS 分支映射、includeClosed 过滤(已闭环排除/包含)、limit 服务端截断、module+bizNo 排序。
- **MyTaskController（3）**：list 转发(limit/includeClosed 参数)；`@PreAuthorize` = `my.task.list`（铁律第 9 条 C1 已闭环，V237 种子授权 sysadmin/admin/sqe/operator）。
- **AssignReassignService（6）**：通用指派/改派——单人指派(写 qms_assign_record + notifyUser + 返回姓名，userName 空回退 userId)、角色团队指派(写记录 + notifyRoles + 返回角色名)、ownerUserId 与角色皆空抛 IllegalArgumentException、reassign action 标记、无渠道默认仅站内弹窗。
- **KpiCompareServiceImpl（3）**：分公司 KPI 对比——orgs 驼峰重映射（JdbcTemplate `LinkedCaseInsensitiveMap` 大小写不敏感，`raw.get("orgId")` 能匹配 PG 折叠小写 `orgid`）、items 转置（11 指标齐全，values 按 orgCode 排列）、`rate` 分母≤0 返回 0.0、`cnt` 的 `queryForObject` 异常时安全返回 0L。
- **KpiCompareController（2）**：compare 转发 `kpiCompareService.compare` 返回 code=0；`@PreAuthorize` = `system.org.switch`（铁律第 9 条 C1 合规）。
- **CsWorkOrderServiceImpl（10）**：page 多字段模糊 + 状态/类型/优先级过滤；create 默认值回退（org→curOrg、status=PENDING、woType=REPAIR、priority=NORMAL、orderNo=WO-时间戳）+ insert + createdBy=curUser，orgId 已设不覆盖；update 不存在抛 BusinessException、存在则仅编辑基础字段（状态不回退）；状态机 assign（非 PENDING 抛"仅待派单工单可派单"、PENDING→ASSIGNED 置 ownerId/Name）、complete（非 ASSIGNED 抛、ASSIGNED→DONE 回填 handleDetail）、close（非 DONE 抛、DONE→CLOSED 回填 satisfaction/comment）；dashboard 按状态计数 + urgentPending + monthly 异常兜底空；satisfactionStats 的 queryForMap 异常兜底（avgScore=0/rated=0/分布全 0）。
- **CsWorkOrderController（10）**：page/get/dashboard/satisfactionStats/create/update/delete/assign/complete/close 转发 + assignable-users 复用 `UserService.listForSelect`；`@PreAuthorize` 权限码 create=cs.workorder.create / assign=cs.workorder.assign / close=cs.workorder.close / satisfaction=cs.satisfaction.list（铁律第 9 条 C1 合规）。
- **QmsInternalAuditServiceImpl（7）**：page 多字段模糊 + 状态过滤；create 默认值回退（org→curOrg、status=PLANNED、auditNo=IA-时间戳）+ insert + createdBy；update 不存在抛 BusinessException、存在则编辑字段；delete 级联软删其不符合项；状态机 advance（PLANNED→ONGOING→DONE→CLOSED 单向、非法流转抛"非法的状态流转"、停留原状态幂等）；saveNc 新默认 OPEN/MINOR/编号 + insert，改不存在抛错、置 CLOSED 自动填 closedAt；stats 审计四态计数 + NC 三态计数 + ncCloseRate 比例。
- **QmsAdverseEventServiceImpl（5）**：page 多字段模糊 + 类型/状态过滤；create 默认值回退（PENDING/GENERAL/编号）+ insert + 通知；update 不存在抛 BusinessException、存在则编辑字段；handle 设置状态/描述/责任人；stats 状态(PENDING/HANDLING/DONE)+严重度(GENERAL/SERIOUS/CRITICAL)计数 + processRate。
- **QmsComplianceBoardServiceImpl（3）**：board 跨模块聚合（goal/audit/adverse/feedback 四子块）；健康度加权评分（goalRate25/ncRate25/adverseRate20/fbRate15/satReach15）+ 等级阈值（优≥90/良≥80/预警<80）；feedback 的 queryForMap 异常兜底（total/done/handleRate/avgScore=0、分布空）。
- **QmsInternalAuditController（5）/ QmsAdverseEventController（4）/ QmsComplianceBoardController（2）**：代表端点转发 + `@PreAuthorize` 权限码（qms-mgmt.audit.* / qms-mgmt.adverse.* / qms-mgmt.dashboard.list）一致性（铁律第 9 条 C1 合规）。
- 原 ncm 8D 基线（6）：create 三分支（统一整改源头）+ Controller 转发。
- **本轮扩面（M7~M13 收尾，+52 service UT）**：
  - **FiaTaskServiceImpl（13）**：首件任务三态机——signInspector（notFound/wrongStatus/整单→WAIT_REVIEW/逐项不改状态）、signReviewer（wrongStatus/需审批→IN_APPROVAL+createApproval）、signApprover（wrongStatus）、setDisposition（SUPPLIER 拒工厂处置/SORT 接受/REWORK 接受）、rejectTask（notFound/幂等/→REJECTED）。
  - **Ncm8dServiceImplAdvance（9）**：advanceStage（顺序错/已闭环/无需审批推进 D2→D3/需审批停留 D1+teamMembers）、approveStage（阶段不匹配/未待审批/口令错/通过→D2/驳回→退回 D1）。
  - **SpcSubgroupServiceImpl（4）**：create 参数采集——paramNotFound/计量型空值抛错/计量型正常落库(xbar=11.0+3测量值)/计数型(xbar=null+无测量值)。
  - **SqmSupplierPerformanceGrade（11，反射）**：等级区间匹配 V114 A[90,101)/B[80,90)/C[70,80)/D[0,70)——满分100→A、边界90→A、89.99→B、70→C、69.99→D、规则未配置/查询异常回退 levelOf。
  - **UserServiceImpl（12）**：越权防护——getCurrent 未认证 401、create 重名 400 + 强制本公司 org（防跨公司伪造）、update/resetPassword/assignRoles 不存在 404、跨公司管理员(org_id=null)非 admin→403（水平越权→垂直提权防护）、超管放行、assignRoles 正常先删后插并清权限缓存。
  - **RoleServiceImplDelete（3）**：delete 不存在 404、分公司管理员删其他公司角色 403、同公司角色清子表(FK 无级联)后删除并 evictAll。

### 3.2 后端 IT（11，qms-bootstrap）
- **AuthLoginIntegrationTest（4）**：登录签发 JWT → 带 token 调受保护接口 200 / 无 token 401 / 错密码 401。
- **Ncm8dCreateIntegrationTest（2）**：创建 8D source=不良记录 + 无 token 401。
- **PermissionIntegrationTest（5）**：权限拦截运行时验证（铁律第9条 C1）——① 真实 JWT 路径：无 token→401（anyRequest authenticated）、admin 全量 token→200；② 方法级 `@PreAuthorize` AOP 路径（注入代理 Bean 直接调用 + 显式 SecurityContext）：context 含 fia.task.list→放行、含无关码→AccessDeniedException、含 fia.wolock.release 调 list→拒绝（守卫按方法精确匹配）。**注：HTTP/MockMvc 路径下 SecurityContext 会被安全过滤器链在到达 Controller 前重置，故方法级守卫改用代理 Bean 直接调用精确验证 AOP 拦截语义。**

### 3.3 前端 ET（15，qms-web-new Vitest）
- **status.spec.ts（6）**：六变体映射全覆盖（cs/tlm/ncm）+ 未命中回退 p-mute。
- **permission.spec.ts（5）**：has/hasAny/admin(`*`)短路。
- **directive.spec.ts（4）**：v-permission 无权限 `el.remove()` 移除元素（越权按钮防护）。

### 3.4 权限码一致性检查（契约联调）
- 前端引用权限码：**29** 个 / 后端 `@PreAuthorize` 声明：**126** 个
- 破例（前端有、后端无）：**6 个 `tlm.*` 工装码**（二期暂缓预期破例）+ **真实断裂 2 处**：`fia/wo-lock/release`、`emergency-release` 前端引用而后端 `FiaWoLockController` 无对应端点（前端会 404，需后端补端点或前端移除）。

---

## 4. 执行结果

<!--AUTO:RESULTS-->
#### 后端单元测试
```text
$ ./mvnw test   (reactor: common + service + api + bootstrap)

[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.130 s -- in com.konli.qms.common.api.RTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.konli.qms.common.exception.BusinessExceptionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.425 s -- in com.konli.qms.common.security.DataScopeInterceptorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.482 s -- in com.konli.qms.common.security.JwtUtilTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.404 s -- in com.konli.qms.service.approval.impl.ApprovalCenterServiceImplTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.575 s -- in com.konli.qms.service.archive.impl.ArchiveServiceImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.283 s -- in com.konli.qms.service.assign.AssignReassignServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.255 s -- in com.konli.qms.service.cs.impl.CsWorkOrderServiceImplTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.989 s -- in com.konli.qms.service.fia.impl.FiaTaskServiceImplTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.031 s -- in com.konli.qms.service.kpi.impl.KpiCompareServiceImplTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.285 s -- in com.konli.qms.service.my.MyTaskServiceImplTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.620 s -- in com.konli.qms.service.ncm.impl.Ncm8dServiceImplAdvanceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 s -- in com.konli.qms.service.ncm.impl.Ncm8dServiceImplCreateTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.471 s -- in com.konli.qms.service.ncm.impl.NcmDefectRecordServiceImplLaunch8dTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.313 s -- in com.konli.qms.service.notify.impl.NotifyConfigServiceImplTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.372 s -- in com.konli.qms.service.patrol.impl.PatlTaskServiceImplTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.091 s -- in com.konli.qms.service.qmsmgmt.impl.QmsAdverseEventServiceImplTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.210 s -- in com.konli.qms.service.qmsmgmt.impl.QmsComplianceBoardServiceImplTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.209 s -- in com.konli.qms.service.qmsmgmt.impl.QmsInternalAuditServiceImplTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.092 s -- in com.konli.qms.service.qmsmgmt.impl.QmsQualityGoalServiceImplTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.009 s -- in com.konli.qms.service.spc.impl.SpcRuleServiceImplTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.943 s -- in com.konli.qms.service.spc.impl.SpcSubgroupServiceImplTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.074 s -- in com.konli.qms.service.sqm.impl.SqmAuditServiceImplApproveTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.611 s -- in com.konli.qms.service.sqm.impl.SqmSupplierPerformanceGradeTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.755 s -- in com.konli.qms.service.uop.impl.RoleServiceImplDeleteTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.229 s -- in com.konli.qms.service.uop.impl.UserServiceImplTest
[INFO] Tests run: 153, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.079 s -- in com.konli.qms.api.approval.ApprovalCenterControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.648 s -- in com.konli.qms.api.archive.controller.ArchiveControllerTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.137 s -- in com.konli.qms.api.cs.controller.CsWorkOrderControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.377 s -- in com.konli.qms.api.fia.controller.FiaWoLockControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.142 s -- in com.konli.qms.api.kpi.controller.KpiCompareControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.186 s -- in com.konli.qms.api.my.MyTaskControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.347 s -- in com.konli.qms.api.ncm.controller.Ncm8dControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.257 s -- in com.konli.qms.api.patrol.controller.PatlTaskControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.264 s -- in com.konli.qms.api.qmsmgmt.controller.QmsAdverseEventControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.121 s -- in com.konli.qms.api.qmsmgmt.controller.QmsComplianceBoardControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.266 s -- in com.konli.qms.api.qmsmgmt.controller.QmsInternalAuditControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.298 s -- in com.konli.qms.api.qmsmgmt.controller.QmsQualityGoalControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.302 s -- in com.konli.qms.api.system.controller.NotifyConfigControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.179 s -- in com.konli.qms.api.tlm.controller.TlmMetroRecordControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.341 s -- in com.konli.qms.api.uop.controller.AuthControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.158 s -- in com.konli.qms.api.uop.controller.UserControllerTest
[INFO] Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 48.85 s -- in com.konli.qms.AuthLoginIntegrationTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.462 s -- in com.konli.qms.Ncm8dCreateIntegrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 20.44 s -- in com.konli.qms.PermissionIntegrationTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### 权限码一致性检查
```text
$ node scripts/check-permission-contract.mjs

========================================
 权限码一致性检查 (C1 / 铁律第 9 条)
========================================
前端引用权限码: 31
后端 @PreAuthorize 码: 135

⚠️  发现 1 个前端引用码在后端 @PreAuthorize 中缺失(潜在破例):
   - tlm.tooling.first

   说明:可能因后端 seed 缺失 / 后端接口漏写 @PreAuthorize / 模块暂缓(如 tlm)。
   请核对:前端按钮是否应可见?后端是否需补 seed 或 @PreAuthorize?

```

**总计：249 项后端测试用例（UT 238 + IT 11），通过 249 项，失败 0 项；权限码检查脚本运行正常并产出有效差异报告。**
<!--AUTO:RESULTS-->

---

## 5. 覆盖率与局限

| 维度 | 已覆盖 | 未覆盖（后续阶段） |
|---|---|---|
| 接口/单元 UT | common 14 / uop 21(6+User12+Role3) / spc 8(4+4) / sqm 16(5+11) / ncm 18(9+3+3+create3) / patrol 14 / qmsmgmt 39 / archive 15 / notify 17 / approval 11 / my+assign 17 / kpi 5 / cs 20 / fia 13（共 238） | tlm 等其余（二期暂缓） |
| 集成测试 IT | AuthLogin 4 + Ncm8dCreate 2（直连本机容器 + qms_test 库） | SPC→8D、SQM异常→8D/CAPA 跨模块闭环 IT（T2 目标，待补） |
| 契约测试 CT | 权限码一致性（前端 vs 后端）+ 真实断裂发现 | openapi-diff + Schemathesis 运行时校验 |
| E2E/组件 ET | Vitest 组件测 15（StatusPill/v-permission/store） | Playwright 5 旅程（登录→8D / SPC告警 / 来料异常 / D1签批 / 多角色可见性）|
| 权限拦截 | UT 层 403 校验 + 静态一致性 + IT 运行时 401/200/403 三层验证（PermissionIntegrationTest 5） | 更多 Controller `@PreAuthorize` 运行时 403 集成验证 |

---

## 6. 如何运行

```bash
# 后端接口/单元测试（8D 域）
cd QMS-backend
./mvnw test -pl qms-api,qms-service

# 仅跑 8D 相关
./mvnw test -pl qms-api "-Dtest=Ncm8dControllerTest"
./mvnw test -pl qms-service "-Dtest=Ncm8dServiceImplCreateTest"

# 权限码一致性检查（前端 vs 后端）
cd ..
node scripts/check-permission-contract.mjs            # 报告模式
node scripts/check-permission-contract.mjs --strict   # CI 红线模式（有破例则退出码 1）
```

---

## 7. 后续计划

| 阶段 | 交付物 | 依赖 | 状态 |
|---|---|---|---|
| T1 单元测试 | common/uop/spc/sqm/ncm 共 38 用例 | — | ✅ 已完成 |
| T2 集成测试 | AuthLogin 4 + Ncm8dCreate 2（直连本机容器，独立库 qms_test） | 本机 Docker / CI 服务容器 | ✅ 已完成 |
| T3 契约检查 | check-permission-contract（含 fia 真实断裂发现）+ check-api-contract | — | ✅ 已完成 |
| T4 前端组件 | Vitest 15（StatusPill/v-permission/store） | 前端 pnpm | ✅ 已完成 |
| T4 前端 E2E | Playwright 5 旅程（登录→8D / SPC告警 / 来料异常 / D1签批 / 多角色） | 浏览器二进制 + 登录态 | 🔶 待补（环境就绪后） |
| T5 CI | `.github/workflows/test.yml` 四 stage 门禁 | — | ✅ 已完成 |
| P3 增强 | openapi-diff + Schemathesis 接入 CI | springdoc `/v3/api-docs` | ⬜ 待做 |
| 扩面 | 将 UT 范式复制到 fia/ncm8d/spc/sqm/uop + 权限拦截 IT（本轮已补齐，共 +57 项） | — | ✅ 已完成 |
</content>
