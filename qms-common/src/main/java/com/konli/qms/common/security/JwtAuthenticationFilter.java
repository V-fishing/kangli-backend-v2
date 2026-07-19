package com.konli.qms.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JWT 认证过滤器:解析 token -> 注入 CompanyContext + SecurityContext(含权限码 authorities)。
 *
 * <p>每请求经 {@link PermissionLoader} 加载该用户菜单/按钮权限码,作为 authorities,
 * 供 {@code @PreAuthorize("hasAuthority('xxx')")} 校验。无效 token 不设上下文,交由 Security 返回 401。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PermissionLoader permissionLoader;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(header.substring(7));
                CompanyContext.set(new CompanyContext.CurrentUser(
                        claims.getSubject(),
                        claims.get("username", String.class),
                        claims.get("orgId", String.class),
                        claims.get("dataScope", String.class)));
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                Set<String> codes = permissionLoader.loadPermissionCodes(claims.getSubject());
                for (String code : codes) {
                    authorities.add(new SimpleGrantedAuthority(code));
                }
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                CompanyContext.clear();
                SecurityContextHolder.clearContext();
            }
        }
        try {
            chain.doFilter(req, resp);
        } finally {
            CompanyContext.clear();
        }
    }
}
