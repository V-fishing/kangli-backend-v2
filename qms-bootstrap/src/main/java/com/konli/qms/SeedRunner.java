package com.konli.qms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 17 账号种子（与前端 mock 角色严格对齐，幂等）。
 *
 * <p>两个公司(SZ/MZ) × 8 角色 + 1 跨公司管理员 = 17 个预设账号。
 * 管理员可随时通过 UOP 管理界面调整角色权限（菜单/按钮分配）。
 *
 * <pre>
 * 角色          MZ 账号          SZ 账号          说明
 * ──────────    ────────        ────────         ──────────
 * 操作工        mz.operator     sz.operator      产线操作,自检录入
 * 生产检验员    mz.inspector    sz.inspector     首件/来料检验,不良录入
 * 班组长        mz.shiftleader  sz.shiftleader   产线管理,报警确认
 * 质量工程师    mz.qe           sz.qe            质量分析,SPC,8D
 * SQE           mz.sqe          sz.sqe           供应商审核,来料异常
 * 质量经理      mz.qmanager     sz.qmanager      审批,跨模块全量
 * 采购员        mz.purchaser    sz.purchaser     采购订单,供应商准入
 * 管理员        mz.admin        sz.admin         公司内用户/角色管理
 *
 * 集团管理员: admin (跨公司, dataScope=all)
 * </pre>
 *
 * <p>密码统一: 123456。orgId=null → dataScope=all(跨公司)。</p>
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    /** 8 角色定义(code, name, desc) */
    private static final String[][] ROLE_DEFS = {
        {"operator",    "操作工",     "产线操作 · 自检数据录入"},
        {"inspector",   "生产检验员", "首件/来料检验 · 不良录入 · 器具使用"},
        {"shiftleader", "班组长",     "产线管理 · 报警确认关闭 · 工装状态"},
        {"qe",          "质量工程师", "质量分析 · SPC · 8D整改 · CAPA"},
        {"sqe",         "SQE",        "供应商审核 · 来料异常处置 · 整改验证"},
        {"qmanager",    "质量经理",   "审批授权 · 趋势分析 · 绩效评审"},
        {"purchaser",   "采购员",     "采购订单 · 供应商准入 · 物料变更"},
        {"rd",          "研发工程师", "物料变更研发审批 · 工艺/验证评估"},
        {"admin",       "管理员",     "公司内用户/角色管理 · 基础数据维护"},
    };

    /** 17 账号: (username, realName, orgCode, roleCode) — orgCode 为 null → 跨公司 */
    private static final Object[][] SEED_USERS = {
        // ── 梅州 MZ ──
        {"mz.operator",   "MZ-操作工",     "MZ", "operator"},
        {"mz.inspector",  "MZ-检验员",     "MZ", "inspector"},
        {"mz.shiftleader","MZ-班组长",     "MZ", "shiftleader"},
        {"mz.qe",         "MZ-质量工程师", "MZ", "qe"},
        {"mz.sqe",        "MZ-SQE",        "MZ", "sqe"},
        {"mz.qmanager",   "MZ-质量经理",   "MZ", "qmanager"},
        {"mz.purchaser",  "MZ-采购员",     "MZ", "purchaser"},
        {"mz.admin",      "MZ-管理员",     "MZ", "admin"},
        // ── 深圳 SZ ──
        {"sz.operator",   "SZ-操作工",     "SZ", "operator"},
        {"sz.inspector",  "SZ-检验员",     "SZ", "inspector"},
        {"sz.shiftleader","SZ-班组长",     "SZ", "shiftleader"},
        {"sz.qe",         "SZ-质量工程师", "SZ", "qe"},
        {"sz.sqe",        "SZ-SQE",        "SZ", "sqe"},
        {"sz.qmanager",   "SZ-质量经理",   "SZ", "qmanager"},
        {"sz.purchaser",  "SZ-采购员",     "SZ", "purchaser"},
        {"sz.admin",      "SZ-管理员",     "SZ", "admin"},
        {"mz.rd",         "MZ-研发工程师",  "MZ", "rd"},
        {"sz.rd",         "SZ-研发工程师",  "SZ", "rd"},
        // ── 集团跨公司 ──
        {"admin",         "集团管理员",    null, "admin"},
    };

    @Override
    public void run(ApplicationArguments args) {
        ensureRoles();
        ensureOrgs();
        ensureUsers();
        ensureNotifications();
    }

    private void ensureRoles() {
        for (String[] def : ROLE_DEFS) {
            String code = def[0], name = def[1], desc = def[2];
            Long cnt = jdbc.queryForObject(
                "SELECT count(*) FROM ops.sys_role WHERE role_code = ?", Long.class, code);
            if (cnt == null || cnt == 0) {
                jdbc.update(
                    "INSERT INTO ops.sys_role (role_code, role_name, role_type, perm_desc, status, is_deleted, version) "
                    + "VALUES (?, ?, '预置', ?, '启用', false, 1)",
                    code, name, desc);
                log.info("[SeedRunner] 写入角色: {} ({})", code, name);
            }
        }
    }

    private void ensureOrgs() {
        Long c = jdbc.queryForObject("SELECT count(*) FROM ops.sys_org", Long.class);
        if (c != null && c > 0) return;
        jdbc.update("INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status) VALUES ('MZ','梅州分公司',1,'公司','启用')");
        jdbc.update("INSERT INTO ops.sys_org (org_code, org_name, sort_order, org_type, status) VALUES ('SZ','深圳分公司',2,'公司','启用')");
        log.info("[SeedRunner] 写入公司: 梅州/深圳");
    }

    private void ensureUsers() {
        for (Object[] def : SEED_USERS) {
            String username = (String) def[0], realName = (String) def[1];
            String orgCode  = (String) def[2], roleCode = (String) def[3];

            // orgId 解析: orgCode 为 null → 跨公司
            String orgId = null;
            if (orgCode != null) {
                orgId = jdbc.queryForObject(
                    "SELECT id FROM ops.sys_org WHERE org_code = ?", String.class, orgCode);
            }

            // 用户: 不存在则创建, 存在则更新密码+orgId+状态
            SysUser existing = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (existing != null) {
                existing.setPasswordHash(passwordEncoder.encode("123456"));
                existing.setRealName(realName);
                existing.setOrgId(orgId);
                existing.setStatus("启用");
                userMapper.updateById(existing);
            } else {
                SysUser u = new SysUser();
                u.setUsername(username);
                u.setRealName(realName);
                u.setPasswordHash(passwordEncoder.encode("123456"));
                u.setOrgId(orgId);
                u.setStatus("启用");
                userMapper.insert(u);
            }

            // 角色分配: 确保用户拥有对应角色
            String userId = jdbc.queryForObject(
                "SELECT id FROM ops.sys_user WHERE username = ?", String.class, username);
            String roleId = jdbc.queryForObject(
                "SELECT id FROM ops.sys_role WHERE role_code = ?", String.class, roleCode);
            if (userId != null && roleId != null) {
                Long existsRel = jdbc.queryForObject(
                    "SELECT count(*) FROM ops.sys_user_role WHERE user_id = ?::uuid AND role_id = ?::uuid",
                    Long.class, userId, roleId);
                if (existsRel == null || existsRel == 0) {
                    jdbc.update(
                        "INSERT INTO ops.sys_user_role (user_id, role_id) VALUES (?::uuid, ?::uuid)",
                        userId, roleId);
                }
            }
        }
        log.info("[SeedRunner] 17 账号就绪: 8(MZ) + 8(SZ) + 1(集团 admin), 密码 123456");
    }

    /**
     * 为各账号按角色/公司写入相关示例消息,使前端头像左侧铃铛可接收全部相关消息。
     * <p>user_id 取 sys_user.id(与 JWT sub / NotificationService.listMine 过滤一致),
     * org_id 取自该用户所属组织。幂等: 表非空则不重复写入。</p>
     */
    private void ensureNotifications() {
        Long c = jdbc.queryForObject("SELECT count(*) FROM ops.sys_notification", Long.class);
        if (c != null && c > 0) return;
        int total = 0;
        for (Object[] def : SEED_USERS) {
            String username = (String) def[0], realName = (String) def[1];
            String orgCode = (String) def[2], roleCode = (String) def[3];
            String userId = jdbc.queryForObject(
                "SELECT id FROM ops.sys_user WHERE username = ?", String.class, username);
            String orgId = jdbc.queryForObject(
                "SELECT org_id FROM ops.sys_user WHERE username = ?", String.class, username);
            if (userId == null) continue;
            String orgName = orgCode == null ? "集团" : ("MZ".equals(orgCode) ? "梅州" : "深圳");
            for (Msg m : buildMessages(roleCode, orgCode, orgName)) {
                jdbc.update(
                    "INSERT INTO ops.sys_notification (user_id, user_name, org_id, biz_type, title, content, link, is_read) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, false)",
                    userId, realName, orgId, m.type, m.title, m.content, m.link);
                total++;
            }
        }
        log.info("[SeedRunner] 消息通知种子写入完成, 共 {} 条", total);
    }

    /** 按角色 + 公司生成相关示例消息(标题/内容/跳转路由) */
    private List<Msg> buildMessages(String roleCode, String orgCode, String orgName) {
        List<Msg> list = new ArrayList<>();
        String org = orgName + "分公司";
        switch (roleCode) {
            case "operator":
                list.add(new Msg("FIA", "首件检验任务指派", org + " · WO-260724-012 精密轴承座 BRG-440 待您完成首件送检与自检", "/fia/tasks"));
                list.add(new Msg("FIA", "自检提醒", org + " A线·注塑3号机 今日需完成 5 笔首件自检", "/fia/tasks"));
                list.add(new Msg("FIA", "换模首件确认", "换模具后首件确认提醒:连接器外壳 CN-56", "/fia/tasks"));
                break;
            case "inspector":
                list.add(new Msg("FIA", "首件检验任务待处理", org + " · WO-260724-008 液压阀体 HV-220 待首件检验", "/fia/tasks"));
                list.add(new Msg("FIA", "不合格待判定", org + " · WO-260724-003 首件不合格,待您判定处置", "/fia/tasks"));
                list.add(new Msg("NCM", "不良录入提醒", "产线报工不良 2 笔待不良登记", "/ncm/defect-records"));
                break;
            case "shiftleader":
                list.add(new Msg("FIA", "工单锁定待处置", org + " · WO-260724-003 已锁定 3h12m,等待产线处置决策", "/fia/tasks"));
                list.add(new Msg("FIA", "产线异常告警", org + " C线·装配2号 设备节拍异常,请确认", "/fia/tasks"));
                list.add(new Msg("PATROL", "巡检任务", "今日产线巡检路线待执行", "/patrol/tasks"));
                break;
            case "qe":
                list.add(new Msg("SPC", "控制图越界告警", "BRG-440 内径 Xbar 图第7子组越界 UCL,请分析", "/spc/alarms"));
                list.add(new Msg("NCM", "过程不良待 8D", "注塑工序不良率超阈值,请发起 8D", "/ncm/8d-reports"));
                list.add(new Msg("FIA", "首件异常跟进", org + " · WO-260724-003 不合格待根因分析", "/fia/tasks"));
                break;
            case "sqe":
                list.add(new Msg("SQM", "来料异常待处理", "供应商批次 LOT-2207 来料尺寸超差,待处置", "/sqm/abnormals"));
                list.add(new Msg("SQM", "供应商审核待安排", "供应商 精诚精密 年度审核待排期", "/sqm/audits"));
                list.add(new Msg("SQM", "整改验证", "8D 报告验证待确认", "/sqm/abnormals"));
                break;
            case "qmanager":
                list.add(new Msg("FIA", "处置决策待审批", org + " · WO-260724-003 紧急放行申请待您审批", "/fia/approvals"));
                list.add(new Msg("NCM", "重大不良待评审", "月度重大不良评审会待参加", "/ncm/8d-reports"));
                list.add(new Msg("SQM", "供应商绩效", "供应商季度绩效评审待确认", "/sqm/suppliers"));
                break;
            case "purchaser":
                list.add(new Msg("SQM", "供应商建档待完善", "新供应商 华锐科技 准入资料待补充", "/sqm/suppliers"));
                list.add(new Msg("SQM", "物料变更待发起", "物料 PMMA-02 拟变更,请发起变更", "/sqm/changes"));
                list.add(new Msg("SQM", "采购协同", "采购订单 PO-2026-088 待确认交期", "/sqm/suppliers"));
                break;
            case "rd":
                list.add(new Msg("SQM", "物料/工艺变更待评审", "物料 PMMA-02 变更研发评估待您确认", "/sqm/changes"));
                list.add(new Msg("FIA", "变更影响首件", "工艺变更后首件 BRG-440 待确认", "/fia/tasks"));
                list.add(new Msg("NCM", "设计相关不良", "设计公差评审待参与", "/ncm/8d-reports"));
                break;
            case "admin":
                if (orgCode == null) {
                    list.add(new Msg("DASH", "全公司质量周报", "本周全公司质量周报已生成,跨公司概览可查看", "/dashboard"));
                    list.add(new Msg("ARCH", "跨分公司审计", "跨分公司质量审计记录待查看", "/archive/list"));
                    list.add(new Msg("SYS", "全平台用户治理", "全平台用户/角色待统一治理", "/system/users"));
                } else {
                    list.add(new Msg("SYS", "用户权限配置待复核", org + " 角色权限分配待复核", "/system/roles"));
                    list.add(new Msg("SYS", "组织岗位待维护", org + " 本部门组织岗位待维护", "/system/orgs"));
                    list.add(new Msg("SYS", "菜单配置", "菜单与按钮权限待核对", "/system/menus"));
                }
                break;
            default:
                break;
        }
        return list;
    }

    /** 单条示例消息(业务类型 / 标题 / 内容 / 跳转路由) */
    private static final class Msg {
        final String type, title, content, link;
        Msg(String type, String title, String content, String link) {
            this.type = type;
            this.title = title;
            this.content = content;
            this.link = link;
        }
    }
}
