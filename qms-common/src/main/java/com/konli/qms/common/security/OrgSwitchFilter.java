package com.konli.qms.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 组织切换过滤器:在 {@link JwtAuthenticationFilter} 之后执行,解析请求头 {@code X-Org-Context},
 * 将"生效组织"写入 {@link CompanyContext#setSwitchOrgId},使数据权限拦截器与组织解析器按所选分公司过滤/归属。
 *
 * <p>仅对超管(dataScope=all)或拥有 {@code system.org.switch} 权限的用户生效;无切换头或值为 ALL 时清空,
 * 回到全局视图。普通用户即使携带头部也会被忽略(防伪造)。</p>
 *
 * <p>头取值:组织 UUID 或组织代码(如 MZ/SZ),由本过滤器统一解析为 UUID;{@code ALL}/空 = 全局视图。</p>
 */
@Component
@RequiredArgsConstructor
public class OrgSwitchFilter extends OncePerRequestFilter {

    private static final Pattern UUID_RE = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final Set<String> SWITCH_AUTHORITIES = Set.of("system.org.switch");

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        try {
            // 仅当 JWT 已注入用户上下文(即已通过 JwtAuthenticationFilter)时才处理;
            // 作为 servlet 容器级过滤器先于 Security 链执行时 CompanyContext 为空,此处安全跳过。
            if (CompanyContext.get() != null && isAllowed()) {
                String header = req.getHeader("X-Org-Context");
                if (header != null && !header.isBlank() && !"ALL".equalsIgnoreCase(header.trim())) {
                    String resolved = resolveOrg(header.trim());
                    if (resolved != null) {
                        CompanyContext.setSwitchOrgId(resolved);
                    }
                }
            }
        } catch (Exception ignored) {
            // 切换失败不影响主流程,保持全局视图
        }
        chain.doFilter(req, resp);
    }

    private boolean isAllowed() {
        if (CompanyContext.isAdmin()) return true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (SWITCH_AUTHORITIES.contains(ga.getAuthority())) return true;
        }
        return false;
    }

    private String resolveOrg(String val) {
        if (UUID_RE.matcher(val).matches()) return val;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code = ? LIMIT 1", String.class, val);
        } catch (Exception e) {
            return null;
        }
    }
}
