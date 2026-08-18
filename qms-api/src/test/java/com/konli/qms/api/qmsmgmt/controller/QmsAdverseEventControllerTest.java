package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.domain.qmsmgmt.entity.QmsAdverseEvent;
import com.konli.qms.service.qmsmgmt.QmsAdverseEventService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 不良事件 Controller 接口测试（M8 qmsmgmt 其余，standalone MockMvc）。
 * 覆盖：page/stats/create/update/delete/handle 转发、@PreAuthorize 权限码一致性(铁律第 9 条)。
 */
@ExtendWith(MockitoExtension.class)
class QmsAdverseEventControllerTest {

    @Mock QmsAdverseEventService service;
    @InjectMocks QmsAdverseEventController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("page:转发 service.page")
    void page_forwards() throws Exception {
        when(service.page(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new com.konli.qms.common.api.PageResult<>(List.of(), 0L, 1, 20));
        mvc.perform(get("/api/v1/qms-mgmt/adverse/page")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).page(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("stats:转发 service.stats")
    void stats_forwards() throws Exception {
        when(service.stats()).thenReturn(Map.of("total", 0L));
        mvc.perform(get("/api/v1/qms-mgmt/adverse/stats")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).stats();
    }

    @Test
    @DisplayName("create/update/delete/handle:转发对应 service 方法")
    void lifecycle_forwards() throws Exception {
        QmsAdverseEvent e = new QmsAdverseEvent();
        when(service.create(any())).thenReturn(e);
        mvc.perform(post("/api/v1/qms-mgmt/adverse").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).create(any());

        when(service.update(any())).thenReturn(e);
        mvc.perform(put("/api/v1/qms-mgmt/adverse").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).update(any());

        mvc.perform(delete("/api/v1/qms-mgmt/adverse/1")).andExpect(status().isOk());
        verify(service).delete("1");

        mvc.perform(post("/api/v1/qms-mgmt/adverse/1/handle").param("status", "DONE")
                        .param("handleDesc", "已处理").param("owner", "张三")).andExpect(status().isOk());
        verify(service).handle("1", "DONE", "已处理", "张三");
    }

    @Test
    @DisplayName("权限码声明:adverse.list/create/edit/delete(铁律第9条)")
    void preAuthorize_codes() throws Exception {
        assertPre("page", "hasAuthority('qms-mgmt.adverse.list')");
        assertPre("create", "hasAuthority('qms-mgmt.adverse.create')");
        assertPre("update", "hasAuthority('qms-mgmt.adverse.edit')");
        assertPre("delete", "hasAuthority('qms-mgmt.adverse.delete')");
    }

    private void assertPre(String methodName, String expected) throws Exception {
        Method m = QmsAdverseEventController.class.getMethod(methodName, methodParams(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError(methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(), methodName + " 权限码不符");
    }

    private Class<?>[] methodParams(String name) {
        return switch (name) {
            case "page" -> new Class[]{String.class, String.class, String.class, int.class, int.class};
            case "create" -> new Class[]{QmsAdverseEvent.class};
            case "update" -> new Class[]{QmsAdverseEvent.class};
            case "delete" -> new Class[]{String.class};
            case "handle" -> new Class[]{String.class, String.class, String.class, String.class};
            default -> new Class[]{};
        };
    }
}
