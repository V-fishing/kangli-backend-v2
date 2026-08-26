package com.konli.qms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 8D 阶段签批闭环集成测试（T6 / IT-5）。
 *
 * <p>覆盖测试计划 §4.2 IT-5「8D 签批 OR 语义」：</p>
 * <ul>
 *   <li>完整签批闭环：建 8D → 推进 D1(需审批) → 错误口令被拒 → 正确口令通过 → 进入 D2</li>
 *   <li>指定签批人 OR 语义：审核配置 signer 为「非当前用户」时拒绝；改为包含当前用户后放行
 *       （isAmongSigners 支持逗号分隔 userId/username，任一命中即可签）</li>
 * </ul>
 *
 * <p>依赖：qms_test 库 Flyway 全量迁移（V021 预置 ROOT/MZ/SZ 的 8D 审核配置，
 * 未配置组织回退默认 D1/D3/D5/D7 需审批）；admin/123456 登录。</p>
 */
class Ncm8dApproveIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;
    @Autowired
    JdbcTemplate jdbcTemplate;
    ObjectMapper om = new ObjectMapper();

    /** 清理上一轮测试残留的 MZ 组织 D1 审核配置(避免 signer 残留影响用例间隔离)。 */
    @BeforeEach
    void cleanApprovalConfig() {
        jdbcTemplate.update("DELETE FROM ops.qms_8d_approval_config WHERE org_id <> 'ROOT' AND stage_code = 'D1'");
    }

    // ============ 用例 1:完整签批闭环 ============

    @Test
    @DisplayName("8D 签批闭环:推进D1→错误口令被拒→正确口令通过→进入D2")
    void approve_fullClosedLoop() throws Exception {
        String token = login();
        JsonNode created = create8d(token, "IT-5 签批闭环");
        String id = created.get("id").asText();
        assertThat(created.get("currentStage").asText()).isEqualTo("D1");

        // 推进 D1(默认需审批 → 停留 D1 待审批)
        ResponseEntity<String> adv = advance(token, id, "D1");
        assertThat(adv.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(om.readTree(adv.getBody()).get("code").asInt()).isZero();

        // 错误口令 → 400 签名口令错误
        ResponseEntity<String> bad = approve(token, id, "D1", true, "同意", "wrong-pass");
        JsonNode badBody = om.readTree(bad.getBody());
        assertThat(badBody.get("code").asInt()).isNotZero();
        assertThat(badBody.get("msg").asText()).contains("口令");

        // 正确口令 → 通过,进入 D2
        ResponseEntity<String> ok = approve(token, id, "D1", true, "同意", "123456");
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(om.readTree(ok.getBody()).get("code").asInt()).isZero();

        JsonNode detail = get8d(token, id);
        assertThat(detail.get("report").get("currentStage").asText()).isEqualTo("D2");
    }

    // ============ 用例 2:指定签批人 OR 语义 ============

    @Test
    @DisplayName("8D 签批 OR 语义:signer 非当前用户被拒,改为包含当前用户后放行")
    void approve_signerOrSemantics() throws Exception {
        String token = login();
        JsonNode created = create8d(token, "IT-5 OR语义");
        String id = created.get("id").asText();
        String orgId = created.get("orgId").asText();
        assertThat(orgId).isNotBlank();

        advance(token, id, "D1");

        // 指定签批人为不存在的用户 → admin 无签名权限
        setSigner(orgId, "D1", "nonexistent-user");
        ResponseEntity<String> denied = approve(token, id, "D1", true, "同意", "123456");
        JsonNode deniedBody = om.readTree(denied.getBody());
        assertThat(deniedBody.get("code").asInt()).isNotZero();
        assertThat(deniedBody.get("msg").asText()).contains("无签名权限");

        // 签批人改为包含 admin(逗号分隔多选,OR 语义) → 放行
        setSigner(orgId, "D1", "someone-else,admin");
        ResponseEntity<String> ok = approve(token, id, "D1", true, "同意", "123456");
        assertThat(om.readTree(ok.getBody()).get("code").asInt())
                .as("第二个 approve 返回 body: %s", ok.getBody())
                .isZero();

        JsonNode detail = get8d(token, id);
        assertThat(detail.get("report").get("currentStage").asText()).isEqualTo("D2");
    }

    // ============ 工具 ============

    private String login() throws Exception {
        ResponseEntity<String> resp = rest.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "123456"),
                String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        return data.get("accessToken").asText();
    }

    private JsonNode create8d(String token, String issue) throws Exception {
        HttpHeaders headers = authHeaders(token);
        String body = "{\"flowType\":\"8D\",\"source\":\"人工\",\"issue\":\"" + issue + "\",\"severity\":\"中\"}";
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/8d-reports", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        assertThat(data.get("d8No").asText()).isNotBlank();
        return data;
    }

    private ResponseEntity<String> advance(String token, String id, String stageCode) {
        HttpHeaders headers = authHeaders(token);
        String body = "{\"stageCode\":\"" + stageCode + "\",\"content\":\"IT 阶段内容\",\"owner\":\"admin\"}";
        return rest.exchange(
                "/api/v1/ncm/8d-reports/" + id + "/advance",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private ResponseEntity<String> approve(String token, String id, String stageCode,
                                           boolean approved, String comment, String password) {
        HttpHeaders headers = authHeaders(token);
        String body = "{\"stageCode\":\"" + stageCode + "\",\"approved\":" + approved
                + ",\"comment\":\"" + comment + "\",\"password\":\"" + password + "\"}";
        return rest.exchange(
                "/api/v1/ncm/8d-reports/" + id + "/approve",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private JsonNode get8d(String token, String id) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/8d-reports/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return om.readTree(resp.getBody()).get("data");
    }

    /** 直接写审核配置(按报告归属组织),避免走 saveConfig API 的 ROOT 组织归并口径。 */
    private void setSigner(String orgId, String stageCode, String signer) {
        jdbcTemplate.update(
                "INSERT INTO ops.qms_8d_approval_config (id, org_id, stage_code, need_approval, signer, sort_order) "
                        + "VALUES (?, ?, ?, true, ?, ?) "
                        + "ON CONFLICT (org_id, stage_code) DO UPDATE SET signer = EXCLUDED.signer, need_approval = true",
                UUID.randomUUID().toString(), orgId, stageCode, signer, 1);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}