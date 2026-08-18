-- ============================================================
-- V239 权限版本号表 + 触发器(根治「绕过页面改库后权限缓存不过期」)。
-- 背景: 原权限缓存依赖 PermissionLoader 在 RoleServiceImpl.assign* 里
--       显式 evictAll(), 任何绕过页面的改库路径(手工 SQL / 迁移脚本 /
--       触发器外写入)都不会触发失效, 最多要等 30min TTL 才刷新, 期间
--       用户看到旧菜单/旧按钮, 点到未授权接口才弹 403。
-- 方案: 新增单行版本表 ops.sys_perm_version(version bigint), 对权限相关
--       三张表(sys_role_menu / sys_role_button / sys_user_role)建触发器,
--       任一 INSERT/UPDATE/DELETE 都自增版本号。PermissionLoader 读缓存前
--       先比对全局版本号, 不一致则强制重查。无论谁改库, 缓存必然失效。
-- 全部幂等, 可重复执行。
-- ============================================================

-- 1) 版本表(单行): 初始 version=1
CREATE TABLE IF NOT EXISTS ops.sys_perm_version (
  id    integer PRIMARY KEY CHECK (id = 1),
  version bigint NOT NULL DEFAULT 1
);
INSERT INTO ops.sys_perm_version (id, version) VALUES (1, 1)
ON CONFLICT (id) DO NOTHING;

-- 2) 通用 bump 函数: 把版本号 +1(行锁保证并发安全)
CREATE OR REPLACE FUNCTION ops.bump_perm_version()
RETURNS trigger AS $$
BEGIN
  UPDATE ops.sys_perm_version SET version = version + 1 WHERE id = 1;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 3) 触发器: 权限相关三表任一写操作都 bump 版本号
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_perm_role_menu'
  ) THEN
    CREATE TRIGGER trg_perm_role_menu
      AFTER INSERT OR UPDATE OR DELETE ON ops.sys_role_menu
      FOR EACH STATEMENT EXECUTE FUNCTION ops.bump_perm_version();
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_perm_role_button'
  ) THEN
    CREATE TRIGGER trg_perm_role_button
      AFTER INSERT OR UPDATE OR DELETE ON ops.sys_role_button
      FOR EACH STATEMENT EXECUTE FUNCTION ops.bump_perm_version();
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_perm_user_role'
  ) THEN
    CREATE TRIGGER trg_perm_user_role
      AFTER INSERT OR UPDATE OR DELETE ON ops.sys_user_role
      FOR EACH STATEMENT EXECUTE FUNCTION ops.bump_perm_version();
  END IF;
END $$;
