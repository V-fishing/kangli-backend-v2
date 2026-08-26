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
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 来料异常 → 8D 跨模块闭环集成测试（T6 / IT-4）。
 *
 * <p>覆盖测试计划 §4.2 IT-4「来料异常发起 8D」：</p>
 * <ul>
 *   <li>建来料异常单(level=严重) → 从异常单发起 8D(launchFromAbnormal) 成功</li>
 *   <li>统一整改源头:先落 source=SQM异常 的缺陷记录,再从缺陷记录发起 8D</li>
 *   <li>异常单反向回写:d8Id 关联、rectifyType=8D、status=整改中</li>
 * </ul>
 *
 * <p>sqm_supplier 无迁移种子,测试用 JdbcTemplate 直插一家演示供应商。</p>
 */
class SqmAbnormalLaunchIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;
    @Autowired
    JdbcTemplate jdbcTemplate;
    ObjectMapper om = new ObjectMapper();

    @Test
    @DisplayName("来料异常发起8D:返回8D、缺陷记录回写、异常单反向关联(d8Id/整改中/8D)")
    void abnormal_launch8d_linksBack() throws Exception {
        String token = login();
        String supplierId = insertSupplier();

        // 1. 建来料异常单(严重 → 触发 8D 整改类型)
        HttpHeaders headers = authHeaders(token);
        String abnormalBody = "{\"supplierId\":\"" + supplierId + "\",\"partNo\":\"PN-IT-001\","
                + "\"partName\":\"IT测试物料\",\"description\":\"集成测试来料异常\",\"qty\":2,"
                + "\"level\":\"严重\",\"occurDate\":\"2026-08-26\",\"incomingQty\":100}";
        ResponseEntity<String> abResp = rest.exchange(
                "/api/v1/sqm/abnormals", HttpMethod.POST,
                new HttpEntity<>(abnormalBody, headers), String.class);
        assertThat(abResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode abnormal = om.readTree(abResp.getBody()).get("data");
        System.out.println("=== create 返回 data: " + abnormal.toString());
        String abnormalId = abnormal.get("id").asText();
        String abnormalNo = abnormal.get("abnormalNo").asText();
        assertThat(abnormal.get("status").asText()).isEqualTo("待处理");

        // 2. 从异常单发起 8D
        String launchBody = "{\"report\":{\"sourceRefId\":\"" + abnormalId
                + "\",\"issue\":\"来料异常发起8D-IT\",\"severity\":\"严重\"},\"launch\":{}}";
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/8d-reports/launch", HttpMethod.POST,
                new HttpEntity<>(launchBody, headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        assertThat(data.get("d8No").asText()).isNotBlank();
        assertThat(data.get("source").asText()).isEqualTo("不良记录");
        String defectId = data.get("sourceRefId").asText();
        assertThat(defectId).isNotBlank();

        // 3. 统一整改源头:缺陷记录 source=SQM异常、d8No 回写
        JsonNode def = getDefectRecord(token, defectId);
        assertThat(def.get("source").asText()).isEqualTo("SQM异常");
        assertThat(def.get("d8No").asText()).isEqualTo(data.get("d8No").asText());

        // 4. 异常单反向关联:d8Id 非空、rectifyType=8D、status=整改中
        // 注意:create 返回的 id 为 MyBatis-Plus 生成的去横杠 UUID,列表查询返回 PostgreSQL 规范格式(带横杠),
        // 故用业务编号 abnormalNo 匹配(唯一且不受 UUID 格式影响)。
        JsonNode updated = findAbnormal(token, abnormalNo);
        assertThat(updated.get("d8Id").asText()).isNotBlank();
        assertThat(updated.get("rectifyType").asText()).isEqualTo("8D");
        assertThat(updated.get("status").asText()).isEqualTo("整改中");
    }

    // ============ 工具 ============

    private String insertSupplier() {
        String orgId = jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        String supplierId = UUID.randomUUID().toString();
        String suffix = String.valueOf(System.currentTimeMillis());
        // supplier_code 为 VARCHAR(16) 唯一约束,用 UUID 前 8 位保证短且唯一
        String code = "SUP-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_supplier (id, org_id, supplier_no, supplier_code, name, credit_code, "
                        + "category, status, level, created_at, is_deleted, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, '合格', 'B', now(), false, 0)",
                supplierId, orgId, "SUP-IT-" + suffix, code,
                "IT测试供应商", "CREDIT-IT-" + suffix, "电子");
        return supplierId;
    }

    private JsonNode getDefectRecord(String token, String id) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/defect-records/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return om.readTree(resp.getBody()).get("data");
    }

    private JsonNode findAbnormal(String token, String abnormalNo) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/sqm/abnormals", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode list = om.readTree(resp.getBody()).get("data");
        System.out.println("=== 列表返回 data 数量: " + list.size());
        for (JsonNode item : list) {
            System.out.println("=== 列表项 id=" + item.get("id").asText()
                    + " abnormalNo=" + item.get("abnormalNo").asText()
                    + " status=" + item.get("status").asText());
            if (abnormalNo.equals(item.get("abnormalNo").asText())) {
                return item;
            }
        }
        throw new AssertionError("未找到异常单 " + abnormalNo);
    }

    private String login() throws Exception {
        ResponseEntity<String> resp = rest.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "123456"),
                String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        return data.get("accessToken").asText();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}