package com.konli.qms.bootstrap;

import com.konli.qms.common.security.PermissionLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 开发环境种子数据(幂等):公司 + 用户 + 角色/菜单/按钮/权限分配 + 额外权限按钮。
 *
 * <p>启动时先 {@link PermissionLoader#evictAll()} 清权限缓存(确保迁移/种子变更后权限新鲜)。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PermissionLoader permissionLoader;

    @Override
    public void run(String... args) {
        permissionLoader.evictAll();
        seedOrgsAndUsers();
        seedRbac();
        seedExtraPerms();
        seedFiaPerms();
        seedSpcPerms();
        seedNcmPerms();
        seedSqmPerms();
        seedPatlPerms();
    }

    private void seedOrgsAndUsers() {
        if (count("ops.sys_org") == 0) {
            jdbcTemplate.update("INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status) VALUES (?, ?, ?, '公司', '启用')", "MZ", "梅州分公司", 1);
            jdbcTemplate.update("INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status) VALUES (?, ?, ?, '公司', '启用')", "SZ", "深圳分公司", 2);
            log.info("[SEED] 已预置公司:梅州/深圳");
        }
        if (!userExists("admin")) {
            insertUser("admin", passwordEncoder.encode("admin123"), "系统管理员", null);
            log.info("[SEED] 已预置管理员 admin/admin123(跨公司,dataScope=all)");
        }
        String mzId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (mzId != null && !userExists("mzuser")) {
            insertUser("mzuser", passwordEncoder.encode("user123"), "梅州操作员", mzId);
            log.info("[SEED] 已预置普通用户 mzuser/user123(仅梅州)");
        }
    }

    private void seedRbac() {
        if (count("ops.sys_role") > 0) {
            return;
        }
        String sysadminRoleId = ensureRole("sysadmin", "系统管理员", "预置", "全部权限");
        String operatorRoleId = ensureRole("operator", "操作员", "预置", "仅查看用户");

        String userMenu = ensureMenu("system.user", "用户管理", "/system/user", "system/user/index", 1);
        String roleMenu = ensureMenu("system.role", "角色管理", "/system/role", "system/role/index", 2);
        String menuMenu = ensureMenu("system.menu", "菜单管理", "/system/menu", "system/menu/index", 3);
        String orgMenu  = ensureMenu("system.org",  "组织管理", "/system/org",  "system/org/index",  4);
        List<String> allMenus = List.of(userMenu, roleMenu, menuMenu, orgMenu);

        String btnUserList   = ensureButton(userMenu, "system.user.list",   "查询");
        String btnUserDelete = ensureButton(userMenu, "system.user.delete", "删除");
        String btnRoleList   = ensureButton(roleMenu, "system.role.list",   "查询");
        String btnRoleAssign = ensureButton(roleMenu, "system.role.assign", "分配权限");
        String btnMenuList   = ensureButton(menuMenu, "system.menu.list",   "查询");
        String btnMenuCreate = ensureButton(menuMenu, "system.menu.create", "新增");
        String btnOrgList    = ensureButton(orgMenu,  "system.org.list",    "查询");
        List<String> allButtons = List.of(btnUserList, btnUserDelete, btnRoleList, btnRoleAssign, btnMenuList, btnMenuCreate, btnOrgList);

        assignRoleMenus(sysadminRoleId, allMenus);
        assignRoleButtons(sysadminRoleId, allButtons);
        assignRoleMenus(operatorRoleId, List.of(userMenu));
        assignRoleButtons(operatorRoleId, List.of(btnUserList));
        assignUserRole("admin", sysadminRoleId);
        assignUserRole("mzuser", operatorRoleId);
        log.info("[SEED] 已预置角色/菜单/按钮/权限分配(sysadmin 全量, operator 仅查看用户)");
    }

    /** 额外权限按钮(用户写入/代班),幂等补,分配给 sysadmin。避开 flyway 时序(菜单在种子阶段才建)。 */
    private void seedExtraPerms() {
        String userMenuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code='system.user'");
        if (userMenuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(userMenuId, "system.user.create", "新增/编辑/重置密码"));
        }
        String roleMenuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code='system.role'");
        if (roleMenuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(roleMenuId, "system.delegation.manage", "代班管理"));
        }
    }

    /** FIA 权限种子(fia 菜单 + 标准/任务按钮,分配 sysadmin)。幂等。 */
    private void seedFiaPerms() {
        String fiaMenu = ensureMenu("fia", "首件检验", "/fia", "fia/index", 5);
        assignRoleMenuByCode("sysadmin", fiaMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(fiaMenu, "fia.std.list", "标准查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(fiaMenu, "fia.std.create", "标准管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(fiaMenu, "fia.task.list", "任务查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(fiaMenu, "fia.task.create", "任务录入"));
        assignRoleButtonByCode("sysadmin", ensureButton(fiaMenu, "fia.task.submit", "签名提交"));
    }

    /** SPC 权限种子(spc 菜单 + 参数/子组/告警/能力/规则按钮,分配 sysadmin)。幂等。 */
    private void seedSpcPerms() {
        String spcMenu = ensureMenu("spc", "SPC过程能力", "/spc", "spc/index", 6);
        assignRoleMenuByCode("sysadmin", spcMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.param.list", "参数查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.param.create", "参数管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.subgroup.list", "子组查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.subgroup.create", "子组录入"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.alarm.list", "告警查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.alarm.close", "告警关闭"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.capability.list", "能力分析"));
        assignRoleButtonByCode("sysadmin", ensureButton(spcMenu, "spc.rule.list", "判异规则"));
    }

    /** NCM 权限种子(ncm 菜单 + 不良字典/记录/8D/CAPA 按钮,分配 sysadmin)。幂等。 */
    private void seedNcmPerms() {
        String ncmMenu = ensureMenu("ncm", "不良管理", "/ncm", "ncm/index", 7);
        assignRoleMenuByCode("sysadmin", ncmMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.defect.list", "字典查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.defect.create", "字典管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.record.list", "记录查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.record.create", "记录录入"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.list", "8D查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.create", "8D管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.capa.list", "CAPA查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.capa.create", "CAPA管理"));
    }

    /** SQM 权限种子(sqm 菜单 + 供应商/审核/变更/追溯/异常按钮,分配 sysadmin)。幂等。 */
    private void seedSqmPerms() {
        String sqmMenu = ensureMenu("sqm", "供应商质量", "/sqm", "sqm/index", 8);
        assignRoleMenuByCode("sysadmin", sqmMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.supplier.list", "供应商查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.supplier.create", "供应商管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.audit.list", "审核查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.audit.create", "审核管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.change.list", "变更查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.change.create", "变更管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.trace.list", "追溯查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.trace.create", "追溯录入"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.abnormal.list", "异常查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.abnormal.create", "异常管理"));
    }

    /** PATL 权限种子(patrol 菜单 + 路线/任务按钮,分配 sysadmin)。幂等。 */
    private void seedPatlPerms() {
        String patlMenu = ensureMenu("patrol", "巡检管理", "/patrol", "patrol/index", 9);
        assignRoleMenuByCode("sysadmin", patlMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(patlMenu, "patl.route.list", "路线查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(patlMenu, "patl.route.create", "路线管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(patlMenu, "patl.task.list", "任务查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(patlMenu, "patl.task.create", "任务管理"));
    }

    // ---- helpers ----
    private String ensureRole(String code, String name, String type, String desc) {
        String id = queryId("SELECT id FROM ops.sys_role WHERE role_code=?", code);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_role (role_code, role_name, role_type, perm_desc, status) VALUES (?, ?, ?, ?, '启用')", code, name, type, desc);
            id = queryId("SELECT id FROM ops.sys_role WHERE role_code=?", code);
        }
        return id;
    }

    private String ensureMenu(String code, String name, String path, String component, int sort) {
        String id = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", code);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_menu (menu_code, menu_name, menu_type, path, component, sort_order, visible) VALUES (?, ?, '菜单', ?, ?, ?, true)", code, name, path, component, sort);
            id = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", code);
        }
        return id;
    }

    private String ensureButton(String menuId, String code, String name) {
        String id = queryId("SELECT id FROM ops.sys_button WHERE btn_code=?", code);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_button (menu_id, btn_code, btn_name) VALUES (?::uuid, ?, ?)", menuId, code, name);
            id = queryId("SELECT id FROM ops.sys_button WHERE btn_code=?", code);
        }
        return id;
    }

    private void assignRoleMenus(String roleId, List<String> menuIds) {
        for (String mid : menuIds) {
            if (linkMissing("ops.sys_role_menu", "role_id", roleId, "menu_id", mid)) {
                jdbcTemplate.update("INSERT INTO ops.sys_role_menu (role_id, menu_id) VALUES (?::uuid, ?::uuid)", roleId, mid);
            }
        }
    }

    private void assignRoleButtons(String roleId, List<String> buttonIds) {
        for (String bid : buttonIds) {
            if (linkMissing("ops.sys_role_button", "role_id", roleId, "button_id", bid)) {
                jdbcTemplate.update("INSERT INTO ops.sys_role_button (role_id, button_id) VALUES (?::uuid, ?::uuid)", roleId, bid);
            }
        }
    }

    private void assignRoleButtonByCode(String roleCode, String buttonId) {
        String roleId = queryId("SELECT id FROM ops.sys_role WHERE role_code=?", roleCode);
        if (roleId == null || buttonId == null) {
            return;
        }
        if (linkMissing("ops.sys_role_button", "role_id", roleId, "button_id", buttonId)) {
            jdbcTemplate.update("INSERT INTO ops.sys_role_button (role_id, button_id) VALUES (?::uuid, ?::uuid)", roleId, buttonId);
        }
    }

    private void assignRoleMenuByCode(String roleCode, String menuId) {
        String roleId = queryId("SELECT id FROM ops.sys_role WHERE role_code=?", roleCode);
        if (roleId == null || menuId == null) {
            return;
        }
        if (linkMissing("ops.sys_role_menu", "role_id", roleId, "menu_id", menuId)) {
            jdbcTemplate.update("INSERT INTO ops.sys_role_menu (role_id, menu_id) VALUES (?::uuid, ?::uuid)", roleId, menuId);
        }
    }

    private void assignUserRole(String username, String roleId) {
        String userId = queryId("SELECT id FROM ops.sys_user WHERE username=?", username);
        if (userId == null) {
            return;
        }
        if (linkMissing("ops.sys_user_role", "user_id", userId, "role_id", roleId)) {
            jdbcTemplate.update("INSERT INTO ops.sys_user_role (user_id, role_id) VALUES (?::uuid, ?::uuid)", userId, roleId);
        }
    }

    private boolean linkMissing(String table, String colA, String valA, String colB, String valB) {
        Long c = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table + " WHERE " + colA + "=?::uuid AND " + colB + "=?::uuid", Long.class, valA, valB);
        return c == null || c == 0;
    }

    private long count(String table) {
        Long c = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return c == null ? 0 : c;
    }

    private boolean userExists(String username) {
        Long c = jdbcTemplate.queryForObject("SELECT count(*) FROM ops.sys_user WHERE username = ?", Long.class, username);
        return c != null && c > 0;
    }

    private String queryId(String sql, Object... args) {
        List<String> ids = jdbcTemplate.queryForList(sql, String.class, args);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void insertUser(String username, String hash, String realName, String orgId) {
        if (orgId == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_user (username, password_hash, real_name, org_id, status) VALUES (?, ?, ?, NULL, '启用')", username, hash, realName);
        } else {
            jdbcTemplate.update("INSERT INTO ops.sys_user (username, password_hash, real_name, org_id, status) VALUES (?, ?, ?, ?::uuid, '启用')", username, hash, realName, orgId);
        }
    }
}
