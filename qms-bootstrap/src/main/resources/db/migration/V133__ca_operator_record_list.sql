-- V133: 补齐纠正措施页所需查询/写权限给 operator（解决页面初始化 403）
--
-- 背景：纠正措施页(CorrectiveActionList.vue)初始化时会调用 /ncm/defect-records(list，需 ncm.record.list)
-- 拉取"关联不良单"下拉；V131 仅给 operator 授权了 ncm.corrective-actions 菜单(派生 ncm.ca.list)，
-- 未含 ncm.record.list，导致页面初始化请求 403、弹"无权限"。
-- 同时 operator 也缺少 ncm.ca.create（无法真正新建纠正措施），与"给了 CA 权限应能操作"的预期不符。
--
-- 本迁移：
--   ① 把查询按钮码 ncm.record.list 授权给 operator + sysadmin（纯查询，无权限放大）；
--   ② 把写按钮码 ncm.ca.create 授权给 operator（sysadmin 已由 V131 覆盖，ON CONFLICT 幂等跳过）。
-- 全部幂等。

DO $$
BEGIN
  INSERT INTO ops.sys_role_button (id, role_id, button_id)
  SELECT ops.gen_uuid_v7(), r.id, b.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_button b
  WHERE r.role_code IN ('operator', 'sysadmin')
    AND b.btn_code = 'ncm.record.list'
  ON CONFLICT (role_id, button_id) DO NOTHING;

  INSERT INTO ops.sys_role_button (id, role_id, button_id)
  SELECT ops.gen_uuid_v7(), r.id, b.id
  FROM ops.sys_role r
  CROSS JOIN ops.sys_button b
  WHERE r.role_code = 'operator'
    AND b.btn_code = 'ncm.ca.create'
  ON CONFLICT (role_id, button_id) DO NOTHING;
END $$;
