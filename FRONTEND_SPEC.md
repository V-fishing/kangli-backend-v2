# QMS 前端构建规格书

> 基于 kangli 后端 (398 Java / 54 Controller / ~200 API) | 最后更新: 2026-07-24

---

## 〇、全局约定

### 响应结构

```json
{ "code": 0, "msg": "success", "data": { ... } }
```

- `code`: 0=成功, 非0=失败
- `msg`: 消息（**注意是 `msg` 不是 `message`**）
- `data`: 业务数据

### 认证

```
POST /api/v1/auth/login     → { account, password }  → { token, refreshToken, userInfo }
Header: Authorization: Bearer <token>
Token 过期 → POST /api/v1/auth/refresh → { refreshToken } → 新 token
```

### 数据隔离

- 每个业务实体带 `org_id` 字段
- 后端 `DataScopeInterceptor` 自动按用户所属公司过滤
- 前端无需传 `org_id`，JWT 中已包含
- 跨公司管理员 (`admin`) 的 `dataScope=all` 不过滤

### 权限码（74 个）

每行格式：`权限码  →  控制的操作  |  建议角色`

```
── FIA 首件检验 ──
fia.std.list             查看检验标准              (检验员+)
fia.std.create           创建/编辑检验标准          (质量工程师+)
fia.task.list            查看任务列表/详情          (检验员+)
fia.task.create           创建任务 / 录入参数       (检验员+)
fia.sign.inspector        检验员签名(密码)          (检验员)
fia.sign.reviewer         复核人签名(密码)          (班组长+)
fia.sign.approver         批准人签名(密码)          (质量经理+)
fia.task.disposition      设置处置路径              (质量工程师+)

── IncomingCheck 供应商来料首件 ──
(共用 fia.task.list / fia.task.create / fia.sign.* / fia.sign.disposition)

── SPC 过程能力 ──
spc.param.list           查看参数                  (检验员+)
spc.param.create          创建/编辑参数             (质量工程师+)
spc.subgroup.list        查看子组                  (检验员+)
spc.subgroup.create       录入子组                  (检验员+)
spc.alarm.list           查看告警                  (检验员+)
spc.alarm.close           关闭告警                  (班组长+)
spc.alarm.launch-8d       告警→发起8D               (质量工程师+)
spc.capability.list      查看能力分析              (检验员+)
spc.rule.list            查看判异规则              (质量工程师+)

── NCM 不良管理 ──
ncm.defect.list           查看不良字典             (检验员+)
ncm.defect.create          编辑不良字典             (质量工程师+)
ncm.record.list           查看不良记录             (检验员+)
ncm.record.create          录入不良记录             (检验员+)
ncm.8d.list               查看 8D 列表/详情        (检验员+)
ncm.8d.create              创建 8D / 填写阶段内容   (检验员+)
ncm.8d.advance             推进到下一阶段           (班组长+)
ncm.8d.approve             审批/驳回 D3/D5/D7      (质量经理+)
ncm.8d.reopen              重开已闭环 8D            (质量工程师+)
ncm.capa.list             查看 CAPA 列表           (检验员+)
ncm.capa.create            创建 CAPA               (质量工程师+)
ncm.capa.close             关闭 CAPA               (质量经理+)   ← 需 progress=100
ncm.capa.approve           审批/驳回 CAPA          (质量经理+)
ncm.capa.reset             重置 CAPA               (质量工程师+)
ncm.corrective.close       关闭纠正措施             (质量工程师+)

── SQM 供应商质量 ──
sqm.supplier.list          查看供应商               (检验员+)
sqm.supplier.create         编辑供应商               (SQE+)
sqm.audit.list             查看审核                (检验员+)
sqm.audit.create            创建审核计划/记录       (SQE+)
sqm.audit.plan.start       开始审核                (SQE+)
sqm.audit.nc.close          关闭 NC                 (SQE+)
sqm.audit.archive           生成归档报告            (SQE+)
sqm.abnormal.list          查看来料异常             (检验员+)
sqm.abnormal.create         创建来料异常             (SQE+)
sqm.abnormal.close          关闭来料异常             (SQE+)        ← 需关联8D已闭环
sqm.abnormal.escalation-check 批量升级检查          (质量经理+)
sqm.change.list            查看物料变更             (检验员+)
sqm.change.create           创建物料变更             (SQE+)
sqm.change.submit           提交变更                 (SQE+)
sqm.change.approve          审批变更                 (质量经理+)
sqm.change.close            关闭变更                 (SQE+)
sqm.change.rollback         回退变更                 (质量经理+)
sqm.change.verify-sign      变更签名验证             (SQE+)
sqm.fmea.list              查看 FMEA               (SQE+)
sqm.fmea.edit               编辑 FMEA               (SQE+)
sqm.fmea.close              闭环风险                 (SQE+)        ← 需 evidence
sqm.fmea.reopen             重开风险                 (SQE+)
sqm.fmea.scan-overdue       扫描超期                 (质量经理+)
sqm.trace.list             查看追溯                 (检验员+)
sqm.trace.create            创建追溯节点             (SQE+)

── Patrol 巡检 ──
patl.route.list             查看路线                 (检验员+)
patl.route.create            创建/编辑路线            (班组长+)
patl.task.list              查看任务                 (检验员+)
patl.task.create             创建任务                 (班组长+)
patl.task.record            提交巡检记录              (检验员+)
patl.task.close             关闭任务                 (班组长+)

── System 系统管理 ──
system.user.list            查看用户                 (管理员)
system.user.create           创建/编辑用户            (管理员)
system.user.delete           删除用户                 (管理员)
system.role.list            查看角色                 (管理员)
system.role.assign           分配角色权限             (管理员)
system.menu.list            查看菜单                 (管理员)
system.menu.create           创建/编辑菜单            (管理员)
system.org.list             查看组织                 (管理员)
system.delegation.manage     代班管理                (管理员)
```

---

## 全局节点流程

> 每个节点标注了：**当前状态下可执行的操作按钮** + **下一步可到达的状态**。

### 0.1 首件检验 (产线 FIA)

```
                        ┌─ [创建任务] ──────────────────────────┐
                        ↓                                         │
┌──────────────────────────────────────────────────────────────┐ │
│ 待检                                                        │ │
│ 按钮: [录入参数]                          perm: fia.task.create│
│ → 录入参数(首次) → 进行中                                   │ │
└──────────────────────────────────────────────────────────────┘ │
                        ↓                                         │
┌──────────────────────────────────────────────────────────────┐ │
│ 进行中                                                       │ │
│ 按钮: [录入参数(补充)] fia.task.create                       │ │
│       [检验员签名]     fia.sign.inspector                    │ │
│ → 检验员签名(密码) → 待复核                                  │ │
└──────────────────────────────────────────────────────────────┘ │
                        ↓                                         │
┌──────────────────────────────────────────────────────────────┐ │
│ 待复核                                                       │ │
│ 按钮: [复核人签名]    fia.sign.reviewer                     │ │
│ → 两级签名: 签名→已完成+归档                                 │ │
│ → 三级签名: 签名→审批中                                      │ │
└──────────────────────────────────────────────────────────────┘ │
                  ↓ (三级)          ↓ (两级)                      │
┌────────────────────────────┐  ┌──────────────────────────────┐ │
│ 审批中                      │  │ 已完成                       │ │
│ 按钮: [批准人签名]          │  │ 按钮: [查看报告] [归档]      │ │
│       fia.sign.approver     │  │        fia.task.list         │ │
│       [驳回]                │  │ → SPC联动(CTQ写入)           │ │
│ → 签名→已完成+归档          │  │ → 追溯联动(合格→物料表)     │ │
│ → 驳回→已驳回               │  │ → 工单解锁                   │ │
└────────────────────────────┘  └──────────────────────────────┘ │
         ↓                                                        │
┌──────────────────────────────────────────────────────────────┐ │
│ 已驳回                                                       │ │
│ 按钮: [重新提交]       fia.task.create                       │ │
│ → 退回至待复核或进行中                                       │ │
└──────────────────────────────────────────────────────────────┘ │

处置路径 (设置时需要 fia.task.disposition 权限):
  FACTORY: 退货 | 返工 | 让步接收 | 紧急放行 | 豁免开工
  让步接收/紧急放行/豁免开工 → 自动发起审批单 → 审批通过才放行
```

### 0.2 供应商来料首件 (IncomingCheck)

```
来料批次入库 (SqmIncomingLot)
        ↓
┌──────────────────────────────────────────────┐
│ [batch-by-lot]  AQL 抽样计算 → 批量建单      │
│ 自动匹配检验计划 → 分配到检验员              │
└──────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────┐
│ 待检                                        │
│ 可操作: [录入参数]                           │
└──────────────────────────────────────────────┘
        ↓        (签名流程同产线FIA)
┌──────────────────────────────────────────────┐
│ 待复核 → 审批中 → 已完成                     │
│ 处置枚举: 合格入库 | 退货 | 让步接收 | 挑选   │
│ 不触发工单锁定 / SPC联动                     │
│ 触发: 来料追溯联动                           │
└──────────────────────────────────────────────┘
```

### 0.3 8D 报告流程

```
不良记录 / 来料异常 / SPC告警
        ↓
┌──────────────────────────────────────────────────────────────┐
│ 创建 8D                                                      │
│ 按钮: [创建] ncm.8d.create                                  │
│       [从来料异常发起 launch] ncm.8d.create                  │
│ → 简易流程(flowType=简易) → 直接 D8 闭环                     │
│ → 标准流程 → D1                                              │
└──────────────────────────────────────────────────────────────┘
        ↓
┌──────┐   ┌──────┐   ┌──────────┐   ┌──────┐   ┌──────────┐
│ D1   │→  │ D2   │→  │ D3       │→  │ D4   │→  │ D5       │
│ 团队 │   │ 问题 │   │ 遏制措施  │   │ 根因 │   │ 纠正措施  │
│      │   │      │   │ 🔒需审批  │   │      │   │ 🔒需审批  │
└──────┘   └──────┘   └──────────┘   └──────┘   └──────────┘
                        ↑ 审批需:                 ↑ 审批需:
                     ncm.8d.approve          ncm.8d.approve
                                                    │ D4 高严重度
                                                    │ →自动触发 CAPA
                                                    ↓
┌──────┐   ┌──────────┐   ┌──────────┐
│ D6   │→  │ D7       │→  │ D8       │
│ 实施 │   │ 预防措施  │   │ 闭环总结  │
│      │   │ 🔒需审批  │   │ 关闭异常单 │
└──────┘   └──────────┘   └──────────┘
              ↑ 审批需:
           ncm.8d.approve

按钮权限:
  [推进 advance]  ncm.8d.advance  body: { stageCode, content, owner }
  [审批 approve]  ncm.8d.approve  body: { approved, comment, approver }
    → 驳回 → currentStage 退回至该阶段，重新提交
    → 通过 → 调用 advance 推进到下一阶段
    推进前自动校验: 前一阶段若是 D3/D5/D7, 审批必须"已通过"
  [重开 reopen]   ncm.8d.reopen   已闭环 → 回到进行中
```

### 0.4 CAPA 流程

```
8D D4(高严重度) 或 不良记录手动发起
        ↓
┌──────────────────────────────────────────────────────┐
│ 待启动                                               │
│ 按钮: [开始分析]                     ncm.capa.create  │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 分析中                                               │
│ 按钮: [更新进度] ncm.capa.create                      │
│       [重置]     ncm.capa.reset                        │
│ → progress ≥ 60 且当前非实施中 → 待审批              │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 待审批                                               │
│ 按钮: [通过] ncm.capa.approve                         │
│       [驳回] ncm.capa.approve                          │
│ → 通过 → 实施中                                      │
│ → 驳回 → 分析中 (progress 回退到 50)                  │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 实施中                                               │
│ 按钮: [更新进度] ncm.capa.create                      │
│ → progress = 100 → 已验证                            │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 已验证 (progress=100)                                 │
│ 按钮: [关闭] ncm.capa.close                           │
│ 关闭前置条件: progress=100 且 status=已验证            │
│ → 关闭 → 级联关闭关联的 8D → 级联关闭来源异常单       │
└──────────────────────────────────────────────────────┘
```

### 0.5 来料异常 → 整改 → 闭环

```
供应商来料 → 检验不合格
        ↓
┌──────────────────────────────────────────────────────┐
│ 待整改 (创建异常单)                                   │
│ 可操作: [发起8D launch] [发起CAPA] [制定整改方案]     │
│ → 发起8D → 整改中                                    │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 整改中                                               │
│ 可操作: [更新整改方案] [关联8D] [关联CAPA]            │
│ → 8D 闭环 → 待验证                                   │
│ → CAPA 已验证 → 可关闭                               │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 待验证                                               │
│ 可操作: [三批验证] [关闭]                             │
│ 关闭前置条件: 关联8D已闭环 或 关联CAPA已验证           │
└──────────────────────────────────────────────────────┘

定时任务: 每天 8:00 扫描超7天未闭环异常 → 通知 SQE → 超14天升级质量经理+采购
重复问题: 30天内同 supplier+物料 ≥2次 → 自动创建升级记录 → 增加审核频次
```

### 0.6 SPC 数据采集 → 告警

```
┌──────────────────────────────────────────────────────┐
│ 工序配置 → 参数定义 → 子组采集 → 控制图渲染          │
│ 可操作: [创建工序] [创建参数] [录入子组]              │
│                                                    │
│ 每次录入子组 → 自动计算控制限 → WECO 8规则判异       │
│   → 预警: 仅站内弹窗通知                             │
│   → 报警: 站内弹窗 + 企微/钉钉 (按管理员配置的渠道)   │
│                                                    │
│ 告警可操作: [关闭] [发起8D]                          │
└──────────────────────────────────────────────────────┘
```

### 0.7 供应商审核流程

```
┌──────────────────────────────────────────────────────┐
│ 审核计划 (待确认)                                    │
│ 可操作: [确认] [编辑]                                 │
│ → 确认 → 已确认                                      │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 审核计划 (已确认)                                    │
│ 可操作: [开始审核]                                    │
│ → 开始 → 审核中 → 创建审核记录                        │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 审核记录                                             │
│ 可操作: [创建NC] [生成报告] [归档]                    │
│                                                    │
│ NC (不符合项)                                       │
│   可操作: [关闭] → 需 verifyResult + verifyComment    │
│                                                    │
│ NC 超期扫描: 每天 8:00 扫描 deadline 已过未闭环       │
│   → 升级通知采购+质量经理                            │
└──────────────────────────────────────────────────────┘
```

### 0.8 物料变更流程

```
┌──────────────────────────────────────────────────────┐
│ 变更申请 (创建)                                      │
│ 按钮: [提交] sqm.change.submit                        │
│       [编辑] sqm.change.create                        │
│       [删除] sqm.change.create                        │
│ → 提交 → 待审批                                      │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 待审批                                               │
│ 按钮: [通过] sqm.change.approve                       │
│       [驳回] sqm.change.approve                        │
│       [回退] sqm.change.rollback                        │
│ → 通过 → 实施中                                      │
│ → 驳回 → 退回                                        │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 实施中                                               │
│ 按钮: [验证签名] sqm.change.verify-sign               │
│       [关闭]     sqm.change.close                     │
│ → 签名完成 → 关闭                                    │
└──────────────────────────────────────────────────────┘
```

### 0.9 FMEA 风险闭环

```
┌──────────────────────────────────────────────────────┐
│ 风险项 (创建)                                        │
│ 可操作: [更新] [闭环 close]                           │
│                                                    │
│ 闭环条件:                                           │
│   - 必须提供 evidence (措施执行证据)                  │
│   - 高风险项 (highRiskFlag=true):                    │
│     必须确认 recurrenceVerified (3个月无复发)         │
│                                                    │
│ 可操作: [重开 reopen] → 重新评估                     │
│ 定时: [扫描超期 scan-overdue]                        │
└──────────────────────────────────────────────────────┘
```

### 0.10 巡检流程

```
┌──────────────────────────────────────────────────────┐
│ 巡检路线 (创建)                                      │
│ 可操作: [编辑] [删除]                                │
│   body: { name, checkpoints: [{name, standard}] }   │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 巡检任务 (创建)                                      │
│ 可操作: [提交记录] [关闭]                             │
│ → submitRecord: { checkpointId, result, remark }    │
│ → close: 关闭任务                                    │
└──────────────────────────────────────────────────────┘
        ↓ (发现异常)
┌──────────────────────────────────────────────────────┐
│ 巡检异常                                             │
│ 可操作: [关闭]                                       │
└──────────────────────────────────────────────────────┘
```

### 0.11 追溯查询流程

```
┌──────────────────────────────────────────────────────┐
│ 来料批次管理                                         │
│ 可操作: [创建批次] [搜索] [查看批次详情]              │
└──────────────────────────────────────────────────────┘
        ↓
┌──────────────────────────────────────────────────────┐
│ 追溯节点                                             │
│ 可操作: [创建节点] [搜索节点] [查看节点详情]          │
│   [查看产品详情] [查看原材料详情]                     │
│                                                    │
│ 追溯查询:                                           │
│   /trace/tree          → 按 nodeCode 查树           │
│   /trace/full-tree     → 完整追溯树                  │
│   /trace/tree-recursive → 递归追溯                   │
│   /trace/tree-from-node → 从指定节点向上追溯          │
│   /trace/direction      → 双向追溯方向               │
│                                                    │
│ 关键件序列号:                                       │
│   /key-part-sns        → 管理关键件对应关系          │
└──────────────────────────────────────────────────────┘
```

### 0.12 认证与多公司流程

```
┌──────────────────────────────────────────────────────┐
│ 登录 /api/v1/auth/login                              │
│   body: { account, password }                        │
│   → JWT token (含 orgId, dataScope)                  │
│   → 前端存储 token 到 localStorage                    │
│                                                    │
│ 获取用户信息 GET /api/v1/uop/me                      │
│   → { username, dataScope, permissions[] }           │
│   → dataScope=all → 跨公司管理员 (看全量)             │
│   → dataScope=orgId → 仅看该公司数据                  │
│                                                    │
│ 公司切换: 无传统"切换"——跨公司管理员自动看全量        │
│ 普通用户固定绑定一个 orgId, 前端无需传公司参数         │
│                                                    │
│ Token 刷新: POST /api/v1/auth/refresh                │
│   → 新 token + refreshToken (滚动刷新,旧refresh失效)  │
│                                                    │
│ 权限缓存: 每请求查 Redis (key: qms:perms:{userId})    │
│ TTL 30min, 缓存带版本号; 权限表触发器自动 bump        │
│ ops.sys_perm_version, 任何改库路径都强制失效缓存       │
└──────────────────────────────────────────────────────┘
```

---

## 一、UOP — 用户组织权限

### 端点

```
POST   /api/v1/auth/login          body: { account, password }
POST   /api/v1/auth/refresh        body: { refreshToken }
GET    /api/v1/uop/me               → 当前用户 + 权限码列表
GET    /api/v1/uop/users            → 用户列表
POST   /api/v1/uop/users            → 创建用户
PUT    /api/v1/uop/users/{id}       → 编辑用户
DELETE /api/v1/uop/users/{id}       → 删除用户 (软删除)
POST   /api/v1/uop/users/{id}/reset-password
POST   /api/v1/uop/users/{id}/roles → 分配角色
GET    /api/v1/uop/users/{id}/roles
GET    /api/v1/uop/roles            → 角色列表
POST   /api/v1/uop/roles            → 创建角色
DELETE /api/v1/uop/roles/{id}
POST   /api/v1/uop/roles/{id}/menus    → 角色赋菜单
POST   /api/v1/uop/roles/{id}/buttons  → 角色赋按钮
POST   /api/v1/uop/roles/{id}/users    → 角色赋用户
GET    /api/v1/uop/roles/{id}/users
GET    /api/v1/uop/menus            → 菜单列表
GET    /api/v1/uop/menus/tree       → 菜单树
POST   /api/v1/uop/menus            → 创建菜单
DELETE /api/v1/uop/menus/{id}
GET    /api/v1/uop/orgs             → 组织列表
GET    /api/v1/uop/orgs/tree        → 组织树
POST   /api/v1/uop/orgs             → 创建组织
DELETE /api/v1/uop/orgs/{id}
GET    /api/v1/uop/delegations      → 代班列表
POST   /api/v1/uop/delegations      → 创建代班
POST   /api/v1/uop/delegations/{id}/revoke
GET    /api/v1/dict                 → 字典列表
GET    /api/v1/dict/{type}          → 按类型查字典
```

### 菜单/按钮模型

```
sys_menu (菜单)          sys_button (按钮)
├── menu_code            ├── btn_code
├── menu_name            ├── btn_name
├── path                 └── menu_id (FK → sys_menu)
├── component
└── parent_id (树)
```

---

## 二、FIA — 首件检验（产线首件）

BASE: `/api/v1/fia/tasks`

### 业务流程

```
创建任务 → 录入检验参数 → 检验员签名(密码) → 复核人签名
  → 批准人签名 → 处置(放行/返工/让步接收) → 审批 → 归档
  → SPC联动(CTQ数据写入) → 来料追溯联动 → 工单解锁
```

### 状态机

```
待检 → 进行中 → 待复核 → 审批中 → 已完成
  ↓                 ↓         ↓
超时              已驳回    已驳回
```

### 端点

```
GET    /dashboard              → 看板 (KPI/状态分布/趋势)
GET    /                       → 任务列表 (全量, 不分页)
GET    /{id}                   → 详情 (含检验项+日志)
GET    /match-std              → 按物料+工序匹配检验标准
POST   /                       → 创建任务 body: { woNo, lineName, procName, triggerType, stdId, ... }
POST   /{id}/items             → 录入参数 body: { items: [{id, measuredValue, judge}] }
POST   /{id}/sign-inspector    → 检验员签名 body: { password }
POST   /{id}/sign-reviewer     → 复核人签名 body: { password }
POST   /{id}/sign-approver     → 批准人签名 body: { password }
POST   /{id}/disposition       → 处置 ?disposition=放行/返工/让步接收/紧急放行/豁免开工
GET    /{id}/archive           → 归档报告
GET    /archives               → 归档列表
GET    /{id}/log                → 操作日志 (10步)
```

### disposition 枚举 (产线)

```
合格放行 | 退货 | 返工 | 让步接收 | 紧急放行 | 豁免开工
```

### 关联子模块

```
GET    /api/v1/fia/triggers        → 触发类型 CRUD
GET    /api/v1/fia/stds            → 检验标准 CRUD
GET    /api/v1/fia/approvals       → 审批单 (列表/详情/创建/通过)
GET    /api/v1/fia/wo-lock         → 工单锁定列表
GET    /api/v1/fia/intercept-config → 拦截配置
GET    /api/v1/fia/sign-config     → 签名配置
```

---

## 三、IncomingCheck — 供应商来料首件检验

BASE: `/api/v1/fia/incoming-checks`

### 业务流程

```
来料批次入库 → AQL抽样计算 → 自动建首件任务 → 检验员签名
  → 复核 → 批准 → 处置(合格入库/退货/让步接收/挑选)
```

### 端点

```
GET    /dashboard              → 看板 (状态分布/今日汇总/批次覆盖率)
GET    /                       → 列表 (自动过滤 source=SUPPLIER)
GET    /{id}                   → 详情
GET    /match-std              → 按物料+供应商+工序匹配标准
POST   /batch-by-lot           → 来料批次批量建单 body: { lotNo, orgId }
POST   /{id}/items             → 录入参数
POST   /{id}/sign-inspector    → 检验员签名
POST   /{id}/sign-reviewer     → 复核人签名
POST   /{id}/sign-approver     → 批准人签名
POST   /{id}/disposition       → 处置 ?disposition=合格入库/退货/让步接收/挑选
```

### disposition 枚举 (来料)

```
合格入库 | 退货 | 让步接收 | 挑选
```

---

## 四、SPC — 过程能力

### 端点

```
GET    /api/v1/spc/dashboard              → 看板
GET    /api/v1/spc/params                  → 参数 CRUD
GET    /api/v1/spc/subgroups               → 子组 CRUD + 采集
POST   /api/v1/spc/subgroups               → 创建子组 body: { paramId, sampleValues[], ... }
GET    /api/v1/spc/control-chart           → 控制图数据 (Xbar-R/Xbar-S)
GET    /api/v1/spc/histogram               → 直方图
GET    /api/v1/spc/capability              → 过程能力 CP/CPK
POST   /api/v1/spc/capability/calc         → 重算能力指数
GET    /api/v1/spc/capability/trend        → CPK 趋势
GET    /api/v1/spc/capability/supplier-cpk → 供应商 CPK
GET    /api/v1/spc/alarms                  → 告警列表
POST   /api/v1/spc/alarms/{id}/close       → 关闭告警
POST   /api/v1/spc/alarms/{id}/launch-8d   → 告警→发起8D
GET    /api/v1/spc/rules                   → 判异规则 (WECO 1-8)
GET    /api/v1/spc/rules/triggers          → 规则触发记录
GET    /api/v1/spc/control-limits          → 控制限
POST   /api/v1/spc/control-limits/calc     → 重算控制限
GET    /api/v1/spc/notify-channels         → 通知渠道
PUT    /api/v1/spc/notify-channels/{id}/toggle → 启停渠道
GET    /api/v1/spc/notify-channels/records → 通知记录
GET    /api/v1/spc/collect-tasks           → 采集任务
GET    /api/v1/spc/global-config           → 全局配置
```

### SPC 判异规则 (WECO 1-8)

```
1. 1点超出 ±3σ
2. 连续9点在中心线同一侧
3. 连续6点递增或递减
4. 连续14点交替升降
5. 连续3点中2点超出 ±2σ
6. 连续5点中4点超出 ±1σ
7. 连续15点在中心线 ±1σ 内
8. 连续8点都在 ±1σ 外
```

---

## 五、NCM — 不良管理

### 端点

```
GET    /api/v1/ncm/dashboard              → 看板
GET    /api/v1/ncm/defect-records         → 不良记录 CRUD
POST   /api/v1/ncm/defect-records         → 录入不良
POST   /api/v1/ncm/defect-records/{id}/launch-8d  → 发起8D
POST   /api/v1/ncm/defect-records/{id}/launch-capa → 发起CAPA
GET    /api/v1/ncm/defect-dicts           → 不良字典 CRUD
GET    /api/v1/ncm/analysis/multi-dim     → 多维度分析
GET    /api/v1/ncm/analysis/trend         → 趋势分析
GET    /api/v1/ncm/analysis/compare       → 环比同比
GET    /api/v1/ncm/analysis/aggregate     → 聚合分析
GET    /api/v1/ncm/analysis/cross         → 交叉分析
POST   /api/v1/ncm/analysis/check-anomaly → 异常检测

GET    /api/v1/ncm/8d-reports             → 8D 列表
POST   /api/v1/ncm/8d-reports             → 创建 8D
POST   /api/v1/ncm/8d-reports/launch      → 从来料异常发起 8D
POST   /api/v1/ncm/8d-reports/{id}/advance → 推进到下一阶段 body: { stageCode, content, owner }
POST   /api/v1/ncm/8d-reports/{id}/approve → 审批阶段 body: { approved, comment, approver }
POST   /api/v1/ncm/8d-reports/{id}/reopen  → 重开 8D

GET    /api/v1/ncm/capas                  → CAPA 列表
POST   /api/v1/ncm/capas                  → 创建 CAPA
POST   /api/v1/ncm/capas/{id}/progress    → 更新进度 body: { progress }
POST   /api/v1/ncm/capas/{id}/close       → 关闭 CAPA (需 progress=100 + status=已验证)
POST   /api/v1/ncm/capas/{id}/approve     → 审批 CAPA
POST   /api/v1/ncm/capas/{id}/reset       → 重置 CAPA

GET    /api/v1/ncm/corrective-actions     → 纠正措施 CRUD + 进度 + 关闭
GET    /api/v1/ncm/escalations            → 升级记录 CRUD
GET    /api/v1/ncm/bi-reports             → BI 报表 CRUD
GET    /api/v1/ncm/filter-schemes         → 筛选方案 CRUD
GET    /api/v1/ncm/fishbones              → 鱼骨图 CRUD
GET    /api/v1/ncm/daily-report-config    → 日报配置
```

### 8D 状态机

```
D1(团队) → D2(问题) → D3(遏制-审批) → D4(根因) → D5(纠正-审批)
  → D6(实施) → D7(预防-审批) → D8(闭环)
  
简易流程: 创建时 flowType=简易 → 直接 D8 闭环
高严重度: D4 自动触发 CAPA
```

### CAPA 状态机

```
待启动 → 分析中 → 待审批 → 实施中 → 已验证(progress=100) → 已关闭
```

---

## 六、SQM — 供应商质量

### 端点

```
# 供应商管理
GET    /api/v1/sqm/suppliers              → 列表 CRUD
GET    /api/v1/sqm/supplier-certs         → 供应商资质 CRUD
GET    /api/v1/sqm/supplier-certs/expiring → 即将到期资质
GET    /api/v1/sqm/performance            → 绩效评分
POST   /api/v1/sqm/performance/calc       → 重新计算绩效
GET    /api/v1/sqm/performance/ranking    → 排名
GET    /api/v1/sqm/performance/audit-freq → 审核频次推荐
GET    /api/v1/sqm/grade-rules            → 等级规则
GET    /api/v1/sqm/shares                 → 份额管理

# 来料异常
GET    /api/v1/sqm/abnormals              → 列表
POST   /api/v1/sqm/abnormals              → 创建
POST   /api/v1/sqm/abnormals/{id}/close   → 关闭 (需关联8D已闭环)
PUT    /api/v1/sqm/abnormals/{id}/rectification → 整改方案
POST   /api/v1/sqm/abnormals/check-escalation  → 批量升级检查

# 供应商审核
GET    /api/v1/sqm/audits/plans           → 审核计划 CRUD
POST   /api/v1/sqm/audits/plans           → 创建计划
PUT    /api/v1/sqm/audits/plans/{id}/confirm → 确认计划
POST   /api/v1/sqm/audits/plans/{id}/start  → 开始审核
GET    /api/v1/sqm/audits/records         → 审核记录 CRUD
GET    /api/v1/sqm/audits/records/{id}/report → 审核报告
GET    /api/v1/sqm/audits/ncs             → 不符合项
POST   /api/v1/sqm/audits/ncs             → 创建 NC
POST   /api/v1/sqm/audits/ncs/{id}/close  → 关闭 NC
GET    /api/v1/sqm/audits/records/{id}/archive → 归档报告
POST   /api/v1/sqm/audits/records/{id}/archive/generate → 生成归档报告

# 物料变更
GET    /api/v1/sqm/changes                → 变更列表
POST   /api/v1/sqm/changes                → 创建变更
POST   /api/v1/sqm/changes/{id}/submit    → 提交
POST   /api/v1/sqm/changes/{id}/approve   → 审批
POST   /api/v1/sqm/changes/{id}/close     → 关闭
POST   /api/v1/sqm/changes/{id}/rollback  → 回退
POST   /api/v1/sqm/changes/{id}/verify-sign → 签名验证
GET    /api/v1/sqm/strict-inspects        → 加严检验

# FMEA
GET    /api/v1/sqm/fmea                   → 风险列表
POST   /api/v1/sqm/fmea                   → 创建风险项
PUT    /api/v1/sqm/fmea/{id}              → 更新
POST   /api/v1/sqm/fmea/{id}/close        → 闭环 (需 evidence + 高风险 recurrenceVerified)
GET    /api/v1/sqm/fmea/{id}/tracks       → 跟踪记录
POST   /api/v1/sqm/fmea/{id}/reopen       → 重开
POST   /api/v1/sqm/fmea/scan-overdue      → 扫描超期
GET    /api/v1/sqm/fmea/types             → 类型列表
GET    /api/v1/sqm/fmea/predict           → 风险预测

# 分析
GET    /api/v1/sqm/analysis/incoming      → 来料分析
GET    /api/v1/sqm/analysis/abnormal      → 异常分析
GET    /api/v1/sqm/dashboard/incoming     → 来料看板

# 追溯
GET    /api/v1/sqm/trace/tree             → 追溯树
GET    /api/v1/sqm/trace/full-tree        → 完整追溯树
GET    /api/v1/sqm/trace/tree-recursive   → 递归追溯
POST   /api/v1/sqm/trace/nodes            → 创建节点
GET    /api/v1/sqm/trace/nodes/search     → 节点搜索
GET    /api/v1/sqm/lots                   → 来料批次 CRUD
GET    /api/v1/sqm/key-part-sns           → 关键件序列号

# 供应商升级
GET    /api/v1/sqm/escalations            → 升级列表
POST   /api/v1/sqm/escalations            → 创建升级

# 审核频次规则
GET    /api/v1/sqm/audit-freq-rules       → CRUD

# SQE 验证
GET    /api/v1/sqm/verifications          → 验证记录
GET    /api/v1/sqm/measures               → 措施管理

# 加严检验
GET    /api/v1/sqm/strict-inspects        → CRUD
```

---

## 七、Patrol — 巡检管理

### 端点

```
GET    /api/v1/patrol/routes              → 巡检路线 CRUD
POST   /api/v1/patrol/routes              → 创建路线 (含检查项)
GET    /api/v1/patrol/tasks               → 巡检任务 CRUD
POST   /api/v1/patrol/tasks               → 创建任务
POST   /api/v1/patrol/tasks/{id}/records  → 提交巡检记录
POST   /api/v1/patrol/tasks/{id}/close    → 关闭任务
GET    /api/v1/patrol/abnormals           → 巡检异常列表
POST   /api/v1/patrol/abnormals/{id}/close → 关闭异常
```

---

## 八、Archive — 归档查询

```
GET    /api/v1/archives                   → 归档列表 (分页)
GET    /api/v1/archives/expiring          → 即将到期
```

---

## 九、全局枚举值

```
检验结果:        合格 | 不合格 | 警告
审核状态:        待审核 | 已审核 | 驳回
FIA 任务状态:    待检 | 进行中 | 待复核 | 审批中 | 已完成 | 超时 | 已作废 | 已驳回
FIA 来源:        FACTORY | SUPPLIER
处置(产线):      合格放行 | 退货 | 返工 | 让步接收 | 紧急放行 | 豁免开工
处置(来料):      合格入库 | 退货 | 让步接收 | 挑选
8D 状态:         进行中 | 已闭环
CAPA 状态:       待启动 | 分析中 | 待审批 | 实施中 | 已验证 | 已关闭
异常单状态:      待整改 | 整改中 | 待验证 | 已闭环 | 已关闭
SPC 告警级别:    预警 | 报警
SPC 通知渠道:    站内弹窗 | 企业微信 | 钉钉 | 自定义Webhook
组织类型:        公司 | 工厂 | 车间 | 产线 | 工位
通用状态:        启用 | 停用
布尔标志:        是 | 否
```

---

## 十、预设账号

| 账号 | 公司 | 密码 |
|------|:---:|------|
| `admin` | 跨公司 | 123456 |
| `mz.operator` ~ `mz.admin` (8个) | 梅州 | 123456 |
| `sz.operator` ~ `sz.admin` (8个) | 深圳 | 123456 |
