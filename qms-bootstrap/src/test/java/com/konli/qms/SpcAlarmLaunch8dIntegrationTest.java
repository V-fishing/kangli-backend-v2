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
 * SPC 告警 → 8D 跨模块闭环集成测试（T6 / IT-3）。
 *
 * <p>覆盖测试计划 §4.2 IT-3「SPC 告警一键发起 8D」：</p>
 * <ul>
 *   <li>SPC 告警 launch-8d 成功,返回 8D 报告(d8No 非空,source=不良记录,currentStage=D1)</li>
 *   <li>统一整改源头:先落 source=SPC报警 的缺陷记录,再从缺陷记录发起 8D,
 *       缺陷记录 d8No 回写、defectDictCode=SPC,质量追溯链 8D→缺陷记录→SPC 告警 完整</li>
 * </ul>
 *
 * <p>SPC 演示数据 Seeder 为 dev profile,测试上下文不加载,故用 JdbcTemplate 直插一条告警。</p>
 */
class SpcAlarmLaunch8dIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;
    @Autowired
    JdbcTemplate jdbcTemplate;
    ObjectMapper om = new ObjectMapper();

    @Test
    @DisplayName("SPC 告警一键发起8D:返回8D且缺陷记录回写(source=SPC报警,d8No关联)")
    void alarm_launch8d_linksDefectRecord() throws Exception {
        String token = login();
        String alarmId = insertAlarm();

        // 一键发起 8D
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/spc/alarms/" + alarmId + "/launch-8d",
                HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(resp.getBody()).get("data");
        assertThat(data.get("d8No").asText()).isNotBlank();
        assertThat(data.get("source").asText()).isEqualTo("不良记录");
        assertThat(data.get("currentStage").asText()).isEqualTo("D1");
        String defectId = data.get("sourceRefId").asText();
        assertThat(defectId).isNotBlank();

        // 统一整改源头:缺陷记录 source=SPC报警、字典=SPC、d8No 已回写
        JsonNode def = getDefectRecord(token, defectId);
        assertThat(def.get("source").asText()).isEqualTo("SPC报警");
        assertThat(def.get("defectDictCode").asText()).isEqualTo("SPC");
        assertThat(def.get("d8No").asText()).isEqualTo(data.get("d8No").asText());
    }

    // ============ 工具 ============

    private String insertAlarm() {
        String orgId = jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        // spc_alarm.param_id 有外键约束,须先落一条 spc_param
        String paramId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO ops.spc_param (id, org_id, param_name, proc_name, unit, spec_text, collect_freq, "
                        + "is_active, created_at, updated_at, is_deleted, version) "
                        + "VALUES (?, ?, 'IT测试参数', 'IT工序', 'mm', '10±0.1', '每班', true, now(), now(), false, 0)",
                paramId, orgId);
        String alarmId = UUID.randomUUID().toString();
        String code = "SPC-IT-" + System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO ops.spc_alarm (id, org_id, code, param_id, param_name, triggered_rule, level, "
                        + "alarm_time, status, wo_no, batch_no, created_at, updated_at, is_deleted, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, now(), '待确认', ?, ?, now(), now(), false, 0)",
                alarmId, orgId, code, paramId, "IT测试参数", "①", "报警",
                "WO-IT-001", "BN-IT-001");
        return alarmId;
    }

    private JsonNode getDefectRecord(String token, String id) throws Exception {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/ncm/defect-records/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return om.readTree(resp.getBody()).get("data");
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