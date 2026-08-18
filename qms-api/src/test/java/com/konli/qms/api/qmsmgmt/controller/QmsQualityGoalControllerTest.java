package com.konli.qms.api.qmsmgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.qmsmgmt.entity.QmsQualityGoal;
import com.konli.qms.service.qmsmgmt.QmsQualityGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 质量目标 Controller 接口测试（M8 qmsmgmt，standalone MockMvc）。
 * 覆盖：请求转发 QmsQualityGoalService、响应 R 结构、@PreAuthorize 权限码（铁律第 9 条）。
 */
@ExtendWith(MockitoExtension.class)
class QmsQualityGoalControllerTest {

    @Mock QmsQualityGoalService service;
    @InjectMocks QmsQualityGoalController controller;
    MockMvc mvc;
    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("page:转发 service.page 并返回 code=0")
    void page_forwards() throws Exception {
        when(service.page(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0L, 1, 20));
        mvc.perform(get("/api/v1/qms-mgmt/goals/page").param("keyword", "合格率"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).page("合格率", null, null, 1, 20);
    }

    @Test
    @DisplayName("stats:转发 service.stats 并返回 code=0")
    void stats_forwards() throws Exception {
        when(service.stats()).thenReturn(Map.of("total", 3L));
        mvc.perform(get("/api/v1/qms-mgmt/goals/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3));
        verify(service).stats();
    }

    @Test
    @DisplayName("create:转发 service.create 并返回 code=0")
    void create_forwards() throws Exception {
        QmsQualityGoal g = new QmsQualityGoal();
        g.setId("g-1");
        g.setGoalName("来料合格率");
        when(service.create(any())).thenReturn(g);

        mvc.perform(post("/api/v1/qms-mgmt/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalName\":\"来料合格率\",\"targetValue\":99,\"actualValue\":98}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.goalName").value("来料合格率"));
        verify(service).create(any());
    }

    @Test
    @DisplayName("update:转发 service.update 并返回 code=0")
    void update_forwards() throws Exception {
        QmsQualityGoal g = new QmsQualityGoal();
        g.setId("g-1");
        when(service.update(any())).thenReturn(g);

        mvc.perform(put("/api/v1/qms-mgmt/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"g-1\",\"goalName\":\"新名\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).update(any());
    }

    @Test
    @DisplayName("delete:转发 service.delete 并返回 code=0")
    void delete_forwards() throws Exception {
        mvc.perform(delete("/api/v1/qms-mgmt/goals/g-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).delete("g-1");
    }

    @Test
    @DisplayName("权限码声明:各操作 @PreAuthorize 与铁律第 9 条一致")
    void preAuthorize_codes_matchContract() throws Exception {
        assertPre("page", "hasAuthority('qms-mgmt.goal.list')");
        assertPre("get", "hasAuthority('qms-mgmt.goal.list')");
        assertPre("stats", "hasAuthority('qms-mgmt.goal.list')");
        assertPre("create", "hasAuthority('qms-mgmt.goal.create')");
        assertPre("update", "hasAuthority('qms-mgmt.goal.edit')");
        assertPre("delete", "hasAuthority('qms-mgmt.goal.delete')");
    }

    private void assertPre(String methodName, String expected) throws Exception {
        Method m = QmsQualityGoalController.class.getMethod(methodName, methodParamTypes(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError("方法 " + methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(),
                "方法 " + methodName + " 权限码声明不符");
    }

    private Class<?>[] methodParamTypes(String name) {
        return switch (name) {
            case "page" -> new Class<?>[]{String.class, String.class, String.class, int.class, int.class};
            case "get", "delete" -> new Class<?>[]{String.class};
            case "stats" -> new Class<?>[]{};
            case "create", "update" -> new Class<?>[]{QmsQualityGoal.class};
            default -> new Class<?>[]{};
        };
    }
}
