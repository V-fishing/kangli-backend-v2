package com.konli.qms.api.cs.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.cs.entity.CsWorkOrder;
import com.konli.qms.service.cs.CsWorkOrderService;
import com.konli.qms.service.uop.UserService;
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
 * 售后工单 Controller 接口测试（M6 cs，standalone MockMvc）。
 * 覆盖：page/get/dashboard/satisfactionStats/create/update/delete/assign/complete/close 转发、
 * assignable-users 复用 UserService、@PreAuthorize 权限码一致性(铁律第 9 条)。
 */
@ExtendWith(MockitoExtension.class)
class CsWorkOrderControllerTest {

    @Mock CsWorkOrderService service;
    @Mock UserService userService;
    @InjectMocks CsWorkOrderController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("page:转发 service.page 返回 code=0")
    void page_forwards() throws Exception {
        when(service.page(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new com.konli.qms.common.api.PageResult<>(List.of(), 0L, 1, 20));
        mvc.perform(get("/api/v1/cs/work-orders/page").param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).page(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("get:转发 service.get")
    void get_forwards() throws Exception {
        when(service.get("1")).thenReturn(new CsWorkOrder());
        mvc.perform(get("/api/v1/cs/work-orders/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).get("1");
    }

    @Test
    @DisplayName("dashboard:转发 service.dashboard")
    void dashboard_forwards() throws Exception {
        when(service.dashboard()).thenReturn(Map.of("pending", 0L));
        mvc.perform(get("/api/v1/cs/work-orders/dashboard"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).dashboard();
    }

    @Test
    @DisplayName("satisfactionStats:转发 service.satisfactionStats")
    void satisfactionStats_forwards() throws Exception {
        when(service.satisfactionStats()).thenReturn(Map.of("avgScore", 0));
        mvc.perform(get("/api/v1/cs/work-orders/satisfaction-stats"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).satisfactionStats();
    }

    @Test
    @DisplayName("assignableUsers:转发 userService.listForSelect")
    void assignableUsers_forwards() throws Exception {
        when(userService.listForSelect()).thenReturn(List.of());
        mvc.perform(get("/api/v1/cs/work-orders/assignable-users"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(userService).listForSelect();
    }

    @Test
    @DisplayName("create:转发 service.create 返回 code=0")
    void create_forwards() throws Exception {
        CsWorkOrder wo = new CsWorkOrder();
        when(service.create(any())).thenReturn(wo);
        mvc.perform(post("/api/v1/cs/work-orders").contentType("application/json").content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).create(any());
    }

    @Test
    @DisplayName("update:转发 service.update")
    void update_forwards() throws Exception {
        CsWorkOrder wo = new CsWorkOrder();
        when(service.update(any())).thenReturn(wo);
        mvc.perform(put("/api/v1/cs/work-orders").contentType("application/json").content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).update(any());
    }

    @Test
    @DisplayName("delete:转发 service.delete")
    void delete_forwards() throws Exception {
        mvc.perform(delete("/api/v1/cs/work-orders/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        verify(service).delete("1");
    }

    @Test
    @DisplayName("assign/complete/close:转发对应 service 方法")
    void lifecycle_forwards() throws Exception {
        mvc.perform(post("/api/v1/cs/work-orders/1/assign").param("responsibleId", "R1"))
                .andExpect(status().isOk());
        verify(service).assign("1", "R1", null);

        mvc.perform(post("/api/v1/cs/work-orders/1/complete").param("handleDetail", "ok"))
                .andExpect(status().isOk());
        verify(service).complete("1", "ok");

        mvc.perform(post("/api/v1/cs/work-orders/1/close").param("satisfaction", "5"))
                .andExpect(status().isOk());
        verify(service).close("1", 5, null);
    }

    @Test
    @DisplayName("权限码声明:create=cs.workorder.create / assign=cs.workorder.assign / close=cs.workorder.close / satisfaction=cs.satisfaction.list(铁律第9条)")
    void preAuthorize_codes() throws Exception {
        assertPreAuthorize("create", "hasAuthority('cs.workorder.create')");
        assertPreAuthorize("assign", "hasAuthority('cs.workorder.assign')");
        assertPreAuthorize("close", "hasAuthority('cs.workorder.close')");
        assertPreAuthorize("satisfactionStats", "hasAuthority('cs.satisfaction.list')");
    }

    private void assertPreAuthorize(String methodName, String expected) throws Exception {
        Method m = CsWorkOrderController.class.getMethod(methodName, methodParams(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError(methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(), methodName + " 权限码不符");
    }

    private Class<?>[] methodParams(String methodName) {
        return switch (methodName) {
            case "create" -> new Class[]{CsWorkOrder.class};
            case "assign" -> new Class[]{String.class, String.class, String.class};
            case "close" -> new Class[]{String.class, Integer.class, String.class};
            case "satisfactionStats" -> new Class[]{};
            default -> new Class[]{};
        };
    }
}
