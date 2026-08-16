-- 过程能力指数补充 Cp/Pp(过程潜力指数)及计算诊断 calc_note。
-- 原 calc 只落 cpk/ppk,前端无法区分"为何没有数据"(缺规格限/样本不足/无变异)。
ALTER TABLE ops.spc_capability
  ADD COLUMN IF NOT EXISTS cp NUMERIC(10,3),
  ADD COLUMN IF NOT EXISTS pp NUMERIC(10,3),
  ADD COLUMN IF NOT EXISTS calc_note VARCHAR(255);
COMMENT ON COLUMN ops.spc_capability.cp IS '过程潜力指数 Cp = (USL-LSL)/(6σ组内)';
COMMENT ON COLUMN ops.spc_capability.pp IS '过程性能指数 Pp = (USL-LSL)/(6σ整体)';
COMMENT ON COLUMN ops.spc_capability.calc_note IS '能力无法计算的原因诊断(如未配置规格限/样本不足/数据无变异)';
