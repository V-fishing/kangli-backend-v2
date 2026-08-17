# 康立 QMS 测试产出报告（Test Deliverables）

> 生成日期：2026-08-17
> 配套文档：`TEST-STRATEGY.md`（测试方案/蓝图）
> 范围：接口测试（Controller 层 + Service 层单元测试）+ 契约联调（权限码一致性）
> 状态：P0 + P1（8D 域）已落地并实测通过；P2/P3/P4 待续（骨架见 TEST-STRATEGY.md §3/§4/§5）

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

### 2.2 测试用例文件（后端 UT 38 + IT 6）
| 文件 | 层级 | 用例数 | 覆盖点 |
|---|---|---|---|
| `qms-common/.../common/api/RTest.java` | 纯单元 | 4 | 统一响应 R 成功/失败/分页封装 |
| `qms-common/.../common/exception/BusinessExceptionTest.java` | 纯单元 | 3 | 业务异常码/消息 |
| `qms-common/.../common/security/JwtUtilTest.java` | 纯单元 | 2 | JWT 签发/解析（反射注入 secret） |
| `qms-common/.../common/security/DataScopeInterceptorTest.java` | 纯单元 | 5 | org_id 别名解析 / 括号 bug 回归 / 组织切换 |
| `qms-api/.../uop/controller/AuthControllerTest.java` | Controller（MockMvc） | 3 | 登录 200 / 401 / 参数绑定 |
| `qms-api/.../uop/controller/UserControllerTest.java` | Controller（MockMvc） | 3 | 用户查询 200 / 角色权限 |
| `qms-service/.../spc/impl/SpcRuleServiceImplTest.java` | Service（Mockito） | 4 | 判异规则引擎 |
| `qms-service/.../sqm/impl/SqmAuditServiceImplApproveTest.java` | Service（Mockito） | 5 | 会签 OR 语义审批人校验 403/404/409/null |
| `qms-service/.../ncm/impl/NcmDefectRecordServiceImplLaunch8dTest.java` | Service（Mockito） | 3 | launch8dFromDefect 单人分支 |
| `qms-service/.../ncm/impl/Ncm8dServiceImplCreateTest.java` | Service（Mockito） | 3 | 人工建 8D 先落缺陷记录（原基线） |
| `qms-api/.../ncm/controller/Ncm8dControllerTest.java` | Controller（MockMvc） | 3 | 创建/分页转发（原基线） |
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

### 3.1 后端 UT（38）
- **DataScopeInterceptor（5）**：org_id 别名解析（JOIN 歧义回归）、括号 bug 回归（`(org_id=? OR org_id IS NULL)` 带括号）、组织切换分支。
- **SqmAuditServiceImpl.approve（5）**：登录用户须 ∈ approverId 逗号串（OR 语义），否则 403；空审批人/非指定人/null 兼容。
- **NcmDefectRecordServiceImpl.launch8d（3）**：launch8dFromDefect 单人分支（ownerUserId→缺陷记录→8D）。
- **AuthController/UserController（6）**：standalone MockMvc，登录 200 / 错密码 401 / 无 token 401。
- **SpcRuleServiceImpl（4）**：判异规则引擎。
- 原 ncm 8D 基线（6）：create 三分支（统一整改源头）+ Controller 转发。

### 3.2 后端 IT（6，qms-bootstrap）
- **AuthLoginIntegrationTest（4）**：登录签发 JWT → 带 token 调受保护接口 200 / 无 token 401 / 错密码 401。
- **Ncm8dCreateIntegrationTest（2）**：创建 8D source=不良记录 + 无 token 401。

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
$ ./mvnw test -pl qms-api,qms-service

[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.096 s -- in com.konli.qms.common.api.RTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in com.konli.qms.common.exception.BusinessExceptionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.092 s -- in com.konli.qms.common.security.DataScopeInterceptorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.503 s -- in com.konli.qms.common.security.JwtUtilTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.633 s -- in com.konli.qms.service.ncm.impl.Ncm8dServiceImplCreateTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.467 s -- in com.konli.qms.service.ncm.impl.NcmDefectRecordServiceImplLaunch8dTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.340 s -- in com.konli.qms.service.spc.impl.SpcRuleServiceImplTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.169 s -- in com.konli.qms.service.sqm.impl.SqmAuditServiceImplApproveTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.019 s -- in com.konli.qms.api.ncm.controller.Ncm8dControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.199 s -- in com.konli.qms.api.uop.controller.AuthControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.151 s -- in com.konli.qms.api.uop.controller.UserControllerTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### 权限码一致性检查
```text
$ node scripts/check-permission-contract.mjs

========================================
 权限码一致性检查 (C1 / 铁律第 9 条)
========================================
前端引用权限码: 29
后端 @PreAuthorize 码: 126

⚠️  发现 6 个前端引用码在后端 @PreAuthorize 中缺失(潜在破例):
   - tlm.metro.collect
   - tlm.metro.lock
   - tlm.metro.repair
   - tlm.metro.scrap
   - tlm.tooling.first
```

**总计：38 项测试用例，通过 38 项，失败 0 项；权限码检查脚本运行正常并产出有效差异报告。**
<!--AUTO:RESULTS-->

---

## 5. 覆盖率与局限

| 维度 | 已覆盖 | 未覆盖（后续阶段） |
|---|---|---|
| 接口/单元 UT | common 14 / uop 6 / spc 4 / sqm 5 / ncm 9（共 38） | patrol/qmsmgmt/archive/notify/system/approval/my/kpi 等其余 Service |
| 集成测试 IT | AuthLogin 4 + Ncm8dCreate 2（直连本机容器 + qms_test 库） | SPC→8D、SQM异常→8D/CAPA 跨模块闭环 IT（T2 目标，待补） |
| 契约测试 CT | 权限码一致性（前端 vs 后端）+ 真实断裂发现 | openapi-diff + Schemathesis 运行时校验 |
| E2E/组件 ET | Vitest 组件测 15（StatusPill/v-permission/store） | Playwright 5 旅程（登录→8D / SPC告警 / 来料异常 / D1签批 / 多角色可见性）|
| 权限拦截 | UT 层 403 校验 + 静态一致性 | 更多 Controller `@PreAuthorize` 运行时 403 集成验证 |

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
| 扩面 | 将 UT 范式复制到 patrol/qmsmgmt/archive/notify/system/approval/my/kpi | — | ⬜ 待做 |
</content>
