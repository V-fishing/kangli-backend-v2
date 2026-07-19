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

    private CompanyContext() {}

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static boolean isAdmin() {
        CurrentUser u = get();
        return u != null && "all".equals(u.dataScope());
    }
}
