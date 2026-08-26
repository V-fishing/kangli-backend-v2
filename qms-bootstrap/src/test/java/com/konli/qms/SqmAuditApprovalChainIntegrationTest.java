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
 * 审核会签链集成测试（T6 / IT-6）。
 *
 * <p>覆盖测试计划 §4.2 IT-6「审核会签链」：</p>
 * <ul>
 *   <li>完整闭环：配置会签人(质量 veto + 采购 + 研发) → 建计划惰性生成会签链 → 逐人签批 → 全部 done → start 推进「进行中」</li>
 *   <li>OR 语义：节点绑定 approver_id 时非指定审批人签批被拒(403)，改为包含当前用户后放行</li>
 *   <li>一票否决：veto 节点否决(rejected)后即使其余节点全签，startPlan 仍 409</li>
 *   <li>重复签批：同一节点二次签批 409</li>
 * </ul>
 *
 * <p>依赖：qms_test 库 Flyway 全量迁移；admin/123456 登录(dataScope=all 放行 DataScopeGuard)。
 * 会签配置用 JdbcTemplate 直写 sqm_audit_approval_cfg(org_id 可空,按 audit_type 匹配)，
 * 测试用独立 audit_type「IT会签」避免污染种子配置。</p>
 */
class SqmAuditApprovalChainIntegrationTest extends QmsIntegrationBase {

    /** 测试专用审核类型(不与种子配置冲突)。 */
    private static final String AUDIT_TYPE = "IT会签";

    @Autowired
    TestRestTemplate rest;
    @Autowired
    JdbcTemplate jdbcTemplate;
    ObjectMapper om = new ObjectMapper();

    /** 清理上一轮测试残留:会签节点 → 计划 → 会签配置(保证用例间隔离)。 */
    @BeforeEach
    void cleanResidue() {
        jdbcTemplate.update("DELETE FROM ops.sqm_audit_approval WHERE audit_id IN "
                + "(SELECT id FROM ops.sqm_audit_plan WHERE audit_type = ?)", AUDIT_TYPE);
        jdbcTemplate.update("DELETE FROM ops.sqm_audit_plan WHERE audit_type = ?", AUDIT_TYPE);
        jdbcTemplate.update("DELETE FROM ops.sqm_audit_approval_cfg WHERE audit_type = ?", AUDIT_TYPE);
    }

    // ============ 用例 1:完整会签链闭环(OR 语义 + 全部签完推进) ============

    @Test
    @DisplayName("会签链闭环:配置质量(veto)+采购+研发 → 非指定人403 → 逐签通过 → 全部done → start 200 进行中")
    void approvalChain_fullClosedLoop() throws Exception {
        String token = login();
        String adminId = adminUserId();
        String supplierId = insertSupplier();

        // 配置会签人:质量(veto)+采购+研发;研发指定为不存在用户,用于验证 OR 语义 403
        setCfg("[{\"role\":\"zhiliang\",\"label\":\"质量\",\"veto\":true,\"userId\":\"" + adminId + "\",\"userIds\":[\"" + adminId + "\"]},"
                + "{\"role\":\"caigou\",\"label\":\"采购\",\"veto\":false,\"userId\":\"" + adminId + "\",\"userIds\":[\"" + adminId + "\"]},"
                + "{\"role\":\"yanfa\",\"label\":\"研发\",\"veto\":false,\"userId\":\"nonexistent-user\",\"userIds\":[\"nonexistent-user\"]}]");

        // 建计划 → 惰性生成会签链
        JsonNode plan = createPlan(token, supplierId);
        String planId = plan.get("id").asText();
        assertThat(plan.get("status").asText()).isEqualTo("待执行");

        JsonNode approvals = listApprovals(token, planId);
        assertThat(approvals.size()).isEqualTo(3);
        JsonNode quality = findApproval(approvals, "zhiliang_0");
        assertThat(quality.get("hasVeto").asBoolean()).isTrue();
        assertThat(quality.get("status").asText()).isEqualTo("pending");

        // OR 语义:研发节点指定了不存在用户 → admin 签批被拒
        ResponseEntity<String> denied = approve(token, planId, "yanfa_2", true, "同意");
        JsonNode deniedBody = om.readTree(denied.getBody());
        assertThat(deniedBody.get("code").asInt()).isNotZero();
        assertThat(deniedBody.get("msg").asText()).contains("仅指定审批人");

        // 把研发节点审批人改为 admin(逗号分隔多选,OR 语义任一命中) → 放行
        jdbcTemplate.update("UPDATE ops.sqm_audit_approval SET approver_id = ? "
                + "WHERE audit_id = ? AND approval_role = 'yanfa_2'", adminId, planId);
        ResponseEntity<String> okYanfa = approve(token, planId, "yanfa_2", true, "同意");
        assertThat(om.readTree(okYanfa.getBody()).get("code").asInt())
                .as("研发节点二次签批返回 body: %s", okYanfa.getBody())
                .isZero();

        // 质量、采购签通过
        approve(token, planId, "zhiliang_0", true, "同意");
        approve(token, planId, "caigou_1", true, "同意");

        // 全部 done → start 200 → 状态推进「进行中」
        ResponseEntity<String> start = startPlan(token, planId);
        assertThat(start.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode started = om.readTree(start.getBody()).get("data");
        assertThat(started.get("status").asText()).isEqualTo("进行中");
    }

    // ============ 用例 2:一票否决 ============

    @Test
    @DisplayName("一票否决:质量节点否决 → 其余全签仍 start 409,计划保持待执行")
    void approvalChain_vetoBlocksStart() throws Exception {
        String token = login();
        String adminId = adminUserId();
        String supplierId = insertSupplier();

        setCfg("[{\"role\":\"zhiliang\",\"label\":\"质量\",\"veto\":true,\"userId\":\"" + adminId + "\",\"userIds\":[\"" + adminId + "\"]},"
                + "{\"role\":\"caigou\",\"label\":\"采购\",\"veto\":false,\"userId\":\"" + adminId + "\",\"userIds\":[\"" + adminId + "\"]}]");

        JsonNode plan = createPlan(token, supplierId);
        String planId = plan.get("id").asText();

        // 质量节点否决
        ResponseEntity<String> reject = approve(token, planId, "zhiliang_0", false, "不通过");
        assertThat(om.readTree(reject.getBody()).get("code").asInt()).isZero();
        // 采购通过
        approve(token, planId, "caigou_1", true, "同意");

        // 节点状态:质量 rejected、采购 done
        JsonNode approvals = listApprovals(token, planId);
        assertThat(findApproval(approvals, "zhiliang_0").get("status").asText()).isEqualTo("rejected");
        assertThat(findApproval(approvals, "caigou_1").get("status").asText()).isEqualTo("done");

        // start → 409(质量一票否决)
        ResponseEntity<String> start = startPlan(token, planId);
        JsonNode startBody = om.readTree(start.getBody());
        assertThat(startBody.get("code").asInt()).isNotZero();
        assertThat(startBody.get("msg").asText()).contains("全部会签通过");

        // 计划仍待执行
        JsonNode planAfter = getPlan(token, planId);
        assertThat(planAfter.get("status").asText()).isEqualTo("待执行");
    }

    // ============ 用例 3:重复签批 ============

    @Test
    @DisplayName("重复签批:同一节点二次签批 409")
    void approvalChain_duplicateApproveRejected() throws Exception {
        String token = login();
        String adminId = adminUserId();
        String supplierId = insertSupplier();

        setCfg("[{\"role\":\"zhiliang\",\"label\":\"质量\",\"veto\":false,\"userId\":\"" + adminId + "\",\"userIds\":[\"" + adminId + "\"]}]");

        JsonNode plan = createPlan(token, supplierId);
        String planId = plan.get("id").asText();

        // 首次签批通过
        ResponseEntity<String> first = approve(token, planId, "zhiliang_0", true, "同意");
        assertThat(om.readTree(first.getBody()).get("code").asInt()).isZero();

        // 二次签批 → 409
        ResponseEntity<String> second = approve(token, planId, "zhiliang_0", true, "再次同意");
        JsonNode secondBody = om.readTree(second.getBody());
        assertThat(secondBody.get("code").asInt()).isNotZero();
        assertThat(secondBody.get("msg").asText()).contains("不可重复");
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

    private String adminUserId() {
        return jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_user WHERE username = 'admin'", String.class);
    }

    private String insertSupplier() {
        String orgId = jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        String supplierId = UUID.randomUUID().toString();
        String suffix = String.valueOf(System.currentTimeMillis());
        String code = "SUP-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_supplier (id, org_id, supplier_no, supplier_code, name, credit_code, "
                        + "category, status, level, created_at, is_deleted, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, '合格', 'B', now(), false, 0)",
                supplierId, orgId, "SUP-IT-" + suffix, code,
                "IT会签测试供应商", "CREDIT-IT-" + suffix, "电子");
        return supplierId;
    }

    /** 直写会签配置(org_id 可空,按 audit_type 匹配),避免走 saveConfig API 的归并口径。 */
    private void setCfg(String auditorsJson) {
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_audit_approval_cfg (id, org_id, audit_type, auditors, created_at, updated_at, is_deleted, version) "
                        + "VALUES (?, NULL, ?, ?, now(), now(), false, 0)",
                UUID.randomUUID().toString(), AUDIT_TYPE, auditorsJson);
    }

    private JsonNode createPlan(String token, String supplierId) throws Exception {
        HttpHeaders headers = authHeaders(token);
        String body = "{\"supplierId\":\"" + supplierId + "\",\"auditType\":\"" + AUDIT_TYPE
                + "\",\"planDate\":\"2026-08-26\",\"auditLead\":\"admin\",\"auditorTeam\":\"质量,采购\"}";
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/sqm/audits/plans", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        assertThat(data.get("planNo").asText()).isNotBlank();
        return data;
    }

    private JsonNode listApprovals(String token, String planId) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/sqm/audits/plans/" + planId + "/approvals",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return om.readTree(resp.getBody()).get("data");
    }

    private ResponseEntity<String> approve(String token, String planId, String role,
                                           boolean approved, String opinion) {
        HttpHeaders headers = authHeaders(token);
        String body = "{\"approvalRole\":\"" + role + "\",\"approved\":" + approved
                + ",\"opinion\":\"" + opinion + "\"}";
        return rest.exchange(
                "/api/v1/sqm/audits/plans/" + planId + "/approvals",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private ResponseEntity<String> startPlan(String token, String planId) {
        HttpHeaders headers = authHeaders(token);
        return rest.exchange(
                "/api/v1/sqm/audits/plans/" + planId + "/start",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private JsonNode getPlan(String token, String planId) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/sqm/audits/plans/" + planId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return om.readTree(resp.getBody()).get("data");
    }

    private JsonNode findApproval(JsonNode approvals, String role) {
        for (JsonNode item : approvals) {
            if (role.equals(item.get("approvalRole").asText())) {
                return item;
            }
        }
        throw new AssertionError("未找到会签节点 " + role);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}