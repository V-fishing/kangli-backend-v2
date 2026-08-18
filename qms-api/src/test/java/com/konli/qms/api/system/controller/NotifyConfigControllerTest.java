package com.konli.qms.api.system.controller;

import com.konli.qms.domain.notify.entity.NotifyChannel;
import com.konli.qms.domain.notify.entity.NotifyConfig;
import com.konli.qms.service.notify.NotifyConfigService;
import com.konli.qms.service.uop.UserService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 统一通知配置 Controller 接口测试（M10 notify/system，standalone MockMvc）。
 * 覆盖：list/channels/users 转发、update/updateChannel 转发、@PreAuthorize = system.notify.config。
 */
@ExtendWith(MockitoExtension.class)
class NotifyConfigControllerTest {

    @Mock NotifyConfigService notifyConfigService;
    @Mock UserService userService;

    @InjectMocks NotifyConfigController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("list:转发 notifyConfigService.listAll 并返回 code=0")
    void list_forwards() throws Exception {
        when(notifyConfigService.listAll()).thenReturn(List.of(new NotifyConfig()));
        mvc.perform(get("/api/v1/system/notify-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(notifyConfigService).listAll();
    }

    @Test
    @DisplayName("channels:转发 notifyConfigService.listChannels 并返回 code=0")
    void channels_forwards() throws Exception {
        when(notifyConfigService.listChannels()).thenReturn(List.of(new NotifyChannel()));
        mvc.perform(get("/api/v1/system/notify-config/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(notifyConfigService).listChannels();
    }

    @Test
    @DisplayName("users:转发 userService.listForSelect 并返回 code=0")
    void users_forwards() throws Exception {
        when(userService.listForSelect()).thenReturn(List.of());
        mvc.perform(get("/api/v1/system/notify-config/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(userService).listForSelect();
    }

    @Test
    @DisplayName("update:转发 notifyConfigService.update 并返回 code=0")
    void update_forwards() throws Exception {
        mvc.perform(put("/api/v1/system/notify-config/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCodes\":\"R1\",\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(notifyConfigService).update(eq("1"), eq("R1"), any(), any(), eq(true));
    }

    @Test
    @DisplayName("updateChannel:转发 notifyConfigService.updateChannel 并返回 code=0")
    void updateChannel_forwards() throws Exception {
        mvc.perform(put("/api/v1/system/notify-config/channels/c1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"webhookUrl\":\"http://h\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(notifyConfigService).updateChannel(eq("c1"), eq("http://h"), eq(false), any(), any());
    }

    @Test
    @DisplayName("权限码声明:所有端点 @PreAuthorize = system.notify.config(铁律第 9 条)")
    void preAuthorize_systemNotifyConfig() throws Exception {
        String expected = "hasAuthority('system.notify.config')";
        assertPre("list");
        assertPre("users");
        assertPre("channels");
        assertPre("update");
        assertPre("updateChannel");
    }

    private void assertPre(String methodName) throws Exception {
        Method m = NotifyConfigController.class.getMethod(methodName, methodParamTypes(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError("方法 " + methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals("hasAuthority('system.notify.config')", a.value(),
                "方法 " + methodName + " 权限码声明不符");
    }

    private Class<?>[] methodParamTypes(String name) {
        return switch (name) {
            case "list" -> new Class<?>[]{};
            case "users" -> new Class<?>[]{};
            case "channels" -> new Class<?>[]{};
            case "update" -> new Class<?>[]{String.class, Map.class};
            case "updateChannel" -> new Class<?>[]{String.class, Map.class};
            default -> new Class<?>[]{};
        };
    }
}
