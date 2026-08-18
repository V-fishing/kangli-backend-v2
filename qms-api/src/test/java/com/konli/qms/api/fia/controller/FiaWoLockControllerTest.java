package com.konli.qms.api.fia.controller;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaWoLock;
import com.konli.qms.service.fia.FiaWoLockService;
import com.konli.qms.service.fia.dto.FiaWoLockActiveDTO;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 首件工单锁定 Controller 接口测试（M4 fia，standalone MockMvc）。
 * 覆盖：三个 GET 查询端点转发 + 两个 POST 放行端点转发 + @PreAuthorize 权限码声明检查。
 * 放行端点权限码 fia.wolock.release / fia.wolock.emergency 由 V238 种子注册并授权
 * (铁律第9条 C1 补齐,消除前端 woLock.ts 调用 404 的真实断裂)。
 * 注：standalone MockMvc 不启用 Spring Security 拦截链,@PreAuthorize 不在此环境触发,
 *     故此处仅做声明存在性静态校验,运行时 403 行为由安全集成测试保障。
 */
@ExtendWith(MockitoExtension.class)
class FiaWoLockControllerTest {

    @Mock FiaWoLockService fiaWoLockService;
    @InjectMocks FiaWoLockController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        // 注入非全量组织上下文,供 orgId 解析 + approverId 取用
        CompanyContext.set(new CompanyContext.CurrentUser("u-test-id", "tester", "ORG-MZ", "MZ"));
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    @Test
    @DisplayName("get:转发 getByWoNo 并返回 code=0")
    void get_forwards() throws Exception {
        when(fiaWoLockService.getByWoNo("ORG-MZ", "WO-1")).thenReturn(new FiaWoLock());
        mvc.perform(get("/api/v1/fia/wo-lock").param("woNo", "WO-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(fiaWoLockService).getByWoNo("ORG-MZ", "WO-1");
    }

    @Test
    @DisplayName("list:转发 listAll 并返回 code=0")
    void list_forwards() throws Exception {
        when(fiaWoLockService.listAll("ORG-MZ", "锁定", "WO")).thenReturn(List.of(new FiaWoLock()));
        mvc.perform(get("/api/v1/fia/wo-lock/list").param("status", "锁定").param("woNo", "WO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(fiaWoLockService).listAll("ORG-MZ", "锁定", "WO");
    }

    @Test
    @DisplayName("active:转发 listActive 并返回 code=0")
    void active_forwards() throws Exception {
        when(fiaWoLockService.listActive("ORG-MZ")).thenReturn(List.of(new FiaWoLockActiveDTO()));
        mvc.perform(get("/api/v1/fia/wo-lock/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(fiaWoLockService).listActive("ORG-MZ");
    }

    @Test
    @DisplayName("release:转发 release 并取当前登录用户为 approverId")
    void release_forwards() throws Exception {
        mvc.perform(post("/api/v1/fia/wo-lock/release")
                        .param("woNo", "WO-1").param("releaseReason", "r").param("traceTag", "t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(fiaWoLockService).release("ORG-MZ", "WO-1", "u-test-id", "r", "t");
    }

    @Test
    @DisplayName("emergencyRelease:转发 emergencyRelease 并取当前登录用户为 approverId")
    void emergencyRelease_forwards() throws Exception {
        mvc.perform(post("/api/v1/fia/wo-lock/emergency-release")
                        .param("woNo", "WO-2").param("releaseReason", "er").param("traceTag", "et"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(fiaWoLockService).emergencyRelease("ORG-MZ", "WO-2", "u-test-id", "er", "et");
    }

    @Test
    @DisplayName("权限码声明:release 端点 @PreAuthorize = fia.wolock.release")
    void preAuthorize_release() throws Exception {
        Method m = FiaWoLockController.class.getMethod("release", String.class, String.class, String.class);
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        org.junit.jupiter.api.Assertions.assertNotNull(a, "release 端点必须声明 @PreAuthorize(铁律第9条 C1)");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('fia.wolock.release')", a.value(), "release 权限码声明不符");
    }

    @Test
    @DisplayName("权限码声明:emergencyRelease 端点 @PreAuthorize = fia.wolock.emergency")
    void preAuthorize_emergency() throws Exception {
        Method m = FiaWoLockController.class.getMethod("emergencyRelease", String.class, String.class, String.class);
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        org.junit.jupiter.api.Assertions.assertNotNull(a, "emergencyRelease 端点必须声明 @PreAuthorize(铁律第9条 C1)");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('fia.wolock.emergency')", a.value(), "emergencyRelease 权限码声明不符");
    }
}
