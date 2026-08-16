-- V194 计量追溯: tlm_tool_wo_bind 增加校准状态快照列(GAUGE 绑定工单时写入),
-- 供产品批次/检验记录反查该件产品当时使用的计量器具是否在合格有效期内。
ALTER TABLE ops.tlm_tool_wo_bind ADD COLUMN IF NOT EXISTS calib_no       varchar(64);
ALTER TABLE ops.tlm_tool_wo_bind ADD COLUMN IF NOT EXISTS calib_date     date;
ALTER TABLE ops.tlm_tool_wo_bind ADD COLUMN IF NOT EXISTS calib_due_date date;
