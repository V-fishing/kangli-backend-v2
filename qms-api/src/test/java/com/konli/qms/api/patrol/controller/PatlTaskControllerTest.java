package com.konli.qms.api.patrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.patrol.entity.PatlTask;
import com.konli.qms.service.patrol.PatlTaskService;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 巡检任务 Controller 接口测试（M7 patrol，standalone MockMvc）。
 * 覆盖：请求转发 PatlTaskService、响应 R 结构、@PreAuthorize 权限码（铁律第 9 条一致性）。
 * 注：standalone 模式不校验 403 拦截（见 T2 集成测试），此处仅校验注解声明的权限码。
 */
@ExtendWith(MockitoExtension.class)
class PatlTaskControllerTest {

    @Mock PatlTaskService service;
    @InjectMocks PatlTaskController controller;
    MockMvc mvc;
    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 防御性清理:避免其他测试(如 MyTaskControllerTest)注入的 CompanyContext ThreadLocal 泄漏污染
        CompanyContext.clear();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    @Test
    @DisplayName("list:转发 service.list 并返回 code=0")
    void list_forwards() throws Exception {
        mvc.perform(get("/api/v1/patrol/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).list();
    }

    @Test
    @DisplayName("create:转发 service.create 并返回任务结构与 code=0")
    void create_forwards() throws Exception {
        PatlTask task = new PatlTask();
        task.setId("task-x");
        task.setTaskNo("PT-1");
        when(service.create(any(), any(), any(), any())).thenReturn(task);

        mvc.perform(post("/api/v1/patrol/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgId\":\"MZ\",\"routeId\":\"route-1\",\"shift\":\"早班\",\"planTime\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskNo").value("PT-1"));
        verify(service).create("MZ", "route-1", "早班", null);
    }

    @Test
    @DisplayName("submitRecord:转发 service.submitRecord 并返回 code=0")
    void submitRecord_forwards() throws Exception {
        mvc.perform(post("/api/v1/patrol/tasks/task-1/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checkpointId\":\"cp1\",\"checkpointName\":\"点位A\",\"result\":\"正常\",\"remark\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).submitRecord("task-1", "cp1", "点位A", "正常", "", "系统");
    }

    @Test
    @DisplayName("close:转发 service.close 并返回 code=0")
    void close_forwards() throws Exception {
        mvc.perform(post("/api/v1/patrol/tasks/task-1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).close("task-1");
    }

    @Test
    @DisplayName("权限码声明:各写操作 @PreAuthorize 与铁律第 9 条一致")
    void preAuthorize_codes_matchContract() throws Exception {
        assertPreAuthorize("create", "hasAuthority('patl.task.create')");
        assertPreAuthorize("submitRecord", "hasAuthority('patl.task.record')");
        assertPreAuthorize("close", "hasAuthority('patl.task.close')");
        assertPreAuthorize("list", "hasAuthority('patl.task.list')");
        assertPreAuthorize("get", "hasAuthority('patl.task.list')");
        assertPreAuthorize("page", "hasAuthority('patl.task.list')");
    }

    private void assertPreAuthorize(String methodName, String expected) throws Exception {
        Method m = PatlTaskController.class.getMethod(methodName, methodParamTypes(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) {
            throw new AssertionError("方法 " + methodName + " 缺少 @PreAuthorize");
        }
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(),
                "方法 " + methodName + " 权限码声明不符");
    }

    private Class<?>[] methodParamTypes(String name) {
        return switch (name) {
            case "list" -> new Class<?>[]{};
            case "get", "close" -> new Class<?>[]{String.class};
            case "page" -> new Class<?>[]{String.class, int.class, int.class};
            case "create" -> new Class<?>[]{com.konli.qms.api.patrol.dto.CreateTaskRequest.class};
            case "submitRecord" -> new Class<?>[]{String.class, com.konli.qms.api.patrol.dto.SubmitRecordRequest.class};
            default -> new Class<?>[]{};
        };
    }
}
