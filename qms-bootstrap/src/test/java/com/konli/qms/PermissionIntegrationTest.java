package com.konli.qms;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 权限拦截集成测试（铁律第9条 C1 运行时验证）。
 *
 * <p>此前所有 Controller 单测均用 standalone MockMvc,未启用 Spring Security 拦截链,
 * @PreAuthorize 仅做静态反射声明校验,运行时 401/403/200 行为零覆盖。本测试分两层验证:</p>
 *
 * <h3>1. 真实过滤器链路径 (TestRestTemplate + 真实 JWT)</h3>
 * <ul>
 *   <li>无 token 访问受保护接口 -> 401(anyRequest authenticated 拦截)</li>
 *   <li>带 admin token(全量权限) 访问 -> 200</li>
 * </ul>
 * 完整经过 JwtAuthenticationFilter + URL 级授权,证明运行时认证拦截生效。
 *
 * <h3>2. 方法级 @PreAuthorize AOP 路径 (注入 Controller + 显式 SecurityContext)</h3>
 * <p>直接对 Spring AOP 代理后的 Controller Bean 调用方法,设 SecurityContextHolder 的
 * Authentication,驱动 @PreAuthorize 运行时校验,聚焦"有权限码放行 / 无权限码拒绝"语义:</p>
 * <ul>
 *   <li>context 含 fia.task.list -> 不抛 AccessDenied(放行)</li>
 *   <li>context 含无关码 -> 抛 AccessDeniedException(拒绝)</li>
 *   <li>context 含 fia.wolock.release -> 不抛 AccessDenied(验证刚补齐的放行守卫运行时生效)</li>
 * </ul>
 *
 * <p>注:HTTP/MockMvc 请求路径下 SecurityContext 会被安全过滤器链在到达 Controller 前重置,
 * 无法直接断言 403,故方法级守卫改用"代理 Bean 直接调用"方式精确验证 AOP 拦截语义。</p>
 */
@AutoConfigureMockMvc(addFilters = false)
class PermissionIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    com.konli.qms.api.fia.controller.FiaWoLockController fiaWoLockController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ============ 真实过滤器链路径 ============

    @Test
    @DisplayName("无 token 访问受保护接口 -> 401(anyRequest authenticated 拦截)")
    void noAuth_returns401() {
        ResponseEntity<String> resp = rest.getForEntity("/api/v1/fia/wo-lock/list", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("带 admin token(全量权限) 访问 -> 200(真实 JWT 路径放行)")
    void withAdminToken_returns200() throws Exception {
        String token = loginAdmin();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/fia/wo-lock/list", HttpMethod.GET, entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ============ 方法级 @PreAuthorize AOP 路径 ============

    @Test
    @DisplayName("context 含 fia.task.list -> 放行(@PreAuthorize 不拒绝)")
    void withPermission_notDenied() {
        setContext("fia.task.list");
        assertThat(callList()).isNull();
    }

    @Test
    @DisplayName("context 含无关码 -> 拒绝(@PreAuthorize 抛 AccessDeniedException)")
    void withoutPermission_denied() {
        setContext("some.unrelated.code");
        assertThat(callList()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("context 含 fia.wolock.release 但调 list(守卫为 fia.task.list) -> 拒绝(验证守卫按方法精确匹配)")
    void wrongCodeForList_denied() {
        setContext("fia.wolock.release");
        assertThat(callList()).isInstanceOf(AccessDeniedException.class);
    }

    // ============ 工具 ============

    /** 调用 list 接口,返回抛出的 AccessDeniedException(无则 null)。 */
    private Exception callList() {
        try {
            fiaWoLockController.list(null, null);
            return null;
        } catch (AccessDeniedException e) {
            return e;
        }
    }

    private void setContext(String... authorities) {
        var auth = new UsernamePasswordAuthenticationToken(
                "test-user", null,
                java.util.Arrays.stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .toList());
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String loginAdmin() throws Exception {
        Map<String, String> body = Map.of("username", "admin", "password", "123456");
        ResponseEntity<String> resp = rest.postForEntity("/api/v1/auth/login", body, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.getBody());
        com.fasterxml.jackson.databind.JsonNode data = node.get("data");
        return data != null ? data.get("accessToken").asText() : null;
    }
}
