package com.konli.qms.service.support;

import com.konli.qms.common.security.CompanyContext;
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

    public OrgIdResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String resolve(String reqOrgId) {
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

    private String queryByCode(String orgCode) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code = ? LIMIT 1", String.class, orgCode);
        } catch (Exception ignored) {
            return null;
        }
    }
}
