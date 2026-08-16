-- V64 分公司级角色隔离：给 ops.sys_role 增加 org_id，使梅州(MZ)/深圳(SZ) 各自拥有独立角色副本，
-- role_code 可跨公司重复、权限可分别配置；sysadmin(org_id=null) 仍全局唯一作为超管。
-- 同时把旧全局功能角色转换为「MZ 副本 + SZ 副本」，并重建 system.* 菜单/按钮授权。
-- 全部幂等，可重复执行。

-- ============ 1) 结构变更：加列 + 部分唯一索引 ============
ALTER TABLE ops.sys_role ADD COLUMN IF NOT EXISTS org_id UUID REFERENCES ops.sys_org(id);
CREATE INDEX IF NOT EXISTS idx_sys_role_org ON ops.sys_role(org_id);

-- 旧全局唯一约束（role_code 唯一）改为两条部分唯一索引：
--   全局角色(org_id IS NULL) 按 role_code 唯一（保证仅一个 sysadmin）
--   分公司角色(org_id NOT NULL) 按 (role_code, org_id) 唯一
ALTER TABLE ops.sys_role DROP CONSTRAINT IF EXISTS sys_role_role_code_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_sys_role_code_global
  ON ops.sys_role(role_code) WHERE org_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_sys_role_code_org
  ON ops.sys_role(role_code, org_id) WHERE org_id IS NOT NULL;

-- ============ 2) 数据迁移：确保组织存在 → 删旧全局功能角色 → 重灌每分公司副本 → 重建授权 ============
DO $$
DECLARE
  mz_id UUID;
  sz_id UUID;
  r     RECORD;
  new_id UUID;
BEGIN
  -- 2.1 确保 MZ/SZ 组织存在（Flyway 早于 SeedRunner 执行，全新库此时组织尚未建；幂等）
  INSERT INTO ops.sys_org (id, org_code, org_name, sort_order, org_type, status)
    SELECT ops.gen_uuid_v7(), 'MZ', '梅州分公司', 1, '公司', '启用'
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_org WHERE org_code = 'MZ');
  INSERT INTO ops.sys_org (id, org_code, org_name, sort_order, org_type, status)
    SELECT ops.gen_uuid_v7(), 'SZ', '深圳分公司', 2, '公司', '启用'
    WHERE NOT EXISTS (SELECT 1 FROM ops.sys_org WHERE org_code = 'SZ');

  SELECT id INTO mz_id FROM ops.sys_org WHERE org_code = 'MZ';
  SELECT id INTO sz_id FROM ops.sys_org WHERE org_code = 'SZ';

  -- 2.2 先清子表，再删旧全局功能角色（保留 sysadmin: org_id IS NULL 且 role_code='sysadmin'）
  --     外键无 ON DELETE CASCADE，须手动按依赖顺序清理
  DELETE FROM ops.sys_role_menu   rm
    WHERE rm.role_id IN (SELECT id FROM ops.sys_role WHERE org_id IS NULL AND role_code <> 'sysadmin');
  DELETE FROM ops.sys_role_button rb
    WHERE rb.role_id IN (SELECT id FROM ops.sys_role WHERE org_id IS NULL AND role_code <> 'sysadmin');
  DELETE FROM ops.sys_user_role  ur
    WHERE ur.role_id IN (SELECT id FROM ops.sys_role WHERE org_id IS NULL AND role_code <> 'sysadmin');
  DELETE FROM ops.sys_data_scope ds
    WHERE ds.role_id IN (SELECT id FROM ops.sys_role WHERE org_id IS NULL AND role_code <> 'sysadmin');
  DELETE FROM ops.sys_role
    WHERE org_id IS NULL AND role_code <> 'sysadmin';

  -- 2.3 确保 sysadmin 全局超管存在（org_id=null）；其余功能角色改为每分公司一份
  IF NOT EXISTS (SELECT 1 FROM ops.sys_role WHERE role_code = 'sysadmin') THEN
    INSERT INTO ops.sys_role (id, role_code, role_name, role_type, perm_desc, status, is_deleted, version)
      VALUES (ops.gen_uuid_v7(), 'sysadmin', '系统管理员', '预置', '全部权限', '启用', false, 1);
  END IF;

  -- 2.4 按 MZ/SZ 重灌 9 个功能角色副本（幂等：以 (role_code, org_id) 守卫）
  FOR r IN SELECT * FROM (VALUES
      ('operator',    '操作工',      '产线操作 · 自检数据录入'),
      ('inspector',   '生产检验员',  '首件/来料检验 · 不良录入 · 器具使用'),
      ('shiftleader', '班组长',      '产线管理 · 报警确认关闭 · 工装状态'),
      ('qe',          '质量工程师',  '质量分析 · SPC · 8D整改 · CAPA'),
      ('sqe',         'SQE',         '供应商审核 · 来料异常处置 · 整改验证'),
      ('qmanager',    '质量经理',    '审批授权 · 趋势分析 · 绩效评审'),
      ('purchaser',   '采购员',      '采购订单 · 供应商准入 · 物料变更'),
      ('rd',          '研发工程师',  '物料变更研发审批 · 工艺/验证评估'),
      ('admin',       '管理员',      '公司内用户/角色管理 · 基础数据维护')
    ) AS d(role_code, role_name, perm_desc)
  LOOP
    -- MZ 副本
    IF NOT EXISTS (SELECT 1 FROM ops.sys_role WHERE role_code = r.role_code AND org_id = mz_id) THEN
      INSERT INTO ops.sys_role (id, role_code, role_name, role_type, perm_desc, org_id, status, is_deleted, version)
        VALUES (ops.gen_uuid_v7(), r.role_code, r.role_name, '预置', r.perm_desc, mz_id, '启用', false, 1);
    END IF;
    -- SZ 副本
    IF NOT EXISTS (SELECT 1 FROM ops.sys_role WHERE role_code = r.role_code AND org_id = sz_id) THEN
      INSERT INTO ops.sys_role (id, role_code, role_name, role_type, perm_desc, org_id, status, is_deleted, version)
        VALUES (ops.gen_uuid_v7(), r.role_code, r.role_name, '预置', r.perm_desc, sz_id, '启用', false, 1);
    END IF;
  END LOOP;

  -- 2.5 重建 system.* 授权（原授权随旧角色删除而丢失，此处按每分公司副本重新授予）
  -- 全量（org/菜单/角色/用户 的菜单 + 写按钮）给每分公司 admin/rd/qmanager
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
    SELECT ops.gen_uuid_v7(), rr.id, mm.id
    FROM ops.sys_role rr CROSS JOIN ops.sys_menu mm
    WHERE rr.org_id IN (mz_id, sz_id)
      AND rr.role_code IN ('admin', 'rd', 'qmanager')
      AND mm.menu_code IN ('system.org.list', 'system.menu.list', 'system.role.list', 'system.user.list')
    ON CONFLICT (role_id, menu_id) DO NOTHING;

  INSERT INTO ops.sys_role_button (id, role_id, button_id)
    SELECT ops.gen_uuid_v7(), rr.id, bb.id
    FROM ops.sys_role rr CROSS JOIN ops.sys_button bb
    WHERE rr.org_id IN (mz_id, sz_id)
      AND rr.role_code IN ('admin', 'rd', 'qmanager')
      AND bb.btn_code IN (
        'system.org.create', 'system.org.delete',
        'system.menu.create', 'system.menu.delete',
        'system.role.create', 'system.role.delete', 'system.role.assign',
        'system.user.create', 'system.user.delete', 'system.role.assign')
    ON CONFLICT (role_id, button_id) DO NOTHING;

  -- operator 仅用户管理（菜单 system.user.list + 按钮 user.list/create/delete）
  INSERT INTO ops.sys_role_menu (id, role_id, menu_id)
    SELECT ops.gen_uuid_v7(), rr.id, mm.id
    FROM ops.sys_role rr CROSS JOIN ops.sys_menu mm
    WHERE rr.org_id IN (mz_id, sz_id) AND rr.role_code = 'operator'
      AND mm.menu_code = 'system.user.list'
    ON CONFLICT (role_id, menu_id) DO NOTHING;

  INSERT INTO ops.sys_role_button (id, role_id, button_id)
    SELECT ops.gen_uuid_v7(), rr.id, bb.id
    FROM ops.sys_role rr CROSS JOIN ops.sys_button bb
    WHERE rr.org_id IN (mz_id, sz_id) AND rr.role_code = 'operator'
      AND bb.btn_code IN ('system.user.list', 'system.user.create', 'system.user.delete')
    ON CONFLICT (role_id, button_id) DO NOTHING;

  -- 注意：SQM/FIA/SPC/NCM 等业务模块的菜单/按钮授权由 DataInitializer（按组织循环）在启动时授予，
  --      此处只保证「系统管理」页本身可用；sysadmin 的既有授权未被删除，保持原样。
END $$;
