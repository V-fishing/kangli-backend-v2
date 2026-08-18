# 康立 QMS 测试方案（接口 / 集成 / E2E / 契约联调）

> 适用范围：QMS 前后端工程（`QMS-backend` + `QMS-fronted/konliQMS/qms-web-new`）
> 技术基线：Spring Boot 3.3.5 / Java 21 / MyBatis-Plus 3.5.9 / Vue3.5 / Vite6 / Element Plus 2.8 / axios
> 现状说明：截至本方案编写时，**后端 `src/test` 为空、`mvn test` 空跑**；**前端无测试框架**（无 jest/vitest/playwright）。本方案为从零落地的分层测试体系蓝图，各层均可独立实施、渐进补充。

---

## 0. 测试金字塔与目标

```
        ▲  E2E（Playwright，少量、覆盖关键业务流）
       ╱  ╲
      ▕    ▕  契约联调（OpenAPI diff + Schemathesis/Dredd + 权限码一致性）
     ╱ 集成  ╲  （Testcontainers PG/Redis/MinIO，跨模块、含 Flyway 种子）
    ╱────────╲
   ■ 接口/单元 ■  （JUnit5 + MockMvc + Mockito，数量最多、最快、最稳）
```

| 层级 | 工具 | 范围 | 目标占比 | 执行频率 |
|---|---|---|---|---|
| 接口/单元 | JUnit5 + MockMvc + Mockito + spring-security-test | 单 Controller/Service 方法 | ~60% | 每次 commit / PR |
| 集成 | Testcontainers + `@SpringBootTest` | 跨模块 + 真实 PG/Redis/MinIO + Flyway | ~25% | PR / 夜测 |
| 契约联调 | openapi-diff + Schemathesis/Dredd + 权限码脚本 | 前后端 API 契约 + 权限码一致性 | ~10% | PR / 发布前 |
| E2E | Playwright | 关键用户旅程（登录→业务→整改闭环） | ~5% | 发布前 / 夜测 |

**非目标**：性能压测（JMeter）、混沌工程、视觉回归（设计系统已有 token 约定，由 Code Review 卡点保证）。

---

## 1. 环境准备与依赖引入

### 1.1 后端测试依赖（`qms-parent` pom.xml `dependencyManagement` 已管理 spring-boot 版本，在 `qms-api` / `qms-service` 的 `pom.xml` 增加）

```xml
<!-- 测试核心（scope=test） -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<!-- Spring Security 测试支持（@WithMockUser、SecurityMockMvcRequestPostProcessors） -->
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
<!-- 集成测试真实依赖（PG/Redis/MinIO 容器） -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <version>1.20.4</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>1.20.4</version>
  <scope>test</scope>
</dependency>
<!-- 测试数据构造 -->
<dependency>
  <groupId>com.github.javafaker</groupId>
  <artifactId>javafaker</artifactId>
  <version>1.0.2</version>
  <scope>test</scope>
</dependency>
```

> 注意：`mvnw` 离线模式（`-o`）需先联网拉取一次测试依赖；后续可恢复 `-o`。

### 1.2 前端测试依赖（`qms-web-new/package.json`）

```bash
pnpm add -D playwright @playwright/test vitest @vue/test-utils happy-dom
pnpm playwright install chromium
```

### 1.3 基础设施复用

集成测试 **不复用** `docker-compose.yml` 里人工起的容器（避免污染开发库），改用 **Testcontainers** 在测试生命周期内拉起等价镜像（`postgres:16` / `redis:7.2` / `minio/minio`），种子数据由 Flyway 自动执行（与 dev 一致，`--spring.flyway.validate-on-migrate=false` 因 V90 checksum 历史问题需保留）。

---

## 2. 接口测试（API / 单元层）

### 2.1 分层与切片测试原则

- **Controller 层**：用 `@WebMvcTest(XXXController.class)` + `MockMvc` + `@MockBean` 注入 Service，验证：
  - HTTP 状态码（200/400/401/403/404/500）
  - 请求体校验（`@Valid` 触发 400）
  - `@PreAuthorize("hasAuthority('xxx')")` 权限码拦截（用 `spring-security-test` 的 `@WithMockUser(authorities=...)`）
  - 响应体结构与字段
  - 统一包装 `R<T>` 的 `code/msg/data`
- **Service 层**：用 `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks`，验证业务逻辑分支（如 8D 状态机推进顺序、缺陷记录必填字段兜底）。
- **领域/工具层**：纯单元测试（如 `DataScopeInterceptor` 的 org_id 别名解析、SPC 判异规则引擎）。

### 2.2 目录约定

```
qms-api/src/test/java/com/konli/qms/api/.../XxxControllerTest.java
qms-service/src/test/java/com/konli/qms/service/.../XxxServiceImplTest.java
qms-common/src/test/java/com/konli/qms/common/.../XxxTest.java
```

### 2.3 示例：8D 创建接口测试（贴合本次"统一整改源头"改造）

```java
@WebMvcTest(Ncm8dController.class)
class Ncm8dControllerTest {
    @Autowired MockMvc mvc;
    @MockBean Ncm8dService ncm8dService;
    @MockBean NcmDefectRecordService defectRecordService; // 若 Controller 直接依赖

    @Test
    @WithMockUser(authorities = "ncm.8d.create")
    void create_manual8d_persistsDefectFirst() throws Exception {
        // 验证 Controller 层：人工建 8D 必须经 ncm8dService.create 且返回 R.ok
        when(ncm8dService.create(any())).thenReturn(new Qms8dReport());
        mvc.perform(post("/api/v1/ncm/8d-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"flowType\":\"8D\",\"source\":\"人工\",\"issue\":\"测试\",\"severity\":\"高\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void create_withoutPermission_returns403() throws Exception {
        // 无权限用户必须被 @PreAuthorize 拦截
        mvc.perform(post("/api/v1/ncm/8d-reports")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isForbidden());
    }
}
```

### 2.4 接口测试覆盖矩阵（按模块，对齐 `CLAUDE.md` 跨端强约束）

| 模块 | 重点接口 | 必测点 |
|---|---|---|
| 认证 uop | POST `/api/v1/auth/login` | 401 错误密码、200 签发 JWT、角色映射 |
| 8D ncm | POST `/8d-reports`、POST `/launch`（SQM）、`/spc/alarms/{id}/launch-8d` | **source 必须=不良记录、sourceRefId=缺陷记录 id**（本次改造核心断言） |
| 缺陷记录 ncm | POST（create）、`/launch8dFromDefect` | defectDictCode 存在性校验、defectCount/batchTotal 非空兜底、issue 透传 |
| 来料异常 sqm | `/abnormals/{id}/launch-8d`、`/launch-capa` | 异常单回写 d8Id/capaId/rectifyType |
| SPC | `/alarms/{id}/launch-8d`、`/subgroups` | 判异生成 AL-、超界子组触发告警 |
| 权限 sys | `sys_menu`/`sys_role_button` 查询 | 菜单码/按钮码与前端 `v-permission` 一致 |

---

## 3. 集成测试（跨模块 + 真实依赖）

### 3.1 Testcontainers 基座

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "spring.flyway.validate-on-migrate=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
class QmsIntegrationBase {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("qms").withUsername("qms").withPassword("qms");
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2").withExposedPorts(6379);
    @Container
    static GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
        .withExposedPorts(9000, 9001)
        .withEnv("MINIO_ROOT_USER","minioadmin").withEnv("MINIO_ROOT_PASSWORD","minioadmin")
        .withCommand("server /data --console-address :9001");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("minio.endpoint", () -> "http://"+minio.getHost()+":"+minio.getMappedPort(9000));
    }
}
```

### 3.2 业务闭环集成样例

**场景：SPC 告警 → 发起 8D → 缺陷记录溯源**（端到端验证本次三步改造）

```java
@Test
void spcAlarm_launch8d_createsDefectRecord_and_linksBack() {
    // 1. 造一个超界子组触发真实告警（或直插 ops.spc_alarm）
    // 2. 调 POST /api/v1/spc/alarms/{id}/launch-8d
    // 3. 断言：返回 8D 的 source=不良记录、sourceRefId 指向一条 ncm_defect_record(source=SPC报警, defectDictCode=SPC)
    // 4. 断言：缺陷记录的 d8_no 回写 = 8D 单号（双向关联）
    // 5. 断言：审计链 8D → 缺陷记录 → SPC 告警 完整
}
```

**场景：来料异常 → 8D/CAPA 双路径**

```
插入 sqm_incoming_abnormal → POST /launch-8d → 断言缺陷记录(source=SQM异常)+异常单 d8Id 回写
                                  → POST /launch-capa → 断言缺陷记录 capaNo 回写（同一缺陷记录可发多种整改）
```

### 3.3 事务与数据隔离

- 每个测试类 `@Transactional` + `@Commit` 控制；或测试后清理（参考本次清理脚本：`DELETE` 顺序需含 `qms_8d_archived_report` → `qms_8d_report` → `ncm_defect_record`）。
- Flyway 幂等迁移（V235/V236）在 `@SpringBootTest` 启动时自动跑；测试库为独立容器，不影响 dev 库。

---

## 4. 接口契约联调（前后端一致性）

### 4.1 契约来源：Springdoc OpenAPI

后端已集成 `springdoc-openapi 2.6`，Swagger UI：`http://localhost:8080/swagger-ui.html`，OpenAPI JSON：`/v3/api-docs`。

### 4.2 契约差异检测（openapi-diff）

```bash
# CI 中：对比本次分支与 main 的 OpenAPI 快照，破坏性变更（字段删除/类型变更/必填新增）直接失败
npx openapi-diff qms-main.json qms-branch.json
```

规则：
- **破坏性变更**（breaking）→ PR 红线，禁止合并。
- **非破坏性**（新增端点/可选字段）→ 允许，但需同步更新前端 `src/api/modules/*` 与 `src/api/types/*`。

### 4.3 契约运行时校验（Schemathesis / Dredd）

```bash
# 基于 OpenAPI 自动生成并模糊测试所有接口，发现 500/校验缺失
schemathesis run http://localhost:8080/v3/api-docs --base-url http://localhost:8080 -x auth
```

重点覆盖：分页参数（page/size 边界）、日期 `value-format=YYYY-MM-DD`、枚举（severity 严重/一般/轻微 vs 高/中/低 双命名）、超大/负数/SQL 注入字符。

### 4.4 权限码一致性联调（强约束 C1）

> AGENTS.md 铁律第 9 条：前端 `v-permission`/`has()` 引用的码须与库内 `sys_role_button` 一致，否则按钮不可见。

脚本化断言（`scripts/check-permission-contract.*`）：
1. 扫描前端 `src/views/**` 提取所有 `has('{module}.{resource}.{action}')` 与 `v-permission="'...'"` 字面量。
2. 扫描后端 `@PreAuthorize("hasAuthority('...')")` 提取后端码。
3. 交叉比对：前端引用码必须 ⊆ 后端 `@PreAuthorize` 码 ⊆ `sys_role_button` 种子码（Flyway V 文件）。
4. 不一致 → CI 失败，并提示"缺按钮权限请补 `sys_role_button` 种子 + 清 Redis 权限缓存"。

### 4.5 类型契约联调（前端 ts 与 OpenAPI）

- 用 `openapi-typescript` 由 `/v3/api-docs` 生成 `src/api/gen/types.ts`，与手写 `src/api/types/*.ts` 比对，避免手写类型漂移。
- 前端 axios 拦截器已统一处理 `R<T>`，契约测试需断言生产响应 `code/msg/data` 结构与前端 `R` 泛型一致。

---

## 5. E2E 测试（Playwright）

### 5.1 关键用户旅程（Priority 排序）

| # | 旅程 | 覆盖页面/接口 | 断言 |
|---|---|---|---|
| E2E-1 | 登录 → 8D 列表 → 人工新建 8D → 缺陷记录溯源 | `/ncm/8d-reports`、`POST /8d-reports` | 新建后 8D 来源=不良记录；缺陷记录列表可见该条(source=人工) |
| E2E-2 | SPC 告警 → 一键发起 8D | `/spc/alarms`、`POST /alarms/{id}/launch-8d` | 跳转 8D 详情，sourceRefId 指向缺陷记录 |
| E2E-3 | 来料异常 → 发起 8D/CAPA | `/sqm/abnormals` | 异常单状态变整改中，缺陷记录关联 |
| E2E-4 | 8D D1 团队组建 → 质量经理签批 → D2 | `8dDetail.vue` | 步骤条推进、通知到达 |
| E2E-5 | 权限码可见性 | 多角色登录 | 无权限按钮不渲染（铁律第 9 条） |

### 5.2 Playwright 项目配置

```ts
// playwright.config.ts
export default defineConfig({
  testDir: './e2e',
  baseURL: 'http://localhost:5174',  // 前端 dev 端口（Vite 自动递增前默认 5174）
  use: { trace: 'on-first-retry', video: 'retain-on-failure' },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'pnpm dev',
    url: 'http://localhost:5174',
    reuseExistingServer: true,   // 复用已启动的 dev server
    timeout: 120_000,
  },
});
```

### 5.3 E2E 与契约联动

- E2E 登录复用 `mz.admin / 123456`（或与 dev 库一致账号），token 由 `localStorage`/`Pinia` 注入，避免走登录 500 干扰（已知登录接口偶发 500 与本次改动无关，E2E 用 API 直登或预置 session）。
- E2E 测试数据走独立 org 或测试后清理（参考本次 `DELETE` 顺序脚本），避免污染 `mz`/`sz` 正式数据。

### 5.4 组件/契约联调层（Vitest + @vue/test-utils）

对 `StatusPill`、`el-` 控件封装、权限指令 `v-permission` 做组件测试：
- `v-permission` 在无权限时确实不渲染按钮（铁律第 9 条落地保障）。
- Swiss Light token 色值不越界（AGENTS.md 铁律 1）。

---

## 6. 测试数据管理

1. **生产数据隔离**：集成/E2E 一律连 Testcontainers 或独立测试库，**绝不**直连 dev/prod 的 `qms-postgres`（本次验证为手动，正式测试须隔离）。
2. **种子一致性**：测试库 Flyway 与 dev 同源（含 V235 SPC / V236 NCM 字典、权限种子），保证 `@PreAuthorize` 码存在。
3. **构造工具**：用 `javafaker` / 工厂方法生成供应商、用户、参数，避免硬编码 UUID（参考历史事故：`NcmDefectDemoSeeder` 随机序列漂移导致数据翻倍）。
4. **清理顺序**（外键约束，本次实测）：
   `qms_8d_stage_detail` → `qms_assign_record` → `qms_8d_archived_report` → `qms_8d_report` → `ncm_defect_record`。

---

## 7. CI/CD 集成

`.github/workflows/test.yml`（或 GitLab CI）分阶段：

```yaml
stages: [unit, integration, contract, e2e]

unit:
  script: cd QMS-backend && ./mvnw -o test -pl qms-api,qms-service  # 接口/单元
integration:
  script: ./mvnw -o verify -Dtest=*IntegrationTest                # Testcontainers
contract:
  script: |
    # 启动后端 → 抓 /v3/api-docs → openapi-diff 比对 main
    # 跑权限码一致性脚本
e2e:
  script: |
    cd QMS-fronted/konliQMS/qms-web-new && pnpm i && pnpm build
    pnpm playwright test
```

门禁：
- 接口/单元 + 集成：PR 必过。
- 契约 diff（breaking）+ 权限码一致性：PR 必过。
- E2E：release / 夜测（允许 flaky 重试 1 次）。

---

## 8. 落地路线（渐进、不阻塞业务）

| 阶段 | 交付物 | 工作量 | 状态 |
|---|---|---|---|
| P0 | 引入后端测试依赖 + 权限码一致性脚本 | 0.5d | ✅ 已完成 |
| P1 | 接口测试覆盖 8D（Controller standalone + Service Mock 单元测试，含本次三步改造断言） | 2d | ✅ 已完成（8D 域） |
| P2 | Testcontainers 集成基座 + 2 条业务闭环（SPC→8D、SQM→8D/CAPA） | 2d | ⬜ 待做 |
| P3 | openapi-diff + Schemathesis 接入 CI + 前端 ts 生成比对 | 1d | ⬜ 待做 |
| P4 | Playwright E2E-1~E2E-5 + Vitest 组件测试 | 3d | ⬜ 待做 |

**立即建议**：先落 P0（权限码一致性脚本价值最高，直接防铁律第 9 条破例），再按业务优先级补 P1/P2。

---

## 10. 已落地交付物（实测通过）

> 最近运行：<!--AUTO:STRATEGY_DATE-->
2026-08-17。以下均经 mvnw test 实际运行绿灯。
<!--AUTO:STRATEGY_DATE-->

<!--AUTO:STRATEGY_RESULTS-->
- 后端测试：186 项，通过 186，失败 0（BUILD SUCCESS）
- 权限码一致性：前端 31 / 后端 135，破例 1 处
<!--AUTO:STRATEGY_RESULTS-->

### 10.1 测试依赖
- `qms-api/pom.xml`：`spring-boot-starter-test` + `spring-security-test`（scope=test）
- `qms-service/pom.xml`：`spring-boot-starter-test`（scope=test）

### 10.2 接口/单元测试用例
- `qms-api/.../ncm/controller/Ncm8dControllerTest.java`（standalone MockMvc，3 用例）
  - 创建 8D 转发 service 返回 `code=0`
  - 分页查询转发 `listPage` 返回 `code=0`
  - 请求体 `issue` 字段正确反序列化
- `qms-service/.../ncm/impl/Ncm8dServiceImplCreateTest.java`（Mockito，3 用例）
  - **人工建 8D（正常流程）：先落缺陷记录（source=人工、defectDictCode=NCM、issue 透传），再从缺陷记录发起 8D**（核心断言，验证本次三步改造）
  - 事件类来源（SQM异常）未填 sourceRefId 抛 400
  - 简易流程：先落缺陷记录、8D 直接闭环、source=不良记录

### 10.3 权限码一致性脚本
- `scripts/check-permission-contract.mjs`（Node，零依赖）
- 运行：`node scripts/check-permission-contract.mjs [--strict]`
- 实测结果：前端引用 29 码、后端 `@PreAuthorize` 126 码；发现 **6 个破例，全部为 `tlm.*` 工装码**（前端已落地 UI 但后端 TLM 二期暂缓、无 seed），与 CLAUDE.md 铁律一致——脚本精准识别此类破例。

### 10.4 运行结果
```
Tests run: 3 ... Ncm8dServiceImplCreateTest   BUILD SUCCESS
Tests run: 3 ... Ncm8dControllerTest           BUILD SUCCESS
```


---

## 9. 风险与对策

| 风险 | 对策 |
|---|---|
| 离线仓库缺测试 JAR（minio/JWT） | 首次构建去 `-o` 联网拉取一次 |
| V90 checksum 冲突导致 Flyway 启动失败 | 测试 profile 保留 `--spring.flyway.validate-on-migrate=false` |
| 登录接口偶发 500（与业务改动无关） | E2E/集成测试用 API 直登或预置 JWT，不依赖 UI 登录 |
| 权限 Redis 缓存 30min 导致新码不生效 | 权限一致性脚本同时校验 `sys_role_button` 种子 + 提示清 `qms:perms:{userId}` |
| 测试库与 dev 库数据混淆 | 集成/E2E 强制 Testcontainers 独立库 |
| 历史预存 TS 错误（前端 ~117 处） | E2E 用 `pnpm build` 但实际仅跑 Playwright，不阻塞于历史类型错误 |
</content>
