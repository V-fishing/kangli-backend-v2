package com.konli.qms;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证登录集成测试（T2 阶段，真实 Spring 上下文 + Testcontainers PG/Redis）。
 * 验证：登录签发 JWT、带 token 访问受保护接口 200、无 token 401（@PreAuthorize 拦截）。
 * 账号使用 Flyway 种子内置的 admin / 123456。
 */
class AuthLoginIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;

    private ResponseEntity<String> login(String username, String password) {
        Map<String, String> body = Map.of("username", username, "password", password);
        return rest.postForEntity("/api/v1/auth/login", body, String.class);
    }

    @Test
    @DisplayName("登录成功:返回 200 且 data.accessToken 非空")
    void login_success_returnsToken() {
        ResponseEntity<String> resp = login("admin", "123456");
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("accessToken");
        assertThat(resp.getBody()).contains("Bearer");
    }

    @Test
    @DisplayName("错误密码:登录失败(非 200,统一错误响应)")
    void login_wrongPassword_fails() {
        ResponseEntity<String> resp = login("admin", "wrong-pass");
        // 错误密码返回非 200 或 code!=0 的错误响应
        boolean failed = resp.getStatusCode() != HttpStatus.OK
                || !resp.getBody().contains("\"code\":0");
        assertThat(failed).isTrue();
    }

    @Test
    @DisplayName("无 token 访问受保护接口:返回 401 未授权(@PreAuthorize 拦截)")
    void protectedApi_withoutToken_returns401() {
        ResponseEntity<String> resp = rest.getForEntity("/api/v1/uop/users", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("带 token 访问受保护接口:返回 200")
    void protectedApi_withToken_returns200() throws Exception {
        String token = extractToken(login("admin", "123456"));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/uop/users", HttpMethod.GET, entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String extractToken(ResponseEntity<String> loginResp) throws Exception {
        JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(loginResp.getBody());
        JsonNode data = node.get("data");
        return data != null ? data.get("accessToken").asText() : null;
    }
}
