# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

康立 QMS 质量管理系统后端。Java 21 + Spring Boot 3.3.5 + MyBatis-Plus 3.5.9 + PostgreSQL 16 + Flyway + Redis 7.2 + MinIO。模块化单体架构,5 个 Maven 子模块,按《QMS-代码规范文档-V1.0》组织。

前端在 `QMS-fronted/konliQMS/qms-web/`(Vue 3 + Vite 6 + TypeScript),有自己的 CLAUDE.md。

## Commands

```bash
# 启动基础设施(PG16 + Redis + MinIO)
cd QMS-backend && docker compose up -d

# 构建(全部模块)
./mvnw clean install -DskipTests

# 构建改动模块后运行
# 注意: -am spring-boot:run 会让 goal 落到 reactor 首个模块(qms-parent,pom)而报错
# 正确姿势:先 install 依赖模块,再单独在 bootstrap 上 run(不加 -am)
./mvnw -pl qms-bootstrap -am install -DskipTests
./mvnw -pl qms-bootstrap spring-boot:run

# 运行测试
./mvnw test

# 单模块测试
./mvnw -pl qms-service test
```

应用启动后:http://localhost:8080,Swagger:http://localhost:8080/swagger-ui.html,健康检查:http://localhost:8080/actuator/health

种子账号:`admin/123456`(跨公司全量)、`mzuser/user123`(仅梅州),另有 17 个角色账号(`mz.qe`/`sz.sqe`/…,密码统一 `123456`,详见 `SeedRunner`)。两个 Runner 均幂等,密码以 `SeedRunner` 的 `123456` 为准。

> 备注:`application-dev.yml` Hikari 设 `initialization-fail-timeout=-1`,**无库也能起冒烟**(Flyway/种子在连库后才执行)。生产用 `application-prod.yml`,敏感配置(JWT secret / DB 密码 / MinIO 密钥)走环境变量(Jasypt 加密 / `QMS_JWT_SECRET` 等),勿用 dev 占位值。当前仓库无 `src/test` 单测源,`mvn test` 暂为空跑;验证以 `.qms-test/` 下的 API 脚本 + Swagger 手测为主。

## Architecture

### 5 Maven 模块 + 层优先包

```
QMS-backend/
├── pom.xml              # 父 POM,版本锁定
├── qms-common/          # 基础设施:R<T>、BaseEntity、Security、DataScope、类型处理器
├── qms-domain/          # 实体 + Mapper(按业务域分包:uop/fia/patrol/spc/ncm/sqm/archive)
├── qms-service/         # Service 接口 + 实现(按业务域分包 + support 工具)
├── qms-api/             # Controller + DTO(按业务域分包)
└── qms-bootstrap/       # 启动类 + application.yml + logback + Flyway 迁移 + DataInitializer/SeedRunner(种子) + schedule(定时任务)
```

依赖方向:`bootstrap -> api -> service -> domain -> common`。Service 不能依赖 api 层 DTO(用 service 层 DTO/原始类型代替)。

### 业务域(`com.konli.qms.{api|service|domain}.{module}`)

| 域 | 覆盖 | 规模 |
|---|---|---|
| `uop` | 账号/组织/角色/菜单/字典/授权/JWT 登录 | 7 controller |
| `fia` | 首件检验:任务/审批/拦截配置/签名配置/检验标准/触发类型/检验计划 | 8 |
| `patrol` | 巡检:路线/任务/异常/记录 | 3 |
| `spc` | 控制图/子组/参数/控制限/规则/能力/报警/采集任务/通知渠道/全局配置 | 10 |
| `ncm` | 缺陷字典/缺陷记录/8D/鱼骨图/CAPA/纠正措施/预警升级/BI 报表 | 11 |
| `sqm` | 供应商/评级/绩效/审计/变更/FMEA/追溯/异常/分析/证书(最大域) | 16 |
| `archive` | 检验记录归档与查询 | 1 |

`service/support/OrgIdResolver` 为跨域共享工具。各域权限码在 `DataInitializer` 里按域 `seed{Module}Perms()` 注册。

### 根包 `com.konli.qms`(注意不是 kangli)

代码规范文档写的 `com.konli.qms`(非 `com.kangli.qms`)。项目文件夹是 `kangli` 但包名是 `konli`。

### 关键基础设施(qms-common)

- **`R<T>`** = `{code, msg, data}`,code=0 成功。前端 `request.ts` 已对齐读 `msg`(非 `message`)。
- **`BaseEntity`**:UUID 主键(ASSIGN_UUID)、org_id、审计字段(createdAt/updatedAt/createdBy/updatedBy)、isDeleted(@TableLogic 软删除)、version(@Version 乐观锁)。业务实体继承它并加自己的 `orgId` 字段(`@TableField("org_id")`)。
- **`GlobalExceptionHandler`**:401(未认证)、403(@PreAuthorize 拒绝)、404(NoResourceFoundException)、业务码(BusinessException)。
- **`SecurityConfig`** + `JwtAuthenticationFilter`:无状态 JWT。每请求经 `PermissionLoader` 加载权限码(Redis 缓存 30min)为 authorities,供 `@PreAuthorize("hasAuthority('xxx')")` 校验。`@EnableMethodSecurity` 已开启。
- **`DataScopeInterceptor`**:MyBatis-Plus InnerInterceptor,非管理员用户的 SELECT 自动追加 `org_id = '<dataScope>'`(应用级 SQL 改写,非 PG RLS)。管理员(dataScope=all)不过滤。
- **`TzLocalDateTimeTypeHandler`** + `TypeHandlerRegistrar`:PG TIMESTAMPTZ -> LocalDateTime,用 `atZoneSameInstant(ZoneId.systemDefault())` 转 +08 时区。**必须注册为默认 handler**(TypeHandlerRegistrar @PostConstruct),否则 ResultMapping 无 jdbcType 时用内置 handler 会报 "Cannot convert TIMESTAMPTZ to LocalDateTime"。
- **`AuditMetaObjectHandler`**:自动填充 createdBy/updatedBy = CompanyContext.userId(非 "system")。`lockUntil` 字段用 `@TableField(updateStrategy = FieldStrategy.ALWAYS)` 确保 null 重置可写。
- **`StringArrayTypeHandler`**:PG `VARCHAR[]` <-> `String[]`(用于 `fia_sign_config.sign_methods` 等)。实体需 `@TableName(autoResultMap = true)` + `@TableField(typeHandler = StringArrayTypeHandler.class)`。
- **`stringtype=unspecified`**(application-dev.yml datasource URL):让 MyBatis-Plus 写 String 到 UUID 列无需显式 cast。
- **`PermissionLoader`**:按 userId 查 sys_role_menu + sys_role_button 的 menu_code/btn_code。Redis 缓存 key `qms:perms:{userId}`,TTL 30min。`evictUser`(用户角色变更)、`evictAll`(角色权限变更)。

### 多分公司(简化模型)

公司 = 顶级 org(`org_type='公司'`,梅州 MZ/深圳 SZ)。用户 `org_id=null` = 跨公司管理员(dataScope=all,看全部);`org_id=某公司` = 普通用户(仅看本公司)。**不加** company_id/sys_company/sys_user_company(简化模型,与数据库设计文档的多分公司矩阵不同)。权限码 + dataScope 均基于 `org_id`。

### 数据库

- 所有表在 `ops.*` schema,主键 UUIDv7(`DEFAULT ops.gen_uuid_v7()`),Flyway 迁移 V01-V38(V23 跳号),schema 增量演进见 `qms-bootstrap/src/main/resources/db/migration/`。
- 种子由两个 Runner 幂等插入,启动时先 `permissionLoader.evictAll()` 清权限缓存:
  - `DataInitializer`(CommandLineRunner):公司、RBAC(角色/菜单/按钮/权限)、各业务域权限码、FIA 检验标准。
  - `SeedRunner`(ApplicationRunner):17 个角色对齐账号(MZ/SZ × 8 角色 + 跨公司 `admin`),密码统一 `123456`,`orgId=null` -> `dataScope=all`。
- WECO 8 判异规则 + 8D 阶段配置(D1-D8)+ 156 条字典(sys_dict)由 V07 种子。

### 定时任务

`QmsApplication` 标 `@EnableScheduling`。集中式定时任务在 `bootstrap/schedule/`:
- `RepeatEscalationJob`:`@Scheduled(cron = "0 0 3 * * ?")`,每日 3:00 扫近 30 天 `sqm_incoming_abnormal`,对同供应商+物料 ≥2 次异常的自动创建升级记录、加审核频次、降采购份额(委托 `SqmAbnormalService.checkRepeatEscalation`)。

另有部分 SQM/SPC 服务 impl 内嵌 `@Scheduled`(采集、证书到期、审计等),新增定时任务优先归到 `bootstrap/schedule/` 集中管理。

### 业务模块模式(从 FIA/SPC/NCM/SQM/patrol 提炼)

每个业务模块遵循:
1. **实体**(qms-domain):extends BaseEntity(有审计字段的表)或 plain(无审计的子表/日志表)。`@TableName("ops.表名")`,`@TableField("snake_case")` 显式映射。
2. **Mapper**:extends `BaseMapper<实体>`,`@Mapper`。
3. **Service**:`@Service @RequiredArgsConstructor`,注入 Mapper。写操作 `@Transactional`。不依赖 api 层 DTO。
4. **Controller**:`@RestController @RequestMapping("/api/v1/{module}/...")`,`R<T>` 包装,`@PreAuthorize("hasAuthority('{module}.{resource}.{action}')")`。
5. **种子**:DataInitializer 加菜单 + 按钮 + 分配 sysadmin。
6. **dataScope**:自动生效(DataScopeInterceptor 按 org_id 过滤)。

### 前端契约

- 路径含 `/api/v1`(vite 代理不剥离 `/api`)
- 响应 `{code, msg, data}`,code=0 成功
- 鉴权 `Authorization: Bearer <JWT>`,token 存 `localStorage['qms_token']`
- camelCase 字段(无 snake->camel 转换层)
- 401 -> 前端登出(无 refresh token)

## Conventions

- 代码规范文档 V1.0 是权威(`规范/QMS-代码规范文档-V1.0.md`),与其他文档冲突时以它为准。
- `R<T>` 用 `msg`(非 `message`)。
- 路径含 `/api/v1` 前缀。
- 分区表(如 spc_subgroup)仅 insert + select,不做 updateById(分区表复合 PK)。如需更新,用 LambdaUpdateWrapper 带分区键。
- 新建实体时注意 DB 表是否有 `org_id NOT NULL`(业务表)vs nullable(全局/配置表)。extends BaseEntity 的实体需自己加 `@TableField("org_id") private String orgId;`(BaseEntity 不含 org_id)。
- 子表/日志表(无审计字段)用 plain entity(不 extends BaseEntity),自有 `@TableId(type = IdType.ASSIGN_UUID)`。
- `currentOperator()` 取当前用户:`CompanyContext.get().userId()`,无上下文返回 "系统"。
- 开发文档在 `开发文档/`,TODO 在 `TODO.md`。
