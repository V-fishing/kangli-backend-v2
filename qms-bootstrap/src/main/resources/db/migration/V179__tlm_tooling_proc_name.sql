-- V179: 工装挂接标准库前置 —— 加工序名称字段
-- 工装首件/SPC 联动需要通过 (product_code + proc_name) 匹配 FIA 标准库(fia_insp_std) 和 SPC 标准线(spc_spec_standard)
-- proc_name 与 process_id 同源，使用文本匹配防 ID 漂移
ALTER TABLE ops.tlm_tooling ADD COLUMN IF NOT EXISTS proc_name VARCHAR(128);
COMMENT ON COLUMN ops.tlm_tooling.proc_name IS '工序名称(与 process_id 同源，用于 FIA/SPC 标准匹配)';