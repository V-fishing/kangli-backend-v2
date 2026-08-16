-- 清洗 SPC 参数历史脏数据:计量型 + 计数型混选(应被 deriveDataType 禁止,但老数据残留)
-- 仅针对 chart_candidates 同时含计量图(Xbar/R/S/I/MR)与计数图(P/NP/C/U)、且被错标为 ATTRIBUTE 的参数。
-- 这些参数的实际子组均为计量数据(xbar 非空、计数字段全空),故收敛为纯计量 Xbar,R,data_type 改回 VARIABLE。
-- 不影响已有 25 条计量子组;chart_type 本就是 Xbar,保持不变。
-- 幂等:已清洗(data_type='VARIABLE' 且 chart_candidates 不含计数码)的行 UPDATE 0 行,可重复执行。

UPDATE ops.spc_param
SET chart_candidates = 'Xbar,R',
    data_type = 'VARIABLE',
    updated_at = NOW()
WHERE data_type = 'ATTRIBUTE'
  AND chart_candidates ~* '(Xbar|R|S|I|MR)'
  AND chart_candidates ~* '(P|NP|C|U)'
  AND (chart_candidates IS DISTINCT FROM 'Xbar,R' OR data_type IS DISTINCT FROM 'VARIABLE');
