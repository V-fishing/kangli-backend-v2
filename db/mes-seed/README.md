# MES 源数据种子 (mes-seed)

QMS 的来料检验 / 完工检验 / 物料绑定数据来自 MES 宽表（`qms` schema）。
本目录提供一份 **2026-08-07** 的快照（数据截至 2026-08-06），供**无 MES 环境**
（演示 / 独立部署）也能展示完整数据：启动后端后，`MesDataSyncJob` 会从 `qms.*`
自动同步生成 `ops.sqm_*` 的供应商、来料异常、来料批次等数据。

> 若生产环境已接入真实 MES（实时灌入 `qms.*`），**无需加载本种子**，以 MES 实时数据为准。

## 文件清单

| 文件 | 说明 |
|---|---|
| `mes_schema.sql` | `qms` schema 及表结构（须最先执行，建表） |
| `material_inspection_202608071045.sql.gz` | 来料检验（驱动供应商 / 来料异常 / 来料批次同步） |
| `finished_goods_inspection_202608071045.sql.gz` | 完工检验 |
| `critical_material_binding_202608071045.sql.gz` | 物料绑定（全链路追溯） |

数据文件已 gzip 压缩（`*.sql.gz`），加载脚本会自动解压灌入，无需手动解压。

## 对方拉取代码后如何使用

前提：PostgreSQL 已就绪；建议在**空库 / 未启动后端前**执行。

### Windows (PowerShell)
```powershell
$env:QMS_DB_PASSWORD = '你的密码'        # 也可在脚本参数里传
.\load_mes_seed.ps1                       # 默认连 localhost:5432/qms 用户 qms
# 或清空已有 qms 种子表后重载：
.\load_mes_seed.ps1 -Reset
```
> Windows 解压依赖 Python（仓库环境已具备）；Linux/macOS 用系统自带 gzip。

### Linux / macOS
```bash
QMS_DB_PASSWORD='你的密码' ./load_mes_seed.sh
# 或：QMS_DB_PASSWORD='...' ./load_mes_seed.sh --reset
```

可用环境变量覆盖连接：`QMS_DB_HOST` / `QMS_DB_PORT` / `QMS_DB_NAME` / `QMS_DB_USER`
（Windows 对应脚本参数 `-HostName` 等）。

### 加载顺序
1. `mes_schema.sql` —— 创建 `qms` schema 与表。
2. 三份 `*_202608071045.sql.gz` —— 解压后以 `COPY` 灌入数据。

### 随后启动后端
启动应用 → Flyway 执行迁移 → `MesDataSyncJob` 在 `ApplicationReadyEvent` 时从 `qms.*`
同步生成 `ops` 业务数据。前端即可看到完整的供应商、来料异常、完工检验等。

## 备注
- 本快照约 66k 来料行 / 67k 完工行 / 297k 绑定行；gzip 后合计约 12MB。
- 重新生成最新快照（gzip）：
  `pg_dump -h <host> -U <user> -d <db> -a -t qms.material_inspection -t qms.finished_goods_inspection -t qms.critical_material_binding | gzip > mes_data_$(date +%Y%m%d%H%M).sql.gz`
