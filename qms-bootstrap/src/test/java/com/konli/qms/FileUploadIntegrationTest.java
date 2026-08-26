package com.konli.qms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MinIO 文件上传/下载集成测试（T6 / IT-7）。
 *
 * <p>覆盖测试计划 §4.2 IT-7「MinIO 文件上传」：</p>
 * <ul>
 *   <li>带 token 上传 multipart 文件 → 返回 objectKey(path 形如 files/{uuid}-{原文件名})</li>
 *   <li>按 objectKey 下载 → 内容与上传一致(字节级比对)</li>
 *   <li>无 token 上传 → 401(isAuthenticated 拦截)</li>
 * </ul>
 *
 * <p>依赖本机 MinIO 容器 qms-minio(:9000, 桶 qms),由 QmsIntegrationBase 注入连接配置。</p>
 */
class FileUploadIntegrationTest extends QmsIntegrationBase {

    @Autowired
    TestRestTemplate rest;
    @LocalServerPort
    int port;
    ObjectMapper om = new ObjectMapper();

    @Test
    @DisplayName("MinIO 上传下载闭环:上传返回 objectKey,下载内容字节一致")
    void upload_then_download_roundTrip() throws Exception {
        String token = login();
        byte[] content = "康立QMS-IT-7-文件上传测试".getBytes(StandardCharsets.UTF_8);

        // 上传
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "it-upload-test.txt";
            }
        });
        ResponseEntity<String> upResp = rest.exchange(
                "/api/v1/files/upload", HttpMethod.POST,
                new HttpEntity<>(form, headers), String.class);
        assertThat(upResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode data = om.readTree(upResp.getBody()).get("data");
        String path = data.get("path").asText();
        assertThat(path).startsWith("files/");
        assertThat(data.get("fileName").asText()).isEqualTo("it-upload-test.txt");

        // 下载(URI 直传避免客户端把斜杠编码成 %2F)
        HttpHeaders dlHeaders = new HttpHeaders();
        dlHeaders.setBearerAuth(token);
        URI uri = URI.create("http://localhost:" + port + "/api/v1/files/download?path=" + path);
        ResponseEntity<byte[]> dlResp = rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(dlHeaders), byte[].class);
        assertThat(dlResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Arrays.equals(dlResp.getBody(), content)).isTrue();
    }

    @Test
    @DisplayName("无 token 上传:返回 401(isAuthenticated 拦截)")
    void upload_withoutToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource("x".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "no-token.txt";
            }
        });
        ResponseEntity<String> resp = rest.exchange(
                "/api/v1/files/upload", HttpMethod.POST,
                new HttpEntity<>(form, headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
}