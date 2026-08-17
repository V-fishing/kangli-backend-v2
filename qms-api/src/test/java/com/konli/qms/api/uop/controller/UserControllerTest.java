package com.konli.qms.api.uop.controller;

import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.service.uop.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理 Controller 接口测试（M1 uop，standalone MockMvc）。
 * 覆盖：/me 当前用户、/users 列表转发、POST /users 创建转发与字段绑定。
 * 注：@PreAuthorize 权限码拦截(403)在 standalone 模式不生效，见 T2 集成测试。
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("/me 返回当前用户")
    void me_forwardsToService() throws Exception {
        com.konli.qms.service.uop.dto.CurrentUserVo vo =
                new com.konli.qms.service.uop.dto.CurrentUserVo("u-1", "admin", null, null, null, null);
        when(userService.getCurrent()).thenReturn(vo);
        mvc.perform(get("/api/v1/uop/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("GET /users 列表转发返回 code=0 与数据")
    void list_forwardsToService() throws Exception {
        SysUser u = new SysUser();
        u.setId("u-1");
        u.setUsername("admin");
        when(userService.list()).thenReturn(List.of(u));
        mvc.perform(get("/api/v1/uop/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    @DisplayName("POST /users 创建:字段绑定并转发 userService.create")
    void create_bindsAndForwards() throws Exception {
        SysUser saved = new SysUser();
        saved.setId("u-2");
        saved.setUsername("mz.operator");
        when(userService.create(any(SysUser.class), anyString())).thenReturn(saved);
        mvc.perform(post("/api/v1/uop/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mz.operator\",\"realName\":\"操作员\",\"orgId\":\"org-1\",\"password\":\"pwd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("mz.operator"));
    }
}
