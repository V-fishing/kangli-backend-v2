# 康立 QMS 新前端构建计划(qms-web-new)

## 目标
基于当前后端(7 域 56 Controller 272 接口),**全新从零**构建一份 Vue 3 + Element Plus 前端,放在 `QMS-fronted/konliQMS/qms-web-new`,严格对齐后端契约。现有 `qms-web` 保留不动。

## 技术栈(与后端契约对齐,版本参照现有 qms-web)
- Vue 3.5 + Vite 6 + TypeScript 5.6
- Element Plus 2.8 + @element-plus/icons-vue
- Pinia 2.2(状态)、vue-router 4.4(路由)、axios 1.7(HTTP)
- echarts 5.5(SPC/NCM/SQM 图表)、@vueuse/core、dayjs、lodash-es
- unplugin-auto-import + unplugin-vue-components(自动导入/注册)

## 项目结构
```
qms-web-new/
├── package.json / vite.config.ts / tsconfig.json / index.html
├── .env.development               # VITE_API_BASE=/api
└── src/
    ├── main.ts / App.vue
    ├── utils/request.ts           # axios + R<T> 对齐(契约中枢)
    ├── api/client.ts              # re-export request
    ├── api/modules/               # 按域组织,272 接口
    │   ├── auth.ts
    │   ├── uop/  (users orgs roles menus dict delegations)
    │   ├── fia/  (tasks incoming-checks approvals stds triggers intercept-config sign-config wo-lock)
    │   ├── patrol/ (routes tasks abnormals)
    │   ├── spc/  (chart params subgroups control-limits rules capability alarms collect-tasks notify-channels global-config)
    │   ├── ncm/  (defect-records 8d fishbones capas corrective-actions defect-dicts escalations filter-schemes bi-reports daily-report-config analysis)
    │   ├── sqm/  (suppliers grade-rules performance measures shares escalations supplier-certs abnormals audits changes strict-inspects fmea verifications analysis audit-freq-rules trace)
    │   └── archive/
    ├── types/                     # 实体/DTO/VO 类型(基于后端)
    ├── stores/ (auth permission company)
    ├── permission/ (directive v-permission, guard)
    ├── router/ (index guard dynamic)
    ├── layouts/ (BasicLayout BlankLayout)
    ├── components/ (通用:表格/表单/搜索栏/分页)
    └── views/ (login company-select dashboard system fia patrol spc ncm sqm archive)
```

## 基础设施(对齐后端契约 — 必须保留的四要素)
1. **request.ts**:baseURL=`/api`;请求拦截器注入 `Authorization: Bearer <token>`(key `qms_token`)+ `X-Trace-Id`;响应拦截器读 `R<T>`(`code===0` 成功、读 **`msg`** 非 message、解包 `data`);401 清 token 跳 `/login?redirect=`(标志位防并发)。
2. **权限**:`v-permission` 指令 + `permissionStore.hasOp(code)`,codes 来自 `me().permissions`,`isAdmin = codes.includes('*')`。
3. **stores**:auth(login→`/v1/auth/login`、me→`/v1/uop/me` 注入权限、restore、logout);permission(hasOp);company(多分公司 currentOrgId,集团返回空)。
4. **路由**:静态(login/company-select/404)+ 动态(从 `/v1/uop/menus/tree` 构建);登录守卫(无 token→login,未选公司→company-select)+ 权限守卫。
5. **布局**:BasicLayout(顶栏:品牌+公司切换+用户;侧边:菜单;主:RouterView+keep-alive)。
6. **vite**:代理 `/api`→`localhost:8080` **不剥离前缀**;AutoImport/Components。

## API 层 + TS 类型
- `api/modules/` 按域 7 域 + auth,逐接口封装(基于 272 接口清单)。
- `types/` 基于后端实体/DTO/VO 定义 TS 接口(后端大量直接用实体做 RequestBody,需参照实体字段)。
- 文件上传/下载(uploadPhoto/downloadReport/getPhoto)用 `responseType:'blob'`。

## 分阶段(每阶段交付可运行模块)
- **阶段 0**:脚手架 + 基础设施 + **uop**(登录、公司选择、用户、角色、组织、菜单、字典、代班 — 27 接口)。交付可登录+系统管理。
- **阶段 1**:**fia**(43 接口:任务、来料首件、审批、检验标准、触发类型、拦截配置、签名配置、工单锁)。
- **阶段 2**:**spc**(33 接口:控制图/直方图、参数、子组、控制限、判异规则、能力指数、告警、采集任务、通知渠道、全局配置)。
- **阶段 3**:**ncm**(53 接口:不良记录、8D、鱼骨图、CAPA、纠正措施、字典、升级、分析方案、BI、日报、多维分析)。
- **阶段 4**:**sqm**(102 接口,最大:供应商、评级、绩效、措施、份额、升级、资质、来料异常、审核、变更、加严、FMEA、SQE验证、分析、频次规则、追溯)。
- **阶段 5**:**patrol**(12 接口:路线、任务、异常)。
- **阶段 6**:**archive**(2 接口:归档查询、到期提醒)+ 整体联调。

## 后端需顺手修的 2 个 bug(否则前端对应接口 403/权限异常)
1. `ArchiveController` 的 `@PreAuthorize` 括号语法错误(`sqm.audit.list'))` or ...` 多一个 `)`)→ 归档接口可能全 403。
2. `NcmCorrectiveActionController.close()` 双 `@PreAuthorize`(同类型重复,只最后一个 `ncm.record.create` 生效)→ 权限不符预期。
> 这两个在阶段 6(ncm/archive)前修掉即可,不阻塞前面阶段。

## 验证
每阶段:`pnpm dev` 启动,对齐**运行中的后端**(后端用 IDE 跑,前端调 localhost:8080;后端命令行 Maven 的 api Lombok 问题不影响运行时)。Swagger 手测关键流程。

## 工作量与节奏
272 接口 / 7 域。每阶段 1-3 轮(API 层 + 类型 + 页面)。全部约 10-15 轮分阶段交付,每阶段结束可独立运行验证。

## 约定
- 权限码逐接口对照清单(后端有不统一处,如 fia 写操作多用 `fia.std.create`、专属码 `fia.sign.inspector`/`sqm.change.rollback` 等,不能假设统一模式)。
- camelCase 字段(无 snake→camel 转换层)。
- 路径前缀 `/api/v1/{module}/...`,baseURL `/api` + 模块内 `/v1/...`。
