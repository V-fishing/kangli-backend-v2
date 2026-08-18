package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.qmsmgmt.entity.QmsInternalAudit;
import com.konli.qms.service.qmsmgmt.QmsInternalAuditService;
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
 * 内审/不符合项 Controller 接口测试（M8 qmsmgmt 其余，standalone MockMvc）。
 * 覆盖：page/get/stats/create/update/delete/advance/nc 转发、@PreAuthorize 权限码一致性(铁律第 9 条)。
 */
@ExtendWith(MockitoExtension.class)
class QmsInternalAuditControllerTest {

    @Mock QmsInternalAuditService service;
    @InjectMocks QmsInternalAuditController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("page:转发 service.page")
    void page_forwards() throws Exception {
        when(service.page(any(), any(), anyInt(), anyInt()))
                .thenReturn(new com.konli.qms.common.api.PageResult<>(List.of(), 0L, 1, 20));
        mvc.perform(get("/api/v1/qms-mgmt/audits/page")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).page(any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("stats:转发 service.stats")
    void stats_forwards() throws Exception {
        when(service.stats()).thenReturn(Map.of("planned", 0L));
        mvc.perform(get("/api/v1/qms-mgmt/audits/stats")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).stats();
    }

    @Test
    @DisplayName("create/update/delete/advance:转发对应 service 方法")
    void lifecycle_forwards() throws Exception {
        QmsInternalAudit a = new QmsInternalAudit();
        when(service.create(any())).thenReturn(a);
        mvc.perform(post("/api/v1/qms-mgmt/audits").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).create(any());

        when(service.update(any())).thenReturn(a);
        mvc.perform(put("/api/v1/qms-mgmt/audits").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).update(any());

        mvc.perform(delete("/api/v1/qms-mgmt/audits/1")).andExpect(status().isOk());
        verify(service).delete("1");

        mvc.perform(post("/api/v1/qms-mgmt/audits/1/advance").param("status", "ONGOING"))
                .andExpect(status().isOk());
        verify(service).advance("1", "ONGOING");
    }

    @Test
    @DisplayName("nc:saveNc/deleteNc 转发")
    void nc_forwards() throws Exception {
        mvc.perform(post("/api/v1/qms-mgmt/audits/nc").contentType("application/json").content("{}"))
                .andExpect(status().isOk());
        verify(service).saveNc(any());
        mvc.perform(delete("/api/v1/qms-mgmt/audits/nc/1")).andExpect(status().isOk());
        verify(service).deleteNc("1");
    }

    @Test
    @DisplayName("权限码声明:list/audit.create/audit.edit/audit.delete/audit.nc(铁律第9条)")
    void preAuthorize_codes() throws Exception {
        assertPre("page", "hasAuthority('qms-mgmt.audit.list')");
        assertPre("create", "hasAuthority('qms-mgmt.audit.create')");
        assertPre("update", "hasAuthority('qms-mgmt.audit.edit')");
        assertPre("delete", "hasAuthority('qms-mgmt.audit.delete')");
        assertPre("saveNc", "hasAuthority('qms-mgmt.audit.nc')");
    }

    private void assertPre(String methodName, String expected) throws Exception {
        Method m = QmsInternalAuditController.class.getMethod(methodName, methodParams(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError(methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(), methodName + " 权限码不符");
    }

    private Class<?>[] methodParams(String name) {
        return switch (name) {
            case "page" -> new Class[]{String.class, String.class, int.class, int.class};
            case "create" -> new Class[]{QmsInternalAudit.class};
            case "update" -> new Class[]{QmsInternalAudit.class};
            case "delete" -> new Class[]{String.class};
            case "saveNc" -> new Class[]{com.konli.qms.domain.qmsmgmt.entity.QmsAuditNc.class};
            default -> new Class[]{};
        };
    }
}
