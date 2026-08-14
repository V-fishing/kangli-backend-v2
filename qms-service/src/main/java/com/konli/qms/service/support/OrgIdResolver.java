package com.konli.qms.service.support;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.uop.SysConfigService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 组织 ID 解析器
 *
 * <p>前端在下拉/切换公司后，请求中携带的 orgId 实际上是「组织代码」(如 MZ/SZ/CD)，
 * 而后端 org_id 列是 UUID 类型，直接入库会触发
 * {@code invalid input syntax for type uuid}。该解析器统一处理：
 * <ol>
 *   <li>请求值本身就是合法 UUID → 直接使用；</li>
 *   <li>请求值是组织代码(如 MZ) → 反查 sys_org 得到真实 UUID；</li>
 *   <li>兜底：使用当前登录用户上下文中的 orgId（仅当为合法 UUID）；</li>
 *   <li>最终兜底：默认梅州(MZ)组织，保证集团管理员未选公司时也能落库。</li>
 * </ol>
 */
@Component
public class OrgIdResolver {

    private static final Pattern UUID_RE = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final JdbcTemplate jdbcTemplate;
    private final SysConfigService sysConfigService;

    public OrgIdResolver(JdbcTemplate jdbcTemplate, SysConfigService sysConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.sysConfigService = sysConfigService;
    }

    public String resolve(String reqOrgId) {
        // 组织切换:超管/有权限者切换分公司且系统开关开启时,新建数据归属所选组织
        String switchOrg = CompanyContext.getSwitchOrgId();
        if (switchOrg != null && UUID_RE.matcher(switchOrg).matches()
                && sysConfigService.getBool("org.switch.affectsWrite", false)) {
            return switchOrg;
        }
        // 普通用户:强制使用上下文 orgId,忽略前端传入(防跨公司伪造 org_id 写入别公司数据)
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && !CompanyContext.isAdmin()
                && u.orgId() != null && !"ROOT".equals(u.orgId())
                && UUID_RE.matcher(u.orgId()).matches()) {
            return u.orgId();
        }
        // 管理员(dataScope=all)/无上下文:走原有解析逻辑(接受前端 org_code/UUID)
        if (reqOrgId != null && UUID_RE.matcher(reqOrgId).matches()) {
            return reqOrgId;
        }
        if (reqOrgId != null && !reqOrgId.isBlank()) {
            String id = queryByCode(reqOrgId);
            if (id != null) {
                return id;
            }
        }
        String ctx = (u != null) ? u.orgId() : null;
        if (ctx != null && !"ROOT".equals(ctx) && UUID_RE.matcher(ctx).matches()) {
            return ctx;
        }
        // 最终兜底：默认梅州
        return queryByCode("MZ");
    }

    /**
     * 查询专用解析：admin 不传 orgId 时返回 null（不限组织）。
     * 与 {@link #resolve} 不同，此方法不会回退到默认 MZ，
     * 适用于 searchNodes / listRoots 等查询场景，避免 admin 因未传 orgId 而被限制为 MZ 看不到其他组织数据。
     */
    public String resolveForQuery(String reqOrgId) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        // 普通用户:强制使用上下文 orgId
        if (u != null && !CompanyContext.isAdmin()
                && u.orgId() != null && !"ROOT".equals(u.orgId())
                && UUID_RE.matcher(u.orgId()).matches()) {
            return u.orgId();
        }
        // 管理员(dataScope=all): 未传 orgId 时返回 null(不限), 传了则按代码/UUID 解析
        if (reqOrgId == null || reqOrgId.isBlank()) {
            return null;
        }
        if (UUID_RE.matcher(reqOrgId).matches()) {
            return reqOrgId;
        }
        return queryByCode(reqOrgId);
    }

    private String queryByCode(String orgCode) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code = ? LIMIT 1", String.class, orgCode);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 由组织代码(orgCode, 如 MZ/SZ)反查其 plant_code(工厂编码)。
     * 三源表(MES 导入)无 org_id(UUID), 仅有 plant_code 文本列, 列表查询需按 plant_code 做组织隔离。
     * 入参可为组织代码或 UUID: UUID 时先反查 org_code 再取 plant_code; 为空时返回 null(不做隔离, 管理员全局视图)。
     */
    public String resolvePlantCode(String reqOrg) {
        if (reqOrg == null || reqOrg.isBlank()) {
            return null;
        }
        String orgCode = reqOrg;
        if (UUID_RE.matcher(reqOrg).matches()) {
            try {
                String code = jdbcTemplate.queryForObject(
                        "SELECT org_code FROM ops.sys_org WHERE id::text = ? LIMIT 1", String.class, reqOrg);
                if (code != null) orgCode = code;
            } catch (Exception ignored) { /* 忽略, 退回原值 */ }
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT plant_code FROM ops.sys_org WHERE org_code = ? LIMIT 1", String.class, orgCode);
        } catch (Exception ignored) {
            return null;
        }
    }
}
