package com.konli.qms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 8D 创建业务闭环集成测试（T2 阶段）。
 * 用真实登录 token 调 POST /api/v1/ncm/8d-reports（人工建 8D），验证：
 *   - 返回 200 + d8No 非空
 *   - 统一整改源头：返回的 8D source=不良记录（由 create 先落缺陷记录再发起）
 * 等价于"缺陷记录→8D 双向关联"的端到端验证（UT 层 Ncm8dServiceImplCreateTest 已单元验证，此处验证接口贯通）。
 */
class Ncm8dCreateIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;
    ObjectMapper om = new ObjectMapper();

    private String token() throws Exception {
        ResponseEntity<String> resp = rest.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "123456"),
                String.class);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        return data.get("accessToken").asText();
    }

    @Test
    @DisplayName("人工建 8D:带 token 创建成功,返回 d8No 且 source=不良记录(统一整改源头)")
    void create_manual8d_linksDefectRecord() throws Exception {
        String token = token();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"flowType\":\"8D\",\"source\":\"人工\",\"issue\":\"集成测试-人工建8D\",\"severity\":\"高\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/8d-reports", HttpMethod.POST, entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode data = om.readTree(resp.getBody()).get("data");
        assertThat(data.get("d8No").asText()).isNotBlank();
        // create() 先落缺陷记录再从缺陷记录发起 8D → 返回 report.source=不良记录
        assertThat(data.get("source").asText()).isEqualTo("不良记录");
        assertThat(data.get("currentStage").asText()).isNotBlank();
    }

    @Test
    @DisplayName("无 token 创建 8D:返回 401(@PreAuthorize 拦截)")
    void create_withoutToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/8d-reports", HttpMethod.POST, entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
