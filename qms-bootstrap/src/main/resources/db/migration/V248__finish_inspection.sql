-- ============================================================
-- V248 完工检验放行能力(复用 ops.fia_task, 以 trigger_type='完工检验' 区分普通首件)
-- 1) fia_task 补收生产字段(可空, 存量数据不受影响)
-- 2) 完工检验单反查索引(按工单 + 触发类型)
-- 3) 完工检验按钮权限码注册 + 角色授权(跨端协同强约束 C1)
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 补收生产字段
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS model_spec      varchar(128);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS production_date date;
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS submitted_qty   numeric(18,4);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS unit            varchar(16);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS plant_code      varchar(32);
ALTER TABLE ops.fia_task ADD COLUMN IF NOT EXISTS plant_name      varchar(128);

COMMENT ON COLUMN ops.fia_task.model_spec      IS '型号规格(完工检验补收字段)';
COMMENT ON COLUMN ops.fia_task.production_date IS '生产日期(完工检验补收字段)';
COMMENT ON COLUMN ops.fia_task.submitted_qty   IS '提交数量(完工检验补收字段)';
COMMENT ON COLUMN ops.fia_task.unit            IS '单位(完工检验补收字段)';
COMMENT ON COLUMN ops.fia_task.plant_code      IS '工厂编码(完工检验补收字段)';
COMMENT ON COLUMN ops.fia_task.plant_name      IS '工厂名称(完工检验补收字段)';

-- 2) 完工检验门禁/可选首件查询索引(按工单 + 触发类型)
CREATE INDEX IF NOT EXISTS idx_fia_task_finish
    ON ops.fia_task (wo_no, trigger_type) WHERE is_deleted = false;

-- 3) 按钮权限码注册(挂 fia 首件检验菜单)
DO $$
DECLARE fia_mid uuid;
BEGIN
  SELECT id INTO fia_mid FROM ops.sys_menu WHERE menu_code = 'fia';
  IF fia_mid IS NOT NULL THEN
    INSERT INTO ops.sys_button (id, menu_id, btn_code, btn_name) VALUES
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.create',  '完工检验建单'),
      (ops.gen_uuid_v7(), fia_mid, 'fia.finish.release', '完工检验放行')
    ON CONFLICT (menu_id, btn_code) DO NOTHING;
  END IF;
END $$;

-- 4) 角色授权(sysadmin/admin 全量; 其余角色由用户在权限页自行勾选)
INSERT INTO ops.sys_role_button (id, role_id, button_id)
SELECT ops.gen_uuid_v7(), r.id, b.id
FROM ops.sys_role r CROSS JOIN ops.sys_button b
WHERE r.role_code IN ('sysadmin','admin')
  AND b.btn_code IN ('fia.finish.create','fia.finish.release')
ON CONFLICT (role_id, button_id) DO NOTHING;
