# 数据库迁移脚本重组说明（收尾基线 · 按板块拆分）

## 背景

原 `db/migration/` 下累积 **215 个增量 Flyway 脚本（V01–V230）**，含大量中途补丁：
- 破坏性脚本（`V94` TRUNCATE 追溯数据、`V96` DELETE 旧来料后全量重插、`V140` 清掉重灌）
- 依赖外部手工步骤的脚本（`V90` 依赖 `qms.material_inspection` 等源表由 psql 手工导入）
- 0 字节占位文件（`V115`–`V119`）、小数版本（`V100.1`）

直接"完整执行 Flyway"在干净环境不可靠。故改为**从当前已跑通的数据库反向导出「结构 + 种子」基线**，并按业务板块拆分为多个版本号文件，便于阅读与维护。

## 当前结构

```
db/migration/
├── README.md                      # 本说明
│
├── 【结构 · 地基】
│   └── V001__baseline_foundation.sql   # 扩展(ltree/pgcrypto/pg_trgm) + CREATE SCHEMA + 公共函数(gen_uuid_v7 等)
│
├── 【结构 · 按板块拆分】  (每张表 pg_dump 逐表导出, 板块内已按下依赖排序, 外键统一后置)
│   ├── V002__schema_sys.sql       # 系统/权限/基础: sys_* / role_module_* / 审计/附件/字典/配置
│   ├── V003__schema_fia.sql       # FIA 检验
│   ├── V004__schema_spc.sql       # SPC 统计过程
│   ├── V005__schema_ncm.sql       # NCM 不合格/8D
│   ├── V006__schema_sqm_core.sql  # SQM 核心: 来料/供应商/物料/关键件
│   ├── V007__schema_sqm_abn.sql   # SQM 异常与整改
│   ├── V008__schema_sqm_audit.sql # SQM 审核
│   ├── V009__schema_sqm_change.sql# SQM 变更
│   ├── V010__schema_sqm_trace.sql # SQM 物料追溯
│   ├── V011__schema_sqm_perf.sql  # SQM 供应商绩效
│   ├── V012__schema_qms.sql       # 体系/8D/CAPA/FMEA
│   ├── V013__schema_patl.sql      # 巡检
│   ├── V014__schema_tlm.sql       # 工装/计量
│   ├── V015__schema_cs.sql        # 售后
│   ├── V016__schema_notify.sql   # 通知
│   └── V017__schema_constraints.sql # 全部外键约束(ALTER TABLE ADD CONSTRAINT FOREIGN KEY), 在建表后统一执行
│
├── 【种子 · 按类别拆分】
│   ├── V018__seed_system.sql      # 系统种子: 组织/用户/角色/菜单/权限/字典/配置
│   ├── V019__seed_dict.sql        # 业务字典: 缺陷字典/FIA触发类型/检验标准(7239行)
│   ├── V020__seed_notify.sql      # 通知配置: notify_config/notify_channel/spc_notify_channel
│   ├── V021__seed_config.sql      # 模块配置与规则: 8D/SQM/SPC/工装 各配置表
│   ├── V022__seed_demo_cs.sql     # [可选] 演示-售后工单/反馈
│   ├── V023__seed_demo_qms.sql    # [可选] 演示-体系内审/不良事件/质量目标
│   └── V024__seed_demo_tlm.sql    # [可选] 演示-工装/计量台账
│
└── legacy/                        # 【暂存】215 个旧增量脚本(Flyway 不扫描, 仅留档回溯)
```

## 文件约定

- **结构文件**（V001~V017）：仅含 DDL。V001 地基（扩展+函数），V002~V016 按板块建表，V017 统一追加外键。
  每个表由 `pg_dump -t` 单独导出（自包含 CREATE TABLE + 索引 + 非外键约束），板块内按依赖拓扑排序，跨板块外键提取到 V017 最后执行，彻底规避 FK 顺序问题。
- **种子文件**（V018~V024）：按业务类别拆分，文件头标注内容/性质/重跑方式。
  开头 `SET session_replication_role = replica;` 禁用 FK 即时检查（解决 sys_menu 等自引用 INSERT 顺序），避免重复执行报错。
- **演示数据**（V022~V024）标注 `[可选, 可关闭]`，生产交付时可不执行。

## 使用方式

### 1. 全新环境（甲方纯净库）
1. 建空库 `qms`，schema `ops` / 扩展 / 函数由 V001 自动创建。
2. 启动后端，Flyway 依次执行 V001→V024，结构 + 种子一次成型。
3. SZ-MES 业务数据（`sqm_incoming_lot` / `sqm_trace_relation` 等）不在此基线内，
   由导入逻辑（`legacy/V90`+`V96` 范式）或独立 ETL 生成。
4. `application.yml` 的 `flyway.locations` 保持 `classpath:db/migration` 即可（不递归子目录，legacy/ 不会被扫）。

### 2. 本地已跑通库切换到新基线
当前库已有 `flyway_schema_history` 记录旧 V01–V230，直接启动会因版本号重复冲突。需：
```sql
TRUNCATE ops.flyway_schema_history;   -- 备份后执行, 让 Flyway 以新基线重新 baseline
-- 然后启动后端 (baseline-on-migrate=true) 以 V001 重新登记
```
或直接 `flyway repair` 后重启。

## 注意事项

- 所有种子为**普通 INSERT**，重跑需先清空对应表（或改为 `ON CONFLICT DO NOTHING`）。
- 分区表 `spc_subgroup` / `spc_subgroup_default` 的索引 ATTACH 已简化处理，功能不受影响。
- `V001` 含 `DEFAULT ops.gen_uuid_v7()` 及 ltree 等扩展，已在反向导出 DDL 中。
- `legacy/` 仅留档，**不参与任何环境迁移**；如需查看某补丁原始逻辑从此处取。
- 已验证：全新库执行 V001~V024 可完整建出 151 张表并灌入种子数据（sys_user=22 / sys_menu=79）。
