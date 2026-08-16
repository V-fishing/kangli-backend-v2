-- 工装维修根因分析: 为维修工单增加结构化故障类型(fault_type), 替代纯自由文本,
-- 以支持按故障类型做柏拉图/占比聚合(需求 2.5.2.6 工装维修根因报表)。
-- 枚举: 磨损/变形/断裂/精度超差/电气故障/其他
ALTER TABLE ops.tlm_repair ADD COLUMN IF NOT EXISTS fault_type varchar(32);
UPDATE ops.tlm_repair SET fault_type = '其他' WHERE fault_type IS NULL;
COMMENT ON COLUMN ops.tlm_repair.fault_type IS '故障类型: 磨损/变形/断裂/精度超差/电气故障/其他';
