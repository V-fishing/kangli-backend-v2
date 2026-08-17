package com.konli.qms.api.ncm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.Ncm8dService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 8D Controller 接口测试(standalone MockMvc,不加载 Spring 上下文)。
 * 覆盖:统一 R<T> 包装、创建入口转发、查询分页转发、参数绑定。
 *
 * 注:@PreAuthorize 权限码拦截(403)需在集成测试(@SpringBootTest + 真实 Security 上下文)中验证,
 * 见测试方案 P2 阶段;本切片测试聚焦 HTTP 层与转发正确性。
 */
@ExtendWith(MockitoExtension.class)
class Ncm8dControllerTest {

    @Mock
    private Ncm8dService ncm8dService;

    @Mock
    private Ncm8dApprovalConfigService ncm8dApprovalConfigService;

    @InjectMocks
    private Ncm8dController controller;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("创建人工 8D:转发到 service.create 并返回 code=0 + 数据")
    void create_forwardsToService_andReturnsOk() throws Exception {
        Qms8dReport saved = new Qms8dReport();
        saved.setD8No("8D-TEST-001");
        saved.setSource("不良记录");
        when(ncm8dService.create(any(Qms8dReport.class))).thenReturn(saved);

        String body = "{\"flowType\":\"8D\",\"source\":\"人工\",\"issue\":\"接口测试-人工建8D\",\"severity\":\"高\"}";
        mvc.perform(post("/api/v1/ncm/8d-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.d8No").value("8D-TEST-001"));
    }

    @Test
    @DisplayName("查询 8D 分页:转发到 service.listPage 并返回 code=0")
    void page_forwardsToService_andReturnsOk() throws Exception {
        when(ncm8dService.listPage(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new com.konli.qms.common.api.PageResult<>());
        mvc.perform(get("/api/v1/ncm/8d-reports/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("创建请求体参数绑定:issue 字段正确反序列化")
    void create_bindsIssueField() throws Exception {
        Qms8dReport saved = new Qms8dReport();
        saved.setD8No("8D-TEST-002");
        when(ncm8dService.create(any(Qms8dReport.class))).thenAnswer(inv -> {
            Qms8dReport r = inv.getArgument(0);
            saved.setIssue(r.getIssue());
            return saved;
        });

        String body = "{\"flowType\":\"8D\",\"source\":\"人工\",\"issue\":\"绑定校验主题\",\"severity\":\"高\"}";
        mvc.perform(post("/api/v1/ncm/8d-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.issue").value("绑定校验主题"));
    }
}
