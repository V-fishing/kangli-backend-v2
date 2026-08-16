-- SPC 参数可配置项:CPK 标准差算法 / σ 倍数 / CPK 自动更新周期。
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS sigma_method VARCHAR(16); -- within(组内) / overall(整体)
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS sigma_k NUMERIC(4,2);      -- 控制限/能力倍数,默认 3
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS cpk_period VARCHAR(8);     -- 批次 / 日 / 周
COMMENT ON COLUMN ops.spc_param.sigma_method IS 'CPK 标准差算法:within/overall';
COMMENT ON COLUMN ops.spc_param.sigma_k IS '控制限/能力指数 σ 倍数,默认 3';
COMMENT ON COLUMN ops.spc_param.cpk_period IS 'CPK 自动滚动更新周期:批次/日/周';

-- SPC 控制限人工覆盖标记。
ALTER TABLE ops.spc_control_limit ADD COLUMN IF NOT EXISTS manual BOOLEAN;
COMMENT ON COLUMN ops.spc_control_limit.manual IS 'true=人工覆盖,优先于自动计算';
