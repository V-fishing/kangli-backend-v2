-- 供应商统一信用代码: MES 同步来源(qms.material_inspection)不含统一社会信用代码,
-- 此前在 MesDataSyncJob 中用 'C' + md5 伪造占位, 已在同步任务中移除生成逻辑。
-- 此处放开 NOT NULL, 真实主数据接入前留空(前端显示「—」)。
-- UNIQUE 约束保留(Postgres 允许多个 NULL, 不冲突)。幂等: 已可空时重复执行无副作用。
ALTER TABLE ops.sqm_supplier ALTER COLUMN credit_code DROP NOT NULL;

-- 幂等清理: 把历史上已写入的伪造信用代码('C' + 31 位十六进制)置空,
-- 避免旧环境(用旧同步逻辑跑过)残留假值。真实 18 位信用代码不会被匹配。
UPDATE ops.sqm_supplier
   SET credit_code = NULL
 WHERE credit_code ~ '^C[0-9a-f]{31}$';

COMMENT ON COLUMN ops.sqm_supplier.credit_code IS '统一社会信用代码; MES 同步无此值, 留空待主数据接入';
