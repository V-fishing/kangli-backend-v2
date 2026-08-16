-- V71: SPC 采集任务增加 collect_mode(采集模式) 列 + 菜单树新增"采集任务管理"页签
-- 列可空性: NOT NULL DEFAULT 'MANUAL',既有行与种子旧 INSERT 自动取默认,零停机、幂等。

ALTER TABLE ops.spc_collect_task
  ADD COLUMN IF NOT EXISTS collect_mode VARCHAR(16) NOT NULL DEFAULT 'MANUAL';
COMMENT ON COLUMN ops.spc_collect_task.collect_mode IS '采集模式: MANUAL(手动录入)/OPC(设备直连)/FILE(文件导入)/MES(MES对接)/AUTO(定时自动采集)';

-- 菜单: 在 SPC 父节点下新增"采集任务管理"(绝对 path,与 V69 范式一致)
DO $$
DECLARE spc_id uuid;
BEGIN
  SELECT id INTO spc_id FROM ops.sys_menu WHERE menu_code = 'spc';
  INSERT INTO ops.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, icon, sort_order, visible)
  SELECT ops.gen_uuid_v7(), spc_id, 'spc.collecttasks', '采集任务管理', '菜单', '/spc/collect-tasks', 'spc/CollectTask', '🗓️', 3, true
  WHERE spc_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM ops.sys_menu WHERE menu_code = 'spc.collecttasks');
END $$;
