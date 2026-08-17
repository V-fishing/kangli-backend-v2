package com.konli.qms.api.uop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konli.qms.api.uop.dto.LoginResponse;
import com.konli.qms.service.uop.AuthService;
import com.konli.qms.service.uop.dto.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证 Controller 接口测试（M1 uop，standalone MockMvc）。
 * 覆盖：登录请求转发 AuthService、响应 LoginResponse 结构、退出转发。
 * 注：@PreAuthorize 在 standalone 模式不生效，403 拦截见 T2 集成测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthController controller;
    MockMvc mvc;
    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("登录:转发到 authService.login 并返回 token 结构与 code=0")
    void login_forwardsAndReturnsToken() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenReturn(new LoginResult("jwt-token-xyz", 1800L));
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(1800));
    }

    @Test
    @DisplayName("登录:username/password 字段正确反序列化")
    void login_bindsCredentials() throws Exception {
        when(authService.login(anyString(), anyString())).thenAnswer(inv -> {
            String u = inv.getArgument(0);
            return new LoginResult("tok-" + u, 1800L);
        });
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mz.admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("tok-mz.admin"));
    }

    @Test
    @DisplayName("退出:无状态返回 code=0")
    void logout_returnsOk() throws Exception {
        mvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
