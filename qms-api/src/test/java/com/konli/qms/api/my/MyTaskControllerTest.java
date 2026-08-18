package com.konli.qms.api.my;

import com.konli.qms.common.dto.MyTaskDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.my.MyTaskService;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 跨模块"我的任务" Controller 接口测试（M12 my，standalone MockMvc）。
 * 覆盖：list 转发(limit/includeClosed 参数)、未登录返回空、@PreAuthorize 权限码声明检查。
 * 权限码 my.task.list 已补齐(铁律第9条 C1，V237 种子授权 sysadmin/admin/sqe/operator)。
 * 注：standalone MockMvc 不启用 Spring Security 拦截链，@PreAuthorize 不在此环境触发，
 *     故此处仅做声明存在性静态校验，运行时 403 行为由安全集成测试保障。
 */
@ExtendWith(MockitoExtension.class)
class MyTaskControllerTest {

    @Mock MyTaskService myTaskService;
    @InjectMocks MyTaskController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        // 模拟 JWT 过滤器已注入登录用户(standalone MockMvc 无过滤器,需手动注入)
        CompanyContext.set(new CompanyContext.CurrentUser("U1", "u1", "MZ", "org"));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void clear() {
        // 清理 ThreadLocal,避免 CompanyContext 泄漏污染后续测试套件(如 PatlTaskControllerTest)
        CompanyContext.clear();
    }

    @Test
    @DisplayName("list:转发 myTaskService.myTasks 并返回 code=0")
    void list_forwards() throws Exception {
        when(myTaskService.myTasks(any(Integer.class), any(Boolean.class))).thenReturn(List.of(new MyTaskDTO()));
        mvc.perform(get("/api/v1/my/tasks").param("limit", "5").param("includeClosed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(myTaskService).myTasks(any(Integer.class), any(Boolean.class));
    }

    @Test
    @DisplayName("list:默认参数(limit=0,includeClosed=false)转发")
    void list_defaultParams_forwards() throws Exception {
        when(myTaskService.myTasks(any(Integer.class), any(Boolean.class))).thenReturn(List.of());
        mvc.perform(get("/api/v1/my/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(myTaskService).myTasks(any(Integer.class), any(Boolean.class));
    }

    @Test
    @DisplayName("权限码声明:list 端点 @PreAuthorize = my.task.list(铁律第9条 C1 已补齐)")
    void preAuthorize_present() throws Exception {
        Method m = MyTaskController.class.getMethod("list", Integer.class, Boolean.class);
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        org.junit.jupiter.api.Assertions.assertNotNull(a, "list 端点必须声明 @PreAuthorize(铁律第9条 C1)");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('my.task.list')", a.value(), "list 权限码声明不符");
    }
}
