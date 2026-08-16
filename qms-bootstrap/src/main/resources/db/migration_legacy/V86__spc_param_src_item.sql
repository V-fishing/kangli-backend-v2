-- 来源 FIA 任务检验项:由「去SPC采集」一键生成 SPC 参数时写入,用于去重(同一检验项只生成一次)
ALTER TABLE ops.spc_param ADD COLUMN IF NOT EXISTS src_item_id UUID REFERENCES ops.fia_insp_item(id);
COMMENT ON COLUMN ops.spc_param.src_item_id IS '来源FIA任务检验项ID(可空);由FIA任务一键生成SPC参数时写入,用于去重';

CREATE INDEX IF NOT EXISTS idx_spc_param_src_item ON ops.spc_param(src_item_id);
-- 同一来源检验项只允许对应一个 SPC 参数,保证重复点击「去SPC采集」不会重复建参
CREATE UNIQUE INDEX IF NOT EXISTS uq_spc_param_src_item ON ops.spc_param(src_item_id) WHERE src_item_id IS NOT NULL;
