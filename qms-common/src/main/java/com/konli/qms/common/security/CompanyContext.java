package com.konli.qms.common.security;

/**
 * 当前用户上下文(ThreadLocal,代码规范§7.3 RLS 所需)。
 *
 * <p>{@code dataScope}:"all" 表示管理员全量(跨公司);其余为所属 org_id(单公司)。
 * 由 {@link JwtAuthenticationFilter} 从 JWT 解析后注入,请求结束清理。</p>
 */
public final class CompanyContext {

    public record CurrentUser(String userId, String username, String orgId, String dataScope) {}

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /**
     * 超级管理员(可配置)内部切换分公司时的"生效组织"UUID。
     * 由 {@link OrgSwitchFilter} 从请求头 X-Org-Context 解析后注入;非空时,
     * 数据权限拦截器与组织解析器优先按该组织过滤/归属(绕过超管全量)。
     * 仅对拥有切换权限(超管或 system.org.switch)的请求生效。
     */
    private static final ThreadLocal<String> SWITCH_ORG = new ThreadLocal<>();

    private CompanyContext() {}

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
        SWITCH_ORG.remove();
    }

    /** 设置切换后的生效组织 UUID(空串/ALL 视为清空,回到全局视图)。 */
    public static void setSwitchOrgId(String id) {
        if (id == null || id.isBlank() || "ALL".equalsIgnoreCase(id)) {
            SWITCH_ORG.remove();
        } else {
            SWITCH_ORG.set(id);
        }
    }

    public static String getSwitchOrgId() {
        return SWITCH_ORG.get();
    }

    public static void clearSwitchOrgId() {
        SWITCH_ORG.remove();
    }

    public static boolean isAdmin() {
        CurrentUser u = get();
        return u != null && "all".equals(u.dataScope());
    }
}
