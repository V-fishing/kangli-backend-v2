package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.service.qmsmgmt.QmsComplianceBoardService;
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
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 体系合规看板 Controller 接口测试（M8 qmsmgmt 其余，standalone MockMvc）。
 * 覆盖：board 转发、@PreAuthorize = qms-mgmt.dashboard.list 权限码一致性(铁律第 9 条)。
 */
@ExtendWith(MockitoExtension.class)
class QmsComplianceBoardControllerTest {

    @Mock QmsComplianceBoardService service;
    @InjectMocks QmsComplianceBoardController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("board:转发 service.board 返回 code=0")
    void board_forwards() throws Exception {
        when(service.board()).thenReturn(Map.of("healthScore", 90.0));
        mvc.perform(get("/api/v1/qms-mgmt/board")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).board();
    }

    @Test
    @DisplayName("权限码声明:board = qms-mgmt.dashboard.list(铁律第 9 条)")
    void preAuthorize_code() throws Exception {
        Method m = QmsComplianceBoardController.class.getMethod("board");
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError("board 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('qms-mgmt.dashboard.list')", a.value(), "board 权限码不符");
    }
}
