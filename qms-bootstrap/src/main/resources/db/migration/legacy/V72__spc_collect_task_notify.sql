-- SPC 采集任务:指定接收人 + 临期提醒去重标记
ALTER TABLE ops.spc_collect_task
  ADD COLUMN collector          VARCHAR(64),
  ADD COLUMN due_reminded      BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN ops.spc_collect_task.collector IS '采集任务指定接收人(通知接收者);为空时回退"班组长"';
COMMENT ON COLUMN ops.spc_collect_task.due_reminded IS '临期提醒是否已发送,避免重复提醒;每次顺延/创建时复位为 false';
