package com.konli.qms.api.kpi.controller;

import com.konli.qms.service.kpi.KpiCompareService;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 分公司 KPI 对比 Controller 接口测试（M13 kpi，standalone MockMvc）。
 * 覆盖：compare 转发 + @PreAuthorize 权限码（system.org.switch，铁律第 9 条一致性）。
 */
@ExtendWith(MockitoExtension.class)
class KpiCompareControllerTest {

    @Mock KpiCompareService kpiCompareService;
    @InjectMocks KpiCompareController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("compare:转发 kpiCompareService.compare 并返回 code=0")
    void compare_forwards() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orgs", java.util.List.of());
        data.put("items", java.util.List.of());
        when(kpiCompareService.compare()).thenReturn(data);

        mvc.perform(get("/api/v1/kpi/compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(kpiCompareService).compare();
    }

    @Test
    @DisplayName("权限码声明:compare 端点 @PreAuthorize = system.org.switch(铁律第 9 条)")
    void preAuthorize_systemOrgSwitch() throws Exception {
        Method m = KpiCompareController.class.getMethod("compare");
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError("compare 方法缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('system.org.switch')", a.value(), "compare 权限码声明不符");
    }
}
