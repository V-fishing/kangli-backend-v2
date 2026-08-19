package com.konli.qms.bootstrap;

import com.konli.qms.common.security.PermissionLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
    private final Environment environment;

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
        seedTlmPerms();
        // 追溯树演示数据已禁用: 脏演示树改由 rebuild_trace_trees.ps1 按真实流程(MES 对齐)重建。
        // 仅保留带 VEN 编号的样例供应商主数据种子, 供重建脚本按 VEN 对齐供应商。
        seedSqmSampleSupplier();
        seedFiaStd();
        // 演示业务数据(15 条假审核),仅 dev profile 灌入,避免污染生产库
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            seedSqmAudits();
        }
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
        // 角色可能已由 SeedRunner 预置,但按钮/菜单权限码仍需确保存在;
        // 以 sys_button 是否为空作为幂等守卫,避免重复播种。
        if (count("ops.sys_button") > 0) {
            return;
        }
        String sysadminRoleId = ensureRole("sysadmin", "系统管理员", "预置", "全部权限", null);

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
        assignUserRole("admin", sysadminRoleId);

        // 分公司级角色: 每分公司各建 admin/qmanager/rd(system.* 全量)与 operator(仅 user.list)
        for (String orgId : branchOrgIds()) {
            String adminRoleId = ensureRole("admin", "管理员", "预置", "公司内用户/角色管理 · 基础数据维护", orgId);
            String qmRoleId    = ensureRole("qmanager", "质量经理", "预置", "审批授权 · 趋势分析 · 绩效评审", orgId);
            String rdRoleId    = ensureRole("rd", "研发工程师", "预置", "物料变更研发审批 · 工艺/验证评估", orgId);
            String opRoleId    = ensureRole("operator", "操作工", "预置", "产线操作 · 自检数据录入", orgId);
            for (String rid : List.of(adminRoleId, qmRoleId, rdRoleId)) {
                assignRoleMenus(rid, allMenus);
                assignRoleButtons(rid, allButtons);
            }
            assignRoleMenus(opRoleId, List.of(userMenu));
            assignRoleButtons(opRoleId, List.of(btnUserList));
        }

        // mzuser 绑定梅州分公司 operator 角色
        String mzOrgId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (mzOrgId != null) {
            String mzOperatorId = queryId("SELECT id FROM ops.sys_role WHERE role_code='operator' AND org_id=?::uuid", mzOrgId);
            if (mzOperatorId != null) {
                assignUserRole("mzuser", mzOperatorId);
            }
        }
        log.info("[SEED] 已预置角色/菜单/按钮/权限分配(sysadmin 全局全量, 分公司 admin/qmanager/rd 全量, operator 仅查看用户)");
    }

    /** 额外权限按钮 + 颗粒度权限(关闭/审批/签名分层),幂等补,分配给 sysadmin。 */
    private void seedExtraPerms() {
        // 颗粒度权限 — 按模块菜单挂载
        assignBtn("fia", "fia.sign.inspector", "检验员签名");
        assignBtn("fia", "fia.sign.reviewer", "复核人签名");
        assignBtn("fia", "fia.sign.approver", "批准人签名");
        assignBtn("fia", "fia.sign.disposition", "处置路径");
        assignBtn("fia", "fia.wolock.emergency", "工单紧急放行");
        assignBtn("fia", "fia.wolock.release", "工单审批释放");
        assignBtn("ncm", "ncm.record.delete", "记录/过滤方案删除");
        assignBtn("ncm", "ncm.capa.close", "CAPA关闭");
        assignBtn("ncm", "ncm.capa.approve", "CAPA审批");
        assignBtn("ncm", "ncm.capa.reset", "CAPA重置");
        assignBtn("ncm", "ncm.corrective.close", "纠正措施关闭");
        assignBtn("ncm", "ncm.ca.list", "纠正措施查询");
        assignBtn("ncm", "ncm.ca.create", "纠正措施管理");
        assignBtn("ncm", "ncm.ca.close", "纠正措施关闭");
        assignBtn("sqm", "sqm.change.submit", "变更提交");
        assignBtn("sqm", "sqm.change.approve", "变更审批");
        assignBtn("sqm", "sqm.change.close", "变更关闭");
        assignBtn("sqm", "sqm.change.rollback", "变更回退");
        assignBtn("sqm", "sqm.change.verify-sign", "变更签名验证");
        assignBtn("sqm", "sqm.change.createFia", "变更创建首件");
        assignBtn("sqm", "sqm.fmea.close", "FMEA闭环");
        assignBtn("sqm", "sqm.fmea.reopen", "FMEA重开");
        assignBtn("sqm", "sqm.fmea.scan-overdue", "FMEA超期扫描");
        assignBtn("sqm", "sqm.abnormal.close", "异常关闭");
        assignBtn("sqm", "sqm.abnormal.escalation-check", "异常升级检查");
        assignBtn("sqm", "sqm.audit.plan.start", "审核开始");
        assignBtn("sqm", "sqm.audit.nc.close", "NC关闭");
        assignBtn("sqm", "sqm.audit.archive", "审核归档");
        assignBtn("sqm", "sqm.audit.approve", "审核会签");
        assignBtn("spc", "spc.alarm.launch-8d", "告警发起8D");
        assignBtn("patrol", "patl.task.record", "巡检记录");
        assignBtn("patrol", "patl.task.close", "巡检任务关闭");

        // DELETE 权限码补齐(与各 Controller @DeleteMapping 的 @PreAuthorize 对齐)
        assignBtn("system.menu", "system.menu.delete", "菜单删除");
        assignBtn("fia", "fia.std.delete", "标准删除");
        assignBtn("patrol", "patl.route.delete", "路线删除");
        assignBtn("sqm", "sqm.audit.delete", "审核删除");
        assignBtn("sqm", "sqm.supplier.delete", "供应商删除");
        assignBtn("spc", "spc.param.delete", "参数删除");
        assignBtn("ncm", "ncm.defect.delete", "字典删除");
        assignBtn("ncm", "ncm.8d.delete", "8D删除");

        String userMenuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code='system.user'");
        if (userMenuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(userMenuId, "system.user.create", "新增/编辑/重置密码"));
        }
        String roleMenuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code='system.role'");
        if (roleMenuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(roleMenuId, "system.delegation.manage", "代班管理"));
            assignRoleButtonByCode("sysadmin", ensureButton(roleMenuId, "system.role.create", "角色新增"));
            assignRoleButtonByCode("sysadmin", ensureButton(roleMenuId, "system.role.delete", "角色删除"));
        }
        String orgMenuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code='system.org'");
        if (orgMenuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(orgMenuId, "system.org.create", "组织新增"));
            assignRoleButtonByCode("sysadmin", ensureButton(orgMenuId, "system.org.delete", "组织删除"));
        }
        // 切换分公司(可配置权限,超管默认拥有,可分配给其他角色;无权限者顶栏不显示切换器)。
        // 挂在始终存在的 system.org.list 菜单下,避免依赖 seedRbac 才创建的 system.org 菜单(幂等且不受 seedRbac 守卫影响)。
        assignBtn("system.org.list", "system.org.switch", "切换分公司");

        // 绩效模块权限码(原 Controller 错用 sqm.capa,已改为 sqm.perf.* 与 sqm.supplier.list 对齐)
        assignBtn("sqm", "sqm.perf.calc", "绩效自动计算");
        assignBtn("sqm", "sqm.perf.create", "绩效手工录入");
        assignBtn("sqm", "sqm.perf.cfg", "绩效指标配置");
        // 审核/签批配置(8D 阶段签批 + 供应商会签),页面在系统管理-审核配置
        assignBtn("sqm", "system.audit-config", "审核/签批配置");

        // 售后模块权限码(cs 菜单由 Flyway V018/V203 预置)
        assignBtn("cs", "cs.workorder.list", "售后工单查询");
        assignBtn("cs", "cs.workorder.create", "工单新建");
        assignBtn("cs", "cs.workorder.edit", "工单编辑");
        assignBtn("cs", "cs.workorder.assign", "工单派单/完成");
        assignBtn("cs", "cs.workorder.close", "工单评价闭环");
        assignBtn("cs", "cs.workorder.delete", "工单删除");
        assignBtn("cs", "cs.feedback.list", "客户反馈查询");
        assignBtn("cs", "cs.feedback.create", "反馈登记");
        assignBtn("cs", "cs.feedback.edit", "反馈编辑");
        assignBtn("cs", "cs.feedback.delete", "反馈删除");
        assignBtn("cs", "cs.feedback.handle", "反馈处理");
        assignBtn("cs", "cs.feedback.link", "反馈联动");
        // 确保 admin 始终有关联 sysadmin 角色(seedRbac 有 count>0 幂等跳过,admin 关联可能被跳过)
        String sysadminRoleId = queryId("SELECT id FROM ops.sys_role WHERE role_code='sysadmin' AND org_id IS NULL");
        if (sysadminRoleId != null) {
            assignUserRole("admin", sysadminRoleId);
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

        // SPC 工序主数据(参数的父级分组):作为 SPC 模块子菜单,分配查询/管理/删除按钮
        String procMenu = ensureChildMenu(spcMenu, "spc.process", "SPC工序管理", "/spc/processes", "spc/processes", 1);
        assignRoleMenuByCode("sysadmin", procMenu);
        assignRoleMenuByCode("admin", procMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(procMenu, "spc.process.list", "工序查询"));
        assignRoleButtonByCode("admin", ensureButton(procMenu, "spc.process.list", "工序查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(procMenu, "spc.process.create", "工序管理"));
        assignRoleButtonByCode("admin", ensureButton(procMenu, "spc.process.create", "工序管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(procMenu, "spc.process.delete", "工序删除"));
        assignRoleButtonByCode("admin", ensureButton(procMenu, "spc.process.delete", "工序删除"));

        // SPC 抽样任务创建(独立二级菜单,与首件采集平行):建任务后任务列表进 SPC参数页,列表跳转采集
        String sampleTaskMenu = ensureChildMenu(spcMenu, "spc.sampletask", "抽样任务创建", "/spc/sample-tasks", "spc/SampleTaskCreate", 4);
        assignRoleMenuByCode("sysadmin", sampleTaskMenu);
        assignRoleMenuByCode("admin", sampleTaskMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(sampleTaskMenu, "spc.sample-task.list", "抽样任务查询"));
        assignRoleButtonByCode("admin", ensureButton(sampleTaskMenu, "spc.sample-task.list", "抽样任务查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sampleTaskMenu, "spc.sample-task.create", "抽样任务创建"));
        assignRoleButtonByCode("admin", ensureButton(sampleTaskMenu, "spc.sample-task.create", "抽样任务创建"));
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
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.create", "8D创建/编辑"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.advance", "8D阶段推进"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.approve", "8D审批(D3/D5/D7)"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.8d.reopen", "8D重开"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.capa.list", "CAPA查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(ncmMenu, "ncm.capa.create", "CAPA管理"));

        // 不良趋势报表(独立菜单,复用 ncm.record.list 接口权限)
        String trendMenu = ensureMenu("ncm.trend", "不良趋势报表", "/ncm/trend-reports", "ncm/trend-reports", 71);
        assignRoleMenuByCode("sysadmin", trendMenu);
        assignRoleMenuByCode("quality", trendMenu);
        assignRoleMenuByCode("mzuser", trendMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(trendMenu, "ncm.record.list", "报表查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(trendMenu, "ncm.trend.generate", "报表生成"));
    }

    /** SQM 权限种子(sqm 菜单 + 供应商/审核/变更/追溯/异常按钮,分配 sysadmin)。幂等。 */
    private void seedSqmPerms() {
        String sqmMenu = ensureMenu("sqm", "供应商质量", "/sqm", "sqm/index", 8);
        assignRoleMenuByCode("sysadmin", sqmMenu);
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.supplier.list", "供应商查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.supplier.create", "供应商管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.audit.list", "审核查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.audit.create", "审核管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.audit.approve", "审核会签"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.change.list", "变更查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.change.create", "变更管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.change.createFia", "变更创建首件"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.trace.list", "追溯查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.trace.create", "追溯录入"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.abnormal.list", "异常查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.abnormal.create", "异常管理"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.fmea.list", "FMEA查询"));
        assignRoleButtonByCode("sysadmin", ensureButton(sqmMenu, "sqm.fmea.edit", "FMEA编辑"));

        // 物料变更三方审批角色:授予 sqm 菜单 + 变更查询/提交/审批 按钮
        // (采购 purchaser / 研发 rd / 质量 sqe)。每次启动幂等补权,使其可参与依次签字。
        // 确保各分公司 rd 角色存在(不依赖 SeedRunner 顺序)
        for (String orgId : branchOrgIds()) {
            ensureRole("rd", "研发工程师", "rd", "查看权限", orgId);
        }
        String[] changeRoles = {"purchaser", "rd", "sqe"};
        for (String rc : changeRoles) {
            assignRoleMenuByCode(rc, sqmMenu);
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.list", "变更查询"));
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.submit", "变更提交"));
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.approve", "变更审批"));
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.close", "变更关闭"));
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.rollback", "变更回退"));
            assignRoleButtonByCode(rc, ensureButton(sqmMenu, "sqm.change.createFia", "变更创建首件"));
        }
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

    /** TLM 工装管理权限种子(tlm 目录 + 台账/维保/异常 菜单 + 16 按钮,分配 sysadmin)。幂等。
     *  与 V170 Flyway 脚本互补: Flyway 负责多环境基线, 本方法保证启动即幂等补权(包括新增角色场景)。 */
    private void seedTlmPerms() {
        String tlmMenu = ensureMenu("tlm", "工装管理", "/tlm", "tlm/index", 10);
        String toolingMenu = ensureChildMenu(tlmMenu, "tlm.tooling.list", "工装台账", "tooling", "tlm/Tooling", 1);
        String maintMenu = ensureChildMenu(tlmMenu, "tlm.maint.list", "工装维保", "maint", "tlm/Maint", 2);
        String abnormalMenu = ensureChildMenu(tlmMenu, "tlm.abnormal.list", "工装异常", "abnormals", "tlm/Abnormal", 3);
        String scrapMenu = ensureChildMenu(tlmMenu, "tlm.scrap.list", "报废管理", "scraps", "tlm/Scraps", 4);
        assignRoleMenuByCode("sysadmin", tlmMenu);
        assignRoleMenuByCode("sysadmin", scrapMenu);
        assignRoleMenuByCode("sysadmin", toolingMenu);
        assignRoleMenuByCode("sysadmin", maintMenu);
        assignRoleMenuByCode("sysadmin", abnormalMenu);
        // 台账按钮 8
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.create", "工装新增"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.edit", "工装编辑"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.delete", "工装删除"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.scrap", "工装报废"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.repair", "工装送修"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.lock", "工装锁定"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.bind", "工装绑定"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.first", "创建首件"));
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.tooling.export", "台账导出"));
        // 维保按钮 5
        assignRoleButtonByCode("sysadmin", ensureButton(maintMenu, "tlm.maint.plan.create", "保养计划新建"));
        assignRoleButtonByCode("sysadmin", ensureButton(maintMenu, "tlm.maint.plan.edit", "保养计划编辑"));
        assignRoleButtonByCode("sysadmin", ensureButton(maintMenu, "tlm.maint.plan.delete", "保养计划删除"));
        assignRoleButtonByCode("sysadmin", ensureButton(maintMenu, "tlm.maint.record.create", "保养记录登记"));
        assignRoleButtonByCode("sysadmin", ensureButton(maintMenu, "tlm.maint.record.delete", "保养记录删除"));
        // 报废按钮 1
        assignRoleButtonByCode("sysadmin", ensureButton(toolingMenu, "tlm.scrap.approve", "报废审批"));
    }

    // ==================== SQM 来料追溯种子(4.4 全链路追溯树) ====================

    /**
     * 预置 SQM 来料追溯种子数据(幂等):sqm_trace_node 为空时才灌。
     * 仅预置一条带 MES VEN 编号的样例供应商(主数据),不构造追溯树。
     * 追溯树演示数据已禁用,改由 rebuild_trace_trees.ps1 按真实流程重建。
     */
    private void seedSqmSampleSupplier() {
        String orgId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (orgId == null) {
            log.warn("[SEED] 缺少 MZ 组织,跳过 SQM 样例供应商种子");
            return;
        }
        Long exist = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ops.sqm_supplier WHERE is_deleted = false AND ven_code = 'VEN00417'", Long.class);
        if (exist != null && exist > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_supplier (org_id, supplier_no, supplier_code, name, credit_code, category, level, status, score, contact_person, contact_phone, address, ven_code) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?, ?, ?, ?)",
                orgId, "SUP-0001", "S001", "华南电子材料有限公司", "91440300MA5SEED001X",
                "电子料", "A", "合格", "92.50", "李经理", "0755-88880001", "广东省深圳市南山区科技园南区 A 座", "VEN00417");
        log.info("[SEED] 已预置样例供应商(华南电子材料, VEN00417)");
    }

    /**
     * 构造两条完整追溯链(来料 incoming -> 原料 raw -> 半成品 semi -> 成品出货 ship -> 客户 customer),
     * 使前端「来料追溯」页面可直接渲染全链路追溯树。
     * 追溯根需 sqm_incoming_lot + sqm_supplier,若无供应商则自动补一条样例供应商。
     * （已禁用: 追溯树演示数据改由重建脚本按真实流程重建）
     */
    private void seedSqmTrace() {
        // 幂等:以专用演示批次 LOT-2026-0001 是否存在为准(库里可能已有仅含单个来料节点的
        // 其它批次,不能以 sqm_trace_node 全表是否为空来判断,否则全链路演示批次永远灌不进去)。
        Long demo = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ops.sqm_incoming_lot WHERE lot_no = 'LOT-2026-0001'", Long.class);
        if (demo != null && demo > 0) {
            return;
        }
        String orgId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (orgId == null) {
            log.warn("[SEED] 缺少 MZ 组织,跳过 SQM 追溯种子");
            return;
        }
        String supplierId = ensureSampleSupplier(orgId);
        if (supplierId == null) {
            log.warn("[SEED] 无可用供应商,跳过 SQM 追溯种子");
            return;
        }

        // -------- 链一:关键件(储能电芯,逐件 SN 追溯) --------
        String lot1 = insertLot(orgId, supplierId, "LOT-2026-0001", "BAT-CELL-3000", "储能电芯",
                "5000", "PCS", "2026-06-01", "合格", "全检", true, "PO-2026-0601", true);
        String n1In = insertNode(orgId, lot1, null, "incoming", "储能电芯来料检验", "IQC-20260601-01",
                "5000", "PCS", "2026-06-01", supplierId, 0, "IQC 全检合格入库", "BAT-CELL-3000", "合格");
        String n1Raw = insertNode(orgId, lot1, n1In, "raw", "锂电芯原料上线", "RAW-LC-260602",
                "5000", "PCS", "2026-06-02", supplierId, 1, "原料扫码上线", "MC-LC-3000", "合格");
        String n1Semi = insertNode(orgId, lot1, n1Raw, "semi", "电池模组组装", "SEMI-MOD-260605",
                "480", "套", "2026-06-05", null, 2, "48 芯/模组", "MOD-48S1P", "合格");
        String n1Ship = insertNode(orgId, lot1, n1Semi, "ship", "储能电池包成品出货", "FG-PACK-260610",
                "120", "套", "2026-06-10", null, 3, "成品终检合格出货", "PACK-61.44kWh", "合格");
        String n1Cust = insertNode(orgId, lot1, n1Ship, "customer", "南方新能源科技有限公司", "CUST-260612",
                "120", "套", "2026-06-12", null, 4, "客户签收", "PACK-61.44kWh", "合格");

        insertRawDetail(orgId, n1Raw, "锂电芯", "WO-260602-01", "MB-LC-0001", "MC-LC-3000",
                "磷酸铁锂电芯", "3.2V/280Ah", "张工", "2026-06-02 08:30:00", "P010", "电芯上线");
        insertProductDetail(orgId, n1Semi, "电池模组", "SEMI-MOD-260605", "2026-06-05", "合格",
                "李工", "王主管", "2026-06-05 17:00:00", null, null, null, "48S1P/15.36kWh", "480", "480", "套");
        insertProductDetail(orgId, n1Ship, "储能电池包", "FG-PACK-260610", "2026-06-10", "合格",
                "赵工", "王主管", "2026-06-10 16:00:00", "南方新能源科技有限公司", "SO-2026-0610",
                "2026-06-10", "1P4S/61.44kWh", "120", "120", "套");
        insertCustomerDetail(orgId, n1Cust, "南方新能源科技有限公司", "C-NF-001", "SO-2026-0610",
                "2026-06-10", "SF-2026061012345", "广东省广州市黄埔区新能源产业园 8 号", "陈经理", "020-88886666", "120", "套");

        insertKeyPartSn(orgId, lot1, supplierId, "SN-BAT-260601-0001", "BAT-CELL-3000", "储能电芯", "出货", "PACK-L1", "WO-260605-01", "2026-06-05 09:10:00");
        insertKeyPartSn(orgId, lot1, supplierId, "SN-BAT-260601-0002", "BAT-CELL-3000", "储能电芯", "出货", "PACK-L1", "WO-260605-01", "2026-06-05 09:11:00");
        insertKeyPartSn(orgId, lot1, supplierId, "SN-BAT-260601-0003", "BAT-CELL-3000", "储能电芯", "在线", "PACK-L1", "WO-260605-02", "2026-06-05 09:12:00");

        // -------- 链二:非关键件(注塑外壳,树状批次追溯) --------
        String lot2 = insertLot(orgId, supplierId, "LOT-2026-0002", "HS-1200", "外壳塑料件",
                "8000", "PCS", "2026-06-03", "合格", "抽检", true, "PO-2026-0603", false);
        String n2In = insertNode(orgId, lot2, null, "incoming", "外壳塑料件来料检验", "IQC-20260603-02",
                "8000", "PCS", "2026-06-03", supplierId, 0, "IQC 抽检合格入库", "MAT-ABS-1200", "合格");
        String n2Raw = insertNode(orgId, lot2, n2In, "raw", "ABS 粒料上线", "RAW-ABS-260604",
                "8000", "PCS", "2026-06-04", supplierId, 1, "注塑原料上线", "MC-ABS-1200", "合格");
        String n2Semi = insertNode(orgId, lot2, n2Raw, "semi", "外壳注塑成型", "SEMI-HS-260606",
                "7800", "PCS", "2026-06-06", null, 2, "注塑 + 喷涂", "HS-1200", "合格");
        String n2Ship = insertNode(orgId, lot2, n2Semi, "ship", "储能机箱成品出货", "FG-BOX-260611",
                "1900", "台", "2026-06-11", null, 3, "组装后随机出货", "BOX-2U", "合格");
        String n2Cust = insertNode(orgId, lot2, n2Ship, "customer", "南方新能源科技有限公司", "CUST-260613",
                "1900", "台", "2026-06-13", null, 4, "客户签收", "BOX-2U", "合格");

        insertRawDetail(orgId, n2Raw, "塑料粒料", "WO-260604-02", "MB-ABS-0001", "MC-ABS-1200",
                "ABS 阻燃粒料", "PA-757", "刘工", "2026-06-04 09:00:00", "P020", "注塑上线");
        insertProductDetail(orgId, n2Semi, "外壳注塑件", "SEMI-HS-260606", "2026-06-06", "合格",
                "孙工", "王主管", "2026-06-06 18:00:00", null, null, null, "HS-1200 灰白", "8000", "7800", "PCS");
        insertProductDetail(orgId, n2Ship, "储能机箱", "FG-BOX-260611", "2026-06-11", "合格",
                "周工", "王主管", "2026-06-11 15:30:00", "南方新能源科技有限公司", "SO-2026-0611",
                "2026-06-11", "BOX-2U", "1900", "1900", "台");
        insertCustomerDetail(orgId, n2Cust, "南方新能源科技有限公司", "C-NF-001", "SO-2026-0611",
                "2026-06-11", "SF-2026061167890", "广东省广州市黄埔区新能源产业园 8 号", "陈经理", "020-88886666", "1900", "台");

        log.info("[SEED] 已预置 SQM 来料追溯种子(2 条全链路追溯树:关键件+非关键件)");
    }

    /**
     * FIA 标准库种子:为每一个(物料编码 + 供应商)来料组合建立「来料首件」标准与检验计划,
     * 使前端「选来料批次自动带出标准」对所有演示批次都开箱即用。按 part_no + supplier_id 幂等。
     */
    private void seedFiaStd() {
        String orgId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (orgId == null) {
            return;
        }
        // 直接依据已存在的来料批次生成标准,覆盖全部演示物料(含同一物料不同供应商的情形)
        List<Map<String, Object>> lots = jdbcTemplate.queryForList(
                "SELECT DISTINCT part_no, supplier_id::text AS supplier_id, part_name FROM ops.sqm_incoming_lot " +
                        "WHERE org_id = ?::uuid AND part_no IS NOT NULL AND supplier_id IS NOT NULL AND is_deleted = false",
                orgId);
        int n = 0;
        for (Map<String, Object> lot : lots) {
            String partNo = (String) lot.get("part_no");
            String supplierId = (String) lot.get("supplier_id");
            Object pn = lot.get("part_name");
            String partName = pn == null ? partNo : (String) pn;
            seedOneFiaStd(orgId, supplierId, partNo, partName);
            n++;
        }
        log.info("[SEED] FIA 来料首件标准预置完成, 覆盖 {} 个(物料,供应商)组合", n);
    }

    private void seedOneFiaStd(String orgId, String supplierId, String partNo, String partName) {
        // 注意:std_code 由 partNo + supplierId 前 8 位组成,可能跨 supplier 碰撞(全局唯一约束)。
        // 因此幂等/跳过判定必须以 code 为准,而非完整 supplier_id。
        String stdCode = "STD-" + partNo + "-" + supplierId.replace("-", "").substring(0, 8);
        // 用 count 判定避免 queryForObject 在 0 行时抛 EmptyResultDataAccessException
        Long exist = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ops.fia_insp_std WHERE org_id = ?::uuid AND code = ? AND is_deleted = false",
                Long.class, orgId, stdCode);
        if (exist != null && exist > 0) {
            return;
        }
        // ON CONFLICT DO NOTHING 兜底跨 supplier 前 8 位碰撞;随后按 code 取回 id(新插或已存在均可)
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_std (org_id, code, material, proc_name, aql, inspect_level, sample_plan, ctq_text, std_version, status, part_no, supplier_id) " +
                        "VALUES (?::uuid, ?, ?, '来料首件', '1.0', 'II', '单次', '关键尺寸/外观 CTQ', 'v1', '生效', ?, ?::uuid) " +
                        "ON CONFLICT (code) DO NOTHING",
                orgId, stdCode, partName, partNo, supplierId);
        String stdId = jdbcTemplate.queryForObject(
                "SELECT id FROM ops.fia_insp_std WHERE org_id = ?::uuid AND code = ? AND is_deleted = false",
                String.class, orgId, stdCode);
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_std_item (org_id, std_id, seq, item_name, is_ctq, std_value, tolerance, unit, value_type) " +
                        "VALUES (?::uuid, ?::uuid, 1, '外观', true, '无划伤/无变形/无污渍', '', '—', '文本')", orgId, stdId);
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_std_item (org_id, std_id, seq, item_name, is_ctq, std_value, tolerance, unit, value_type) " +
                        "VALUES (?::uuid, ?::uuid, 2, '关键尺寸', true, '10.00', '±0.05', 'mm', '数值')", orgId, stdId);
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_std_item (org_id, std_id, seq, item_name, is_ctq, std_value, tolerance, unit, value_type) " +
                        "VALUES (?::uuid, ?::uuid, 3, '性能测试', false, '≥95', '', '%', '数值')", orgId, stdId);
        jdbcTemplate.update(
                "INSERT INTO ops.fia_insp_plan (org_id, plan_code, plan_name, material_category, supplier_id, proc_name, std_id, aql, sample_level, sample_plan, severity, is_active) " +
                        "VALUES (?::uuid, ?, ?, ?, ?::uuid, '来料首件', ?::uuid, 1.0, 'II', '单次', '一般', true)",
                orgId, "PLAN-" + stdCode, "来料首件计划-" + partNo, partNo, supplierId, stdId);
    }

    /** 取首个供应商;若一条都没有则补一条样例供应商,返回其 id。 */
    private String ensureSampleSupplier(String orgId) {
        String existing = queryId("SELECT id FROM ops.sqm_supplier WHERE is_deleted = false ORDER BY created_at LIMIT 1");
        if (existing != null) {
            return existing;
        }
        return jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_supplier (org_id, supplier_no, supplier_code, name, credit_code, category, level, status, score, contact_person, contact_phone, address, ven_code) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?, ?, ?, ?) RETURNING id",
                String.class, orgId, "SUP-0001", "S001", "华南电子材料有限公司", "91440300MA5SEED001X",
                "电子料", "A", "合格", "92.50", "李经理", "0755-88880001", "广东省深圳市南山区科技园南区 A 座", "VEN00417");
    }

    private String insertLot(String orgId, String supplierId, String lotNo, String partNo, String partName,
                             String qty, String unit, String incomingDate, String inspectResult,
                             String inspectType, boolean iqcPass, String poNo, boolean keyPart) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_incoming_lot (org_id, lot_no, supplier_id, part_no, part_name, qty, unit, incoming_date, inspect_result, inspect_type, iqc_pass, po_no, is_key_part) " +
                        "VALUES (?::uuid, ?, ?::uuid, ?, ?, ?::numeric, ?, ?::date, ?, ?, ?, ?, ?) RETURNING id",
                String.class, orgId, lotNo, supplierId, partNo, partName, qty, unit, incomingDate,
                inspectResult, inspectType, iqcPass, poNo, keyPart);
    }

    private String insertNode(String orgId, String rootLotId, String parentNodeId, String nodeType,
                              String nodeName, String batchNo, String qty, String unit, String nodeDate,
                              String supplierId, int treeLevel, String remark,
                              String materialCode, String qualificationType) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_trace_node (org_id, root_lot_id, parent_node_id, node_type, node_name, batch_no, qty, unit, node_date, supplier_id, tree_level, is_valid, remark, material_code, qualification_type) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?::numeric, ?, ?::date, ?::uuid, ?, '是', ?, ?, ?) RETURNING id",
                String.class, orgId, rootLotId, parentNodeId, nodeType, nodeName, batchNo, qty, unit,
                nodeDate, supplierId, treeLevel, remark, materialCode, qualificationType);
    }

    private void insertRawDetail(String orgId, String nodeId, String category, String woNo, String materialBarcode,
                                 String materialCode, String materialName, String specModel,
                                 String scanner, String scanTime, String processCode, String processName) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_trace_raw_detail (org_id, node_id, category, wo_no, material_barcode, material_code, material_name, spec_model, scanner, scan_time, process_code, process_name) " +
                        "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::timestamptz, ?, ?)",
                orgId, nodeId, category, woNo, materialBarcode, materialCode, materialName, specModel,
                scanner, scanTime, processCode, processName);
    }

    private void insertProductDetail(String orgId, String nodeId, String productName, String batchNo,
                                     String productionDate, String inspectResult, String inspector,
                                     String qcReviewer, String qcReviewTime, String customer,
                                     String customerOrderNo, String shipDate, String modelSpec,
                                     String inspectQty, String passQty, String unit) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_trace_product_detail (org_id, node_id, product_name, batch_no, production_date, inspect_result, inspector, qc_reviewer, qc_review_time, customer, customer_order_no, ship_date, model_spec, inspect_qty, pass_qty, unit) " +
                        "VALUES (?::uuid, ?::uuid, ?, ?, ?::date, ?, ?, ?, ?::timestamptz, ?, ?, ?::date, ?, ?::numeric, ?::numeric, ?)",
                orgId, nodeId, productName, batchNo, productionDate, inspectResult, inspector, qcReviewer,
                qcReviewTime, customer, customerOrderNo, shipDate, modelSpec, inspectQty, passQty, unit);
    }

    private void insertCustomerDetail(String orgId, String nodeId, String customerName, String customerCode,
                                      String customerOrderNo, String shipDate, String trackingNo,
                                      String shipAddress, String contactPerson, String contactPhone,
                                      String qty, String unit) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_trace_customer_detail (org_id, node_id, customer_name, customer_code, customer_order_no, ship_date, tracking_no, ship_address, contact_person, contact_phone, qty, unit) " +
                        "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?::date, ?, ?, ?, ?, ?::numeric, ?)",
                orgId, nodeId, customerName, customerCode, customerOrderNo, shipDate, trackingNo,
                shipAddress, contactPerson, contactPhone, qty, unit);
    }

    private void insertKeyPartSn(String orgId, String lotId, String supplierId, String snCode,
                                 String partNo, String partName, String status, String line, String woNo, String scanTime) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_key_part_sn (org_id, sn_code, lot_id, part_no, part_name, supplier_id, status, line, wo_no, scan_time) " +
                        "VALUES (?::uuid, ?, ?::uuid, ?, ?, ?::uuid, ?, ?, ?, ?::timestamptz)",
                orgId, snCode, lotId, partNo, partName, supplierId, status, line, woNo, scanTime);
    }

    // ---- helpers ----
    /** 按 (role_code, org_id) 确保角色存在; orgId=null 为全局角色(如 sysadmin)。 */
    private String ensureRole(String code, String name, String type, String desc, String orgId) {
        String id = orgId == null
                ? queryId("SELECT id FROM ops.sys_role WHERE role_code=? AND org_id IS NULL", code)
                : queryId("SELECT id FROM ops.sys_role WHERE role_code=? AND org_id=?::uuid", code, orgId);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_role (role_code, role_name, role_type, perm_desc, status, org_id) VALUES (?, ?, ?, ?, '启用', ?::uuid)", code, name, type, desc, orgId);
            id = orgId == null
                    ? queryId("SELECT id FROM ops.sys_role WHERE role_code=? AND org_id IS NULL", code)
                    : queryId("SELECT id FROM ops.sys_role WHERE role_code=? AND org_id=?::uuid", code, orgId);
        }
        return id;
    }

    /** MZ/SZ 分公司组织 id 列表(按 org_code 排序, 缺失时返回空)。 */
    private List<String> branchOrgIds() {
        return jdbcTemplate.queryForList(
                "SELECT id FROM ops.sys_org WHERE org_code IN ('MZ','SZ') ORDER BY org_code", String.class);
    }

    private String ensureMenu(String code, String name, String path, String component, int sort) {
        String id = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", code);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_menu (menu_code, menu_name, menu_type, path, component, sort_order, visible) VALUES (?, ?, '菜单', ?, ?, ?, true)", code, name, path, component, sort);
            id = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", code);
        }
        return id;
    }

    /** 子菜单(带 parent_id),用于模块下的二级页签。幂等。 */
    private String ensureChildMenu(String parentId, String code, String name, String path, String component, int sort) {
        String id = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", code);
        if (id == null) {
            jdbcTemplate.update("INSERT INTO ops.sys_menu (parent_id, menu_code, menu_name, menu_type, path, component, sort_order, visible) VALUES (?::uuid, ?, ?, '菜单', ?, ?, ?, true)",
                    parentId, code, name, path, component, sort);
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

    /** 快捷: 按菜单 code + 按钮 code 注册权限按钮并分配给 sysadmin */
    private void assignBtn(String menuCode, String btnCode, String btnName) {
        String menuId = queryId("SELECT id FROM ops.sys_menu WHERE menu_code=?", menuCode);
        if (menuId != null) {
            assignRoleButtonByCode("sysadmin", ensureButton(menuId, btnCode, btnName));
        }
    }

    /** 对同 role_code 的全部角色(各分公司副本 + 全局)逐一授予按钮权限。 */
    private void assignRoleButtonByCode(String roleCode, String buttonId) {
        if (buttonId == null) {
            return;
        }
        List<String> roleIds = jdbcTemplate.queryForList(
                "SELECT id FROM ops.sys_role WHERE role_code=?", String.class, roleCode);
        for (String roleId : roleIds) {
            if (linkMissing("ops.sys_role_button", "role_id", roleId, "button_id", buttonId)) {
                jdbcTemplate.update("INSERT INTO ops.sys_role_button (role_id, button_id) VALUES (?::uuid, ?::uuid)", roleId, buttonId);
            }
        }
    }

    /** 对同 role_code 的全部角色(各分公司副本 + 全局)逐一授予菜单权限。 */
    private void assignRoleMenuByCode(String roleCode, String menuId) {
        if (menuId == null) {
            return;
        }
        List<String> roleIds = jdbcTemplate.queryForList(
                "SELECT id FROM ops.sys_role WHERE role_code=?", String.class, roleCode);
        for (String roleId : roleIds) {
            if (linkMissing("ops.sys_role_menu", "role_id", roleId, "menu_id", menuId)) {
                jdbcTemplate.update("INSERT INTO ops.sys_role_menu (role_id, menu_id) VALUES (?::uuid, ?::uuid)", roleId, menuId);
            }
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

    private boolean hasAuditPlan(String no) {
        return queryId("SELECT id FROM ops.sqm_audit_plan WHERE plan_no=?", no) != null;
    }

    /** SQM 供应商审核种子: 覆盖全部 15 种审核业务类型, 按 plan_no 幂等(已存在跳过, 可增量补充)。 */
    private void seedSqmAudits() {
        String orgId = queryId("SELECT id FROM ops.sys_org WHERE org_code='MZ'");
        if (orgId == null) return;
        String supplierId = queryId("SELECT id FROM ops.sqm_supplier WHERE is_deleted=false ORDER BY created_at LIMIT 1");
        if (supplierId == null) return;

        // 1. 物料变更审核(供应商发起 -> 物料变更管理流程)
        if (!hasAuditPlan("AUD-2026-0001")) {
        String p1 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0001", supplierId, "物料变更审核",
                "2026-05-12", "王质量", "质量,采购,研发", "电芯正极材料升级变更评审", "高", "2026-05-20", "已完成");
        String r1 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0001", p1, supplierId, "物料变更审核", "2026-05-20",
                "王质量", "质量,采购,研发", "通过", "92.50", 1, "材料升级变更评审通过,转入物料变更管理流程试产验证", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, verify_result, verify_comment, verify_date, verify_by, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?, ?, ?::timestamptz, ?, ?::timestamptz)",
                orgId, "NC-2026-0001", r1, supplierId, "8.3 变更通知", "变更后首批来料需加严检验 3 批", "观察项", "已关闭", "供应商SQE", "2026-05-30",
                "已提交变更控制计划PCC", "2026-05-26 10:00:00", "通过", "观察项已闭环", "2026-05-28 09:00:00", "王质量", "2026-05-28 09:30:00");
        }

        // 2. 资质审核(供应商发起 -> 供应商全生命周期管理)
        if (!hasAuditPlan("AUD-2026-0002")) {
        String p2 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0002", supplierId, "资质审核",
                "2026-03-10", "李资质", "质量,采购", "营业执照/体系证书/产能资质复核", "中", "2026-03-18", "已完成");
        String r2 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0002", p2, supplierId, "资质审核", "2026-03-18",
                "李资质", "质量,采购", "通过", "95.00", 1, "资质齐全有效,维持合格供应商名录", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, verify_result, verify_comment, verify_date, verify_by, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?, ?, ?::timestamptz, ?, ?::timestamptz)",
                orgId, "NC-2026-0002", r2, supplierId, "7.2 文件控制", "ISO9001 证书临近换证期", "一般", "已关闭", "供应商质量", "2026-04-10",
                "已提交换证计划并承诺到期前完成", "2026-03-30 14:00:00", "通过", "整改有效", "2026-04-02 10:00:00", "李资质", "2026-04-02 10:30:00");
        }

        // 3. 年度审核(我方发起 -> 供应商全生命周期管理)
        if (!hasAuditPlan("AUD-2026-0003")) {
        String p3 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0003", supplierId, "年度审核",
                "2026-01-15", "王质量", "质量,采购,SQE", "年度体系/过程/交付绩效全面审核", "高", "2026-01-22", "已完成");
        String r3 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0003", p3, supplierId, "年度审核", "2026-01-22",
                "王质量", "质量,采购,SQE", "有条件通过", "81.50", 2, "年度审核有条件通过,需闭环2项不符合项后维持A级", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, need_site_review, verify_result, verify_comment, verify_date, verify_by, verified_batches, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?::boolean, ?, ?, ?::timestamptz, ?, ?::int, ?::timestamptz)",
                orgId, "NC-2026-0003", r3, supplierId, "8.5 生产控制", "关键工序SPC未全覆盖", "严重", "已关闭", "供应商质量总监", "2026-02-15",
                "补充SPC子组并培训,现场复核通过", "2026-02-10 09:00:00", true, "通过", "严重项现场复核合格", "2026-02-12 15:00:00", "王质量", 3, "2026-02-12 15:30:00");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, verify_result, verify_comment, verify_date, verify_by, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?, ?, ?::timestamptz, ?, ?::timestamptz)",
                orgId, "NC-2026-0004", r3, supplierId, "8.7 交付", "交付准时率波动", "一般", "已关闭", "供应商交付", "2026-02-20",
                "优化排产与备货", "2026-02-18 11:00:00", "通过", "交付改善有效", "2026-02-19 16:00:00", "王质量", "2026-02-19 16:30:00");
        }

        // 4. 季度审核(我方发起 -> 生命周期体检, 进行中)
        if (!hasAuditPlan("AUD-2026-0004")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0004", supplierId, "季度审核",
                "2026-07-10", "王质量", "质量,SQE", "季度绩效与交付体检", "中", "进行中");
        }

        // 5. 来料异常审核(我方发起 -> 现场审核 -> 来料异常整改流程)
        if (!hasAuditPlan("AUD-2026-0005")) {
        String p5 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0005", supplierId, "来料异常审核",
                "2026-06-05", "赵异常", "质量,SQE", "针对批次不合格开展现场审核", "高", "2026-06-12", "已完成");
        String r5 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0005", p5, supplierId, "来料异常审核", "2026-06-12",
                "赵异常", "质量,SQE", "不通过", "62.00", 1, "来料批次不合格,启动现场审核并流入来料异常整改(CAR)", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, need_site_review, verify_result, verify_comment, verify_date, verify_by, verified_batches, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?::boolean, ?, ?, ?::timestamptz, ?, ?::int, ?::timestamptz)",
                orgId, "NC-2026-0005", r5, supplierId, "8.6 检验", "来料批次尺寸超差", "严重", "已关闭", "供应商质量", "2026-06-25",
                "停线整改+8D,连续3批合格放行", "2026-06-20 09:00:00", true, "通过", "现场复核+连续3批合格闭环", "2026-06-24 14:00:00", "赵异常", 3, "2026-06-24 14:30:00");
        }

        // 6. 临时审核(质量主管发起 -> 现场审核流程, 待执行)
        if (!hasAuditPlan("AUD-2026-0006")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0006", supplierId, "临时审核",
                "2026-07-18", "王质量", "质量", "针对客诉的临时专项现场审核", "高", "待执行");
        }

        // 7. 重大来料异常审核(系统自动触发 -> 现场审核 -> 整改流程, 计划中)
        if (!hasAuditPlan("AUD-2026-0007")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0007", supplierId, "重大来料异常审核",
                "2026-07-20", "系统自动", "质量,SQE", "重大异常自动触发现场审核", "高", "计划中");
        }

        // 8. 供应商准入审核(供应商发起申请 -> 审核预选目标状态 -> 完整生命周期, 计划中)
        if (!hasAuditPlan("AUD-2026-0008")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0008", supplierId, "供应商准入审核",
                "2026-07-15", "王质量", "质量,采购", "新供应商准入申请,预选目标等级A后进入生命周期", "中", "计划中");
        }

        // 9. 年度复审(我方发起, 已完成, 含记录+不符合项)
        if (!hasAuditPlan("AUD-2026-0009")) {
        String p9 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0009", supplierId, "年度复审",
                "2026-06-28", "王质量", "质量,采购,SQE", "年度复审及纠正措施有效性验证", "中", "2026-06-30", "已完成");
        String r9 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0009", p9, supplierId, "年度复审", "2026-06-30",
                "王质量", "质量,采购,SQE", "通过", "88.00", 1, "年度复审通过,上年度不符合项整改有效", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, verify_result, verify_comment, verify_date, verify_by, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?, ?, ?::timestamptz, ?, ?::timestamptz)",
                orgId, "NC-2026-0009", r9, supplierId, "10.2 改进", "上年度观察项巩固措施待固化", "观察项", "已关闭", "供应商SQE", "2026-07-10",
                "已纳入年度改进计划并固化", "2026-07-05 09:00:00", "通过", "观察项已闭环", "2026-07-08 10:00:00", "王质量", "2026-07-08 10:30:00");
        }

        // 10. 过程审核(我方发起, 已完成, 含记录)
        if (!hasAuditPlan("AUD-2026-0010")) {
        String p10 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0010", supplierId, "过程审核",
                "2026-04-08", "孙过程", "质量,SQE", "制造过程VDA6.3过程能力审核", "中", "2026-04-15", "已完成");
        jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0010", p10, supplierId, "过程审核", "2026-04-15",
                "孙过程", "质量,SQE", "有条件通过", "84.00", 0, "过程审核有条件通过,无严重不符合项", "已完成");
        }

        // 11. 专项审核(我方发起, 进行中)
        if (!hasAuditPlan("AUD-2026-0011")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0011", supplierId, "专项审核",
                "2026-07-22", "周专项", "质量", "针对RoHS限用物质的专项合规审核", "高", "进行中");
        }

        // 12. 飞行检查(我方不预先通知, 待执行)
        if (!hasAuditPlan("AUD-2026-0012")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0012", supplierId, "飞行检查",
                "2026-07-25", "吴飞检", "质量,SQE", "不预先通知的突击现场飞行检查", "高", "待执行");
        }

        // 13. 初次审核(新供应商首轮, 已完成, 含记录+不符合项)
        if (!hasAuditPlan("AUD-2026-0013")) {
        String p13 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0013", supplierId, "初次审核",
                "2026-02-02", "郑初评", "质量,采购", "新供应商准入初次体系审核", "中", "2026-02-09", "已完成");
        String r13 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0013", p13, supplierId, "初次审核", "2026-02-09",
                "郑初评", "质量,采购", "通过", "86.50", 1, "初次审核通过,具备合格供应商准入条件", "已完成");
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_nc (org_id, nc_no, record_id, supplier_id, clause, description, level, status, responsible, deadline, rectify_measure, rectify_date, verify_result, verify_comment, verify_date, verify_by, close_date) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?::date, ?, ?::timestamptz, ?, ?, ?::timestamptz, ?, ?::timestamptz)",
                orgId, "NC-2026-0013", r13, supplierId, "6.1 策划", "质量手册部分条款待补充", "一般", "已关闭", "供应商质量", "2026-02-20",
                "已补发质量手册补充章节", "2026-02-16 09:00:00", "通过", "整改合格", "2026-02-18 10:00:00", "郑初评", "2026-02-18 10:30:00");
        }

        // 14. 附加审核(客户要求追加, 计划中)
        if (!hasAuditPlan("AUD-2026-0014")) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?)",
                orgId, "AUD-2026-0014", supplierId, "附加审核",
                "2026-08-01", "王质量", "质量", "客户追加的碳足迹合规附加审核", "中", "计划中");
        }

        // 15. 重新审核(整改后复评, 已完成, 含记录)
        if (!hasAuditPlan("AUD-2026-0015")) {
        String p15 = jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_plan (org_id, plan_no, supplier_id, audit_type, plan_date, audit_lead, auditor_team, scope, risk_level, actual_date, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?, ?::date, ?, ?, ?, ?, ?::date, ?) RETURNING id",
                String.class, orgId, "AUD-2026-0015", supplierId, "重新审核",
                "2026-05-02", "冯复评", "质量,SQE", "重大异常整改后重新审核确认", "高", "2026-05-08", "已完成");
        jdbcTemplate.queryForObject(
                "INSERT INTO ops.sqm_audit_record (org_id, record_no, plan_id, supplier_id, audit_type, audit_date, audit_lead, auditor_team, result, score, nc_count, conclusion, status) " +
                "VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::date, ?, ?, ?, ?::numeric, ?, ?, ?) RETURNING id",
                String.class, orgId, "REC-2026-0015", p15, supplierId, "重新审核", "2026-05-08",
                "冯复评", "质量,SQE", "通过", "90.00", 0, "重新审核通过,可恢复批量供货", "已完成");
        }
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
