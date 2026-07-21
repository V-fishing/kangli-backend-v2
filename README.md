# 康立 QMS 后端

康立质量过程管理系统(QMS)后端,模块化单体(5 个 Maven 子模块),按《QMS-代码规范文档-V1.0》组织。技术栈版本见《QMS-技术选型与部署方案-V1.5》§4.1。

Java 21 · Spring Boot 3.3.5 · MyBatis-Plus 3.5.9 · MapStruct 1.5.5 · PostgreSQL 16 · Flyway · Redis 7.2 · MinIO · Spring Security + JWT。

## 模块结构(代码规范§2.1)

| 模块 | 职责 | 包路径 |
|---|---|---|
| `qms-common` | 基础设施:`R<T>` / `BaseEntity` / 全局异常 / Security+JWT / `DataScopeInterceptor` / 审计切面 / MyBatis-Plus 配置 / 类型处理器 | `com.konli.qms.common` |
| `qms-domain` | 数据访问层:Entity / Mapper / Repository(按业务域分包) | `com.konli.qms.domain.{module}` |
| `qms-service` | 业务逻辑层:Service 接口 + impl + dto | `com.konli.qms.service.{module}` |
| `qms-api` | 接口层:Controller + 请求响应 DTO | `com.konli.qms.api.{module}` |
| `qms-bootstrap` | 启动:`QmsApplication` / `application.yml` / `logback` / `DataInitializer`(种子数据) / Flyway 迁移 | `com.konli.qms` |

依赖方向:`bootstrap -> api -> service -> domain -> common`(Service 不得依赖 api 层 DTO)。

> 根包为 `com.konli.qms`(注意是 `konli` 不是 `kangli`);项目文件夹叫 `kangli` 但包名是 `konli`。

## 已实现业务域

| 域 | 包 | 说明 |
|---|---|---|
| `uop` | 用户组织权限 | 账号 / 组织 / 角色 / 菜单 / 字典 / 授权 / 鉴权(JWT 登录) |
| `fia` | 首件检验 | 任务 / 审批 / 拦截配置 / 签名配置 / 检验标准 / 触发类型 |
| `patrol` | 巡检 | 巡检路线 / 任务 / 异常 |
| `spc` | 统计过程控制 | 控制图 / 子组 / 参数 / 控制限 / 规则 / 能力 / 报警 / 采集任务 / 通知渠道 / 全局配置 |
| `ncm` | 不合格品管理 | 缺陷字典 / 缺陷记录 / 8D / 鱼骨图 / CAPA / 纠正措施 / 预警升级 / BI 报表 / 日报配置 / 过滤方案 |
| `sqm` | 供应商质量 | 供应商 / 评级 / 绩效 / 审计 / 变更 / FMEA / 追溯 / 异常 / 分析 / 证书 / 共享 / 升级 / 测量 / SQE 验证 |
| `archive` | 归档 | 检验记录归档与查询 |

数据库 schema 由 Flyway 管理,基线迁移 `V01__schema_uop_global.sql` ~ `V16__fmea_risk_track.sql` 共 16 个脚本,schema 名 `ops`,遵循代码规范(`org_id` / UUID 主键 / `ops.*` 表前缀)。

## 关键基础设施(qms-common)

- **`R<T>`** = `{code, msg, data}`,`code=0` 为成功。前端 `request.ts` 已对齐读取 `msg`。
- **`BaseEntity`**:UUID 主键(ASSIGN_UUID)、`org_id`、审计字段(`createdAt`/`updatedAt`/`createdBy`/`updatedBy`)、`isDeleted`(`@TableLogic` 软删除)、`version`(`@Version` 乐观锁)。业务实体继承它并声明自己的 `orgId` 字段(`@TableField("org_id")`)。
- **`GlobalExceptionHandler`**:统一 401(未认证) / 403(`@PreAuthorize` 拒绝) / 404(NoResourceFoundException) / 业务码(`BusinessException`)。
- **`SecurityConfig` + `JwtAuthenticationFilter`**:无状态 JWT。每请求经 `PermissionLoader` 加载权限码(Redis 缓存 30min)为 authorities,供 `@PreAuthorize("hasAuthority('xxx')")` 校验;`@EnableMethodSecurity` 已开启。
- **`DataScopeInterceptor`**:MyBatis-Plus InnerInterceptor,非管理员用户的 SELECT 自动追加 `org_id = '<dataScope>'`(应用级 SQL 改写,非 PG RLS);管理员(`dataScope=all`)不过滤。
- **`AuditMetaObjectHandler`**:自动填充审计字段。

## 运行

1. 起开发基础设施(PostgreSQL 16 + Redis 7.2 + MinIO):
   ```powershell
   docker compose up -d
   ```
2. 启动后端(Flyway 自动建表 + 种子数据):
   ```powershell
   mvn -pl qms-bootstrap -am spring-boot:run
   ```
   或使用 wrapper:`./mvnw.cmd -pl qms-bootstrap -am spring-boot:run`

- 应用首页:http://localhost:8080
- 健康检查:http://localhost:8080/actuator/health
- Swagger UI:http://localhost:8080/swagger-ui.html

种子账号:`admin/admin123`(跨公司全量 `dataScope=all`)、`mzuser/user123`(仅梅州本公司)。

> `application-dev.yml` 中 Hikari 设了 `initialization-fail-timeout=-1`,**无库也能起冒烟**;连库后 Flyway 会自动执行迁移。

## 构建

```powershell
# 全模块构建(跳过测试)
mvn clean install -DskipTests

# 运行测试
mvn test

# 单模块测试
mvn -pl qms-service test
```

## 前端对接

前端 dev 跑在 `localhost:5173`,Vite 代理 `/api` -> `localhost:8080`(**不剥离前缀,直接透传**)。因此后端 Controller 统一使用 `/api/v1/{module}/...` 形式的 `@RequestMapping`,与前端 `src/api/modules/*.ts` 的路径一致。

契约按代码规范:`R<T>={code,msg,data}`,前端 `request.ts` 统一处理 JWT 注入、`X-Trace-Id`、业务码、401 跳登录。

## 多分公司

简化模型:公司 = 顶级 org(`org_type='公司'`);一个管理员 `dataScope=all` 看全部,普通用户按 `org` 看本公司。不加 `company_id` / `sys_user_company`,数据隔离由 `DataScopeInterceptor` 在应用层按 `org_id` 完成。

## 源码托管

- 仓库:`https://github.com/V-fishing/kangliQMSbackend.git`
- 克隆:
  ```powershell
  git clone https://github.com/V-fishing/kangliQMSbackend.git
  cd kangliQMSbackend
  ```
- 提交约定:模块化单体,提交信息建议带模块前缀,如 `feat(spc): 新增控制图接口`、`fix(auth): JWT 过期时间调整`。
- 本仓库已配置 `.gitignore`,自动忽略 `target/`、`**/target/`、`*.class`、`*.jar`、`*.log`、`logs/`、`.idea/`、`*.iml` 等构建产物与本地文件,**不会**提交编译后的类和日志。

> 开发环境配置(`application.yml` / `application-dev.yml`)中的数据库密码、MinIO 密钥、JWT secret 均为**本地开发占位值**,请勿在生产使用;生产环境应通过环境变量注入(Jasypt 加密 / `QMS_JWT_SECRET` 等)。
