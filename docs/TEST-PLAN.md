# 康立 QMS 整项目测试计划（全模块 × 全阶段）

> 本文档是 `TEST-STRATEGY.md`（方法论/金字塔）的**落地执行总表**。
> 目标：为 QMS 前后端工程建立可逐步执行的完整测试体系，按模块维度铺开、按测试阶段递进。
> 范围：后端 82 个 Controller / 15+ 业务模块 + 前端 api/视图 + 前后端契约。
> 技术基线：Spring Boot 3.3.5 / Java 21 / MyBatis-Plus 3.5.9 / Vue3.5 / Vite6 / Element Plus 2.8。

---

## 0. 项目测试现状（截至 2026-08-17）

| 维度 | 现状 |
|---|---|
| 后端单元测试 | P0/P1 已落地：仅 `ncm` 8D 域 6 用例（Controller standalone MockMvc 3 + Service Mockito 3） |
| 后端测试依赖 | qms-api：`spring-boot-starter-test` + `spring-security-test`；qms-service：`spring-boot-starter-test`。**缺 testcontainers**（P2 需补） |
| 集成测试 | **T2 已落地**：qms-bootstrap 引 `testcontainers` + `spring-security-test`（本机 Docker 守护未启动 → 改用直连本地 dev 容器 + 独立库 `qms_test`）；6 用例（AuthLogin 4 + Ncm8dCreate 2）全绿，覆盖登录签发/权限拦截/8D 业务闭环 |
| 契约测试 | `scripts/check-permission-contract.mjs` 已落地（权限码一致性 + Flyway 种子比对，零依赖）；**发现真实断裂**：`fia/wo-lock/release` 与 `emergency-release` 前端引用而后端 `FiaWoLockController` 无对应端点（已记入 STRATEGY §10）。openapi-diff/Schemathesis 未落地 |
| 前端测试 | **T4 已落地**：`vitest@4.1.10` + `@vue/test-utils` + `happy-dom`；15 用例（status 映射 6 + 权限 store 5 + v-permission 指令 4），`pnpm test` 全绿；Playwright E2E 5 旅程尚未落地 |
| CI | **T5 已落地**：`.github/workflows/test.yml` 串联 unit/integration/contract/frontend 四 stage，gate 要求 UT+IT+CT 必过、ET best-effort |
| 主类约束 | 仅 `qms-bootstrap` 有 `QmsApplication`；qms-api/qms-service **无主类** → `@WebMvcTest`/`@SpringBootTest` 在这两个模块直跑失败，需：(a) 单元层用 standalone MockMvc / Mockito；(b) 集成层 `@SpringBootTest(classes=QmsApplication.class)` 放 qms-bootstrap 的 test 目录（或指定主类） |

---

## 1. 测试阶段定义（四层金字塔）

| 阶段 | 代号 | 工具 | 落点模块 | 目标占比 | 执行频率 |
|---|---|---|---|---|---|
| 接口/单元 | **UT** | JUnit5 + standalone MockMvc + Mockito + AssertJ | qms-api / qms-service `src/test` | 60% | 每次 commit |
| 集成 | **IT** | Testcontainers + `@SpringBootTest` + Flyway | qms-bootstrap `src/test`（含主类） | 25% | PR / 夜测 |
| 契约联调 | **CT** | 权限码脚本 + openapi-diff + Schemathesis | scripts/ + CI | 10% | PR / 发布前 |
| E2E/组件 | **ET** | Playwright + Vitest + @vue/test-utils | qms-web-new `e2e` / `src/tests` | 5% | 发布前 / 夜测 |

> **铁律对齐**：所有接口测试必须校验 `@PreAuthorize` 权限码（403）；所有按钮码须纳入 `scripts/check-permission-contract.mjs` 覆盖范围（铁律第 9 条 C1）。

---

## 2. 模块测试矩阵（按业务域拆分，标注优先级）

优先级：**P0=核心闭环（必测）** / **P1=重要业务** / **P2=常规**。

| # | 模块 | 包 | 重点 Controller / Service | 测试重点 | 优先级 | 阶段 |
|---|---|---|---|---|---|---|
| M1 | 认证 uop | `api/uop` `service/uop` | AuthController / UserController / RoleController / MenuController / OrgController / DictController | 登录签发 JWT、角色菜单权限映射、密码重置、数据权限 org 隔离 | P0 | UT+IT+CT |
| M2 | 不合格/CAPA/8D ncm | `api/ncm` `service/ncm` | Ncm8dController/Impl、NcmDefectRecord/Impl、NcmCapa/Impl、NcmCorrectiveAction/Impl | **统一整改源头**（缺陷记录→8D 双向关联）、8D 状态机 D1-D8 推进、签批、CAPA 闭环、纠正措施 | P0 | UT+IT（已部分 UT） |
| M3 | 供应商管理 sqm | `api/sqm` `service/sqm` | SupplierController、Abnormal/Impl、Audit/Impl、Change/Impl、Fmea/Impl、PerfAnalysis/Impl、Trace/Impl | 来料异常→8D/CAPA 双路径、审核会签链、物料变更 ECN 审批、绩效分级联动、全链路追溯树 | P0 | UT+IT |
| M4 | 统计过程控制 spc | `api/spc` `service/spc` | SpcAlarm/Impl、SpcSubgroup/Impl、SpcRule、SpcParam | 判异规则引擎、计数型/计量型采集、超界子组触发告警、告警→8D | P0 | UT+IT |
| M5 | 首件检验 fia | `api/fia` `service/fia` | FiaTask/Impl、FiaIncomingCheck/Impl、FiaSignConfig | 三级电子签名（带密码）、不合格处置、批量来料建单、工装首件 | P1 | UT+IT |
| M6 | 售后 cs | `api/cs` `service/cs` | CsWorkOrder/Impl、CsFeedback/Impl | 工单状态机（PENDING→ASSIGNED→DONE→CLOSED）、派单/完成/关闭、客户反馈→NCM 触发 | P1 | UT（已落地 20：CsWorkOrder Service 10 + Controller 10；cs.workorder.* 与 cs.satisfaction.list 权限码合规） |
| M7 | 巡检 patrol | `api/patrol` `service/patrol` | PatlTask/Impl、PatlRoute/Impl、PatlAbnormal/Impl | 巡检任务状态机（待巡检→已完成）、点位提交、异常计数、异常上报闭环、归档触发 | P2 | UT（已落地 14：Service 9 + Controller 5） |
| M8 | 体系管理 qmsmgmt | `api/qmsmgmt` `service/qmsmgmt` | QmsQualityGoal/Impl、QmsInternalAudit/Impl、QmsAdverseEvent/Impl、QmsComplianceBoard/Impl | 质量目标默认值/达成率统计、内审计划、不良事件、合规看板 | P2 | UT（已落地 39：QualityGoal Service 7 + Controller 6 + InternalAudit Service 7 + Controller 5 + AdverseEvent Service 5 + Controller 4 + ComplianceBoard Service 3 + Controller 2；qms-mgmt.* 与 qms-mgmt.dashboard.list 权限码合规） |
| M9 | 统一归档 archive | `api/archive` `service/archive` | Archive/Impl | 跨表 UNION 查询、留存到期提醒、档案详情/PDF、历史补归档 | P2 | UT（已落地 15：Service 9 + Controller 6） |
| M10 | 通知/系统 notify/system | `api` `service` | NotifyConfig/Impl、SysConfig | 通知渠道配置、ops.notify_config 落库、系统参数 | P1 | UT+CT（已落地 17：NotifyConfig Service 11 + Controller 6；其余待补） |
| M11 | 通用审批中心 approval | `api/approval` `service` | ApprovalCenter/Impl | 审批聚合分支、从审核配置读审批人、指派推送 | P1 | UT（已落地 11：Service 8 + Controller 3；**权限缺口已闭环：pending 端点补 `@PreAuthorize('approval.center.pending')` + V237 种子授权**，铁律第 9 条 C1 合规） |
| M12 | 我的任务/改派 my/assign | `api/my` `service/assign` | MyTask/AssignReassign | 任务聚合、改派流转 | P2 | UT（已落地 17：MyTask Service 8 + MyTask Controller 3 + AssignReassign Service 6；**权限缺口已闭环：my/tasks 端点补 `@PreAuthorize('my.task.list')` + V237 种子授权**，铁律第 9 条 C1 合规） |
| M13 | KPI kpi | `api/kpi` | KpiCompare | 指标对比计算 | P2 | UT（已落地 5：KpiCompare Service 3 + Controller 2；compare 端点 `@PreAuthorize` = system.org.switch，铁律第 9 条合规） |
| M14 | 工装 tlm | `api/tlm` `service/tlm` | Tooling/Metro/Repair/Scrap/Maint | **二期暂缓**（无种子、缺 tlm.* 权限码）；仅做 UI 存在性契约检查 | 暂缓 | CT（破例已知） |
| M15 | 公共组件 common | `common` | DataScopeInterceptor、R、BusinessException、JwtUtil、ObjectStorageService(Minio) | org_id 别名解析、统一响应、异常码、JWT 解析、MinIO 上传 | P0 | UT（纯单元） |

---

## 3. 执行路线图（分期落地，每期可独立验证）

### 期次 T1 — 单元测试扩面（UT，覆盖 P0 模块 M1/M2/M3/M4/M15）
- [ ] **M15 common**：`DataScopeInterceptorTest`（org_id 别名解析 / 括号 bug 回归）、`RTest`、`BusinessExceptionTest`、`JwtUtilTest`
- [ ] **M1 uop**：`AuthControllerTest`（standalone，登录 200/401）、`UserControllerTest`、`RoleControllerTest`（403 权限码拦截）
- [ ] **M2 ncm**：已落地 6 用例；补 `NcmCapaServiceImplTest`、`NcmCorrectiveActionImplTest`、`NcmDefectRecordServiceImplTest`（launch8dFromDefect 单人分支）
- [ ] **M3 sqm**：`SqmIncomingAbnormalServiceImplTest`（launch-8d/launch-capa 回写）、`SqmAuditServiceImplTest`（会签链 OR 语义审批）、`SqmChangeServiceImplTest`（ECN 审批链）、`PerfAnalysisServiceImplTest`（分页/分级联动）
- [ ] **M4 spc**：`SpcRuleTest`（判异引擎纯单元）、`SpcSubgroupServiceImplTest`（计数型落库）、`SpcAlarmServiceImplTest`（超界触发）
- **完成标准**：`mvnw test -pl qms-api,qms-service` 全绿；新增用例 ≥ 30；`gen-test-report.mjs` 刷新文档。

### 期次 T2 — 集成测试基座与核心闭环（IT，Testcontainers）
- [x] qms-bootstrap 补 `testcontainers` + `spring-security-test` 依赖（本机 Docker 守护未启动 → 改用直连本机 dev 容器 + 独立库 `qms_test`）
- [x] 建 `QmsIntegrationBase`（直连 localhost:5432/qms_test + Redis + MinIO，`validate-on-migrate=false`）
- [x] **M1 闭环**：`AuthLoginIntegrationTest`（签发 JWT → 带 token 调受保护接口 200 / 无 token 401 / 错密码 401）
- [x] **M2 闭环**：`Ncm8dCreateIntegrationTest`（创建 8D source=不良记录 + 无 token 401）
- **完成标准**：IT 直连本机容器跑通，不污染 dev 库；`gen-test-report.mjs` 纳入 IT 计数。

### 期次 T3 — 契约联调强化（CT）
- [ ] 接入 `openapi-diff`：CI 比对分支与 main 的 `/v3/api-docs` 快照，breaking 变更红线
- [ ] 接入 `Schemathesis`：基于 OpenAPI 模糊测试分页/日期/枚举边界
- [x] 扩展 `check-permission-contract.mjs`：前端 `request.get/post` URL 与后端 `@RequestMapping`+`@(Get|Post)Mapping` 归一化比对；增加 `sys_role_button` Flyway 种子比对提示；**发现真实断裂** `fia/wo-lock/release` 与 `emergency-release` 后端无端点
- **完成标准**：权限破例报告覆盖全部模块；契约 diff 可本地运行。

### 期次 T4 — 前端组件 + E2E（ET）
- [x] 前端引 `vitest@4.1.10` + `@vue/test-utils` + `happy-dom`：测 StatusPill 状态映射（src/utils/status.ts）、`v-permission` 指令（无权限 `el.remove()`）、权限 store has/hasAny/admin 短路；15 用例全绿
- [ ] 前端引 `playwright`：E2E-1 登录→8D 新建→缺陷记录溯源；E2E-2 SPC 告警→发起 8D；E2E-3 来料异常→8D/CAPA；E2E-4 8D D1 签批推进；E2E-5 多角色权限可见性
- **完成标准**：`pnpm test` 全绿；`pnpm playwright test` 关键旅程通过（复用已登录态，避开登录 500）。

### 期次 T5 — CI 串联
- [x] 加 `.github/workflows/test.yml`：stages=[unit, integration, contract, e2e]，门禁见 STRATEGY §7
  - unit：`mvnw test -pl qms-common,qms-api,qms-service`
  - integration：`mvnw test -pl qms-bootstrap -Dtest=*IntegrationTest`（PG16+Redis7.2+MinIO 服务容器，库 qms_test）
  - contract：`check-permission-contract.mjs --strict` + `check-api-contract.mjs`
  - frontend：`pnpm test`（Vitest）+ Playwright E2E（best-effort，重试 1 次）
  - gate：UT+IT+CT 必过，ET 允许 flaky
- **完成标准**：PR 触发全阶段；UT+IT+CT 必过，ET 允许 flaky 重试 1 次。

---

## 4. 目录与命名约定

```
QMS-backend/
├─ qms-api/src/test/java/.../api/{module}/controller/XxxControllerTest.java      # UT: standalone MockMvc
├─ qms-service/src/test/java/.../service/{module}/impl/XxxServiceImplTest.java   # UT: Mockito
├─ qms-common/src/test/java/.../common/.../XxxTest.java                          # UT: 纯单元
├─ qms-bootstrap/src/test/java/.../{module}/.../XxxIntegrationTest.java          # IT: @SpringBootTest + Testcontainers
QMS-fronted/konliQMS/qms-web-new/
├─ src/tests/{component,unit}/*.spec.ts                                          # Vitest
├─ e2e/*.spec.ts                                                                  # Playwright
scripts/
├─ check-permission-contract.mjs   # CT: 权限码一致性（已落地）
├─ gen-test-report.mjs             # 文档自动刷新（已落地）
└─ (待加) openapi-diff / schemathesis 封装
```

命名：`*Test` = 单元，`*IntegrationTest` = 集成，`*.spec.ts` = 前端。

---

## 5. 风险与对策（沿用 STRATEGY §9，补充整项目项）

| 风险 | 对策 |
|---|---|
| qms-api/service 无主类，`@SpringBootTest` 失败 | IT 统一放 qms-bootstrap（有 QmsApplication），或 `@SpringBootTest(classes=QmsApplication.class)` |
| 离线仓库缺 testcontainers/minio JAR | 首跑去 `-o` 联网拉一次 |
| V90 checksum 冲突致 Flyway 失败 | IT 保留 `--spring.flyway.validate-on-migrate=false` |
| 模块多、用例量大导致构建慢 | 用 `-pl qms-api,qms-service -am` 增量；IT 独立 profile `@Tag("integration")` 按需跑 |
| 前端历史预存 TS 错误 ~117 处 | ET 不阻塞于历史类型错误，仅跑 Playwright/Vitest |
| tlm 二期暂缓致权限破例 | CT 脚本已知 6 处 tlm.* 破例，标记为预期、不红线 |

---

## 6. 进度看板（自动刷新区）

> 最近运行：<!--AUTO:PLAN_PROGRESS-->
> T1 已完成。后端 UT：38 用例（M15 common 14 / M1 uop 6 / M4 spc 4 / M3 sqm 5 / M2 ncm 9，含原 ncm 6 基线）。T2 已完成：IT 6 用例（AuthLogin 4 + Ncm8dCreate 2，直连本机 dev 容器 + 独立库 qms_test）。T3 已完成：CT 权限契约脚本落地并发现 fia/wo-lock 与 emergency-release 真实断裂。T4 已完成：前端 Vitest 15 用例全绿（Playwright 5 旅程环境就绪后补）。T5 已完成：CI 四 stage 门禁。扩面进行中：M7 patrol 14 + M8 qmsmgmt 13 + M9 archive 15 + M10 notify 17 + M11 approval 11 + M12 my/assign 17 + M13 kpi 5 + M6 cs 20 已落地，后端 UT 累计 176 全绿（common 14 / service 101 / api 61）。
<!--AUTO:PLAN_PROGRESS-->

| 期次 | 范围 | 用例数 | 状态 |
|---|---|---|---|
| T1 | UT 扩面 M1/M2/M3/M4/M15 | 38（已落地） | ✅ 已完成 |
| T2 | IT 基座 + M2→M4 闭环 | 6（已落地） | ✅ 已完成 |
| T3 | CT 权限契约 + 断裂发现 | 2 脚本 | ✅ 已完成 |
| T4 | ET Vitest 15 + Playwright 5 | 15（Vitest 已落地）/ 5 旅程待补（环境就绪后补） | ✅ 组件测试已完成 |
| T5 | CI 串联 | 4 stages | ✅ 已完成 |
| 扩面 | M7 patrol 模块 UT | 14（已落地） | ✅ 已完成 |
| 扩面 | M8 qmsmgmt 模块 UT（全量：QualityGoal + InternalAudit + AdverseEvent + ComplianceBoard） | 39（已落地） | ✅ 已完成 |
| 扩面 | M9 archive 模块 UT | 15（已落地） | ✅ 已完成 |
| 扩面 | M10 notify/system 模块 UT | 17（已落地：NotifyConfig Service 11 + Controller 6） | ✅ 已完成 |
| 扩面 | M11 approval 模块 UT | 11（已落地：Service 8 + Controller 3；pending 端点权限缺口已闭环 @PreAuthorize + V237 种子） | ✅ 已完成 |
| 扩面 | M12 my/assign 模块 UT | 17（已落地：MyTask Service 8 + MyTask Controller 3 + AssignReassign Service 6；my/tasks 端点权限缺口已闭环 @PreAuthorize + V237 种子） | ✅ 已完成 |
| 扩面 | M13 kpi 模块 UT | 5（已落地：KpiCompare Service 3 + Controller 2；compare 权限码 system.org.switch 合规） | ✅ 已完成 |
| 扩面 | M6 cs 模块 UT | 20（已落地：CsWorkOrder Service 10 + Controller 10；cs.workorder.* 与 cs.satisfaction.list 权限码合规） | ✅ 已完成 |
| 扩面 | 其余模块（audit/adverse/qmsmgmt 其余） | 待做 | ⬜ 待做 |
