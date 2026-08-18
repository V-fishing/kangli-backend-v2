package com.konli.qms.api.tlm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmMetroRecord;
import com.konli.qms.service.tlm.TlmMetroRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 计量数据采集记录 Controller 接口测试（tlm，standalone MockMvc）。
 * 覆盖：page 查询转发 + create 录入转发 + create @PreAuthorize 权限码声明检查。
 * create 权限码 hasAnyAuthority('tlm.metro.calib','tlm.metro.list','tlm.metro.collect')
 * 已补齐 tlm.metro.collect(铁律第9条 C1, 与前端 MetroCollect.vue 入口码一致, 消除契约断裂)。
 * 注：standalone MockMvc 不启用 Spring Security 拦截链, @PreAuthorize 不在此环境触发,
 *     故此处仅做声明存在性静态校验, 运行时 403 行为由安全集成测试保障。
 */
@ExtendWith(MockitoExtension.class)
class TlmMetroRecordControllerTest {

    @Mock TlmMetroRecordService metroRecordService;
    @InjectMocks TlmMetroRecordController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("page:转发 metroRecordService.page 并返回 code=0")
    void page_forwards() throws Exception {
        when(metroRecordService.page(null, null, null, 1, 20)).thenReturn(new PageResult<>());
        mvc.perform(get("/api/v1/tlm/metro-record/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(metroRecordService).page(null, null, null, 1, 20);
    }

    @Test
    @DisplayName("create:转发 metroRecordService.create 并返回 code=0")
    void create_forwards() throws Exception {
        TlmMetroRecord rec = new TlmMetroRecord();
        when(metroRecordService.create(rec)).thenReturn(rec);
        mvc.perform(post("/api/v1/tlm/metro-record").contentType("application/json").content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(metroRecordService).create(org.mockito.ArgumentMatchers.any(TlmMetroRecord.class));
    }

    @Test
    @DisplayName("权限码声明:create 端点 @PreAuthorize 含 tlm.metro.collect(铁律第9条 C1)")
    void preAuthorize_create() throws Exception {
        Method m = TlmMetroRecordController.class.getMethod("create", TlmMetroRecord.class);
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        org.junit.jupiter.api.Assertions.assertNotNull(a, "create 端点必须声明 @PreAuthorize(铁律第9条 C1)");
        org.junit.jupiter.api.Assertions.assertTrue(
                a.value().contains("tlm.metro.collect"),
                "create 权限码声明必须覆盖 tlm.metro.collect(前端入口码), 实际: " + a.value());
    }
}
