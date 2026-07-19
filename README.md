# 康立 QMS 后端

模块化单体(5 模块),按《QMS-代码规范文档-V1.0》组织。技术栈版本见《QMS-技术选型与部署方案-V1.5》§4.1。

## 模块结构(代码规范§2.1)

| 模块 | 职责 | 包路径 |
|---|---|---|
| `qms-common` | 公共:BaseEntity / R&lt;T&gt; / 全局异常 / 审计切面 / 工具(stage-4 落地) | `com.konli.qms.common` |
| `qms-domain` | 数据访问层:Entity / Mapper / Repository | `com.konli.qms.domain.{module}` |
| `qms-service` | 业务逻辑层:Service / impl / dto | `com.konli.qms.service.{module}` |
| `qms-api` | 接口层:Controller / 请求响应 DTO | `com.konli.qms.api.{module}` |
| `qms-bootstrap` | 启动:QmsApplication / application.yml / logback | `com.konli.qms` |

依赖方向:`bootstrap -> api -> service -> domain -> common`

一期模块包(代码规范§2.2 + patrol):`uop` `fai` `patrol` `spc` `nc` `tool` `sqa` `ecm` `lot` `capa` `glb`

## 技术栈

Java 21 · Spring Boot 3.3.x · MyBatis-Plus 3.5.x · MapStruct 1.5.x · PostgreSQL 16 · Flyway · Redis 7.2
(stage-4+ 引入:Spring Security+JWT / MinIO / Flowable / LiteFlow / XXL-Job / Redisson / FastExcel / openhtmltopdf / POI / Jasypt)

## 运行(空壳冒烟)

1. 起开发基础设施(PG16 + Redis7 + MinIO):
   ```powershell
   docker compose up -d
   ```
2. 启动后端:
   ```powershell
   mvn -pl qms-bootstrap -am spring-boot:run
   ```
   > 空壳设了 Hikari `initialization-fail-timeout=-1`,**无库也能起冒烟**;stage-1 建库后可删该行。

- 应用首页:http://localhost:8080
- 健康检查:http://localhost:8080/actuator/health
- Swagger UI:http://localhost:8080/swagger-ui.html

## 前端对接

前端 dev 跑在 `localhost:5173`,vite 代理 `/api` -> `localhost:8080`(剥离 `/api` 前缀),故后端 `@RequestMapping` **不含 `/api`**。
> 契约按代码规范文档:`R<T>={code,msg,data}`。前端 `request.ts` 现读 `message`,stage-4 需改为 `msg`。

## 多分公司

简化模型:公司 = 顶级 org(`org_type='公司'`);一个管理员 `dataScope=all` 看全部,普通用户按 `org` 看本公司。不加 `company_id`/`sys_user_company`。

## 后续 stage

- **stage-1**:Flyway 迁移脚本(基线 128 表按 `org_id`/UUID/`ops.*` 重做)-> 开启 `spring.flyway.enabled=true`
- **stage-4**:基础设施层(`R<T>` / `BaseEntity` / MyBatis-Plus 配置 / Security+JWT / `dataScope` 拦截器 / 审计切面 / JSON 日志)

## 源码托管

- 仓库:`https://github.com/V-fishing/kangliQMSbackend.git`
- 克隆:
  ```powershell
  git clone https://github.com/V-fishing/kangliQMSbackend.git
  cd kangliQMSbackend
  ```
- 提交约定:模块化单体,提交信息建议带模块前缀,如 `feat(spc): 新增控制图接口`、`fix(auth): JWT 过期时间调整`。
- 本仓库已配置 `.gitignore`,自动忽略 `target/`、`*.class`、`*.log`、`logs/`、`*.idea` 等构建产物与本地文件,**不会**提交编译后的类和日志。

> 开发环境配置(`application.yml` / `application-dev.yml`)中的数据库密码、MinIO 密钥、JWT secret 均为**本地开发占位值**,请勿在生产使用;生产环境应通过环境变量注入。
