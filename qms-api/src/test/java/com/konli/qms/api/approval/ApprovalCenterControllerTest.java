package com.konli.qms.api.approval;

import com.konli.qms.common.dto.PendingApprovalDTO;
import com.konli.qms.service.approval.ApprovalCenterService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 通用审批中心 Controller 接口测试（M11 approval，standalone MockMvc）。
 * 覆盖：pending 转发(含 limit 截断)、@PreAuthorize 权限码声明检查。
 * 权限码 approval.center.pending 已补齐(铁律第9条 C1，V237 种子授权 sysadmin/admin/sqe/operator)。
 * 注：standalone MockMvc 不启用 Spring Security 拦截链，@PreAuthorize 不在此环境触发，
 *     故此处仅做声明存在性静态校验，运行时 403 行为由安全集成测试保障。
 */
@ExtendWith(MockitoExtension.class)
class ApprovalCenterControllerTest {

    @Mock ApprovalCenterService approvalCenterService;
    @InjectMocks ApprovalCenterController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("pending:转发 approvalCenterService.myPending 并返回 code=0")
    void pending_forwards() throws Exception {
        when(approvalCenterService.myPending()).thenReturn(List.of(new PendingApprovalDTO()));
        mvc.perform(get("/api/v1/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(approvalCenterService).myPending();
    }

    @Test
    @DisplayName("pending:带 limit 参数转发且服务端截断生效")
    void pending_withLimit_truncates() throws Exception {
        PendingApprovalDTO d1 = new PendingApprovalDTO();
        PendingApprovalDTO d2 = new PendingApprovalDTO();
        PendingApprovalDTO d3 = new PendingApprovalDTO();
        when(approvalCenterService.myPending()).thenReturn(List.of(d1, d2, d3));

        mvc.perform(get("/api/v1/approvals/pending").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        verify(approvalCenterService).myPending();
    }

    @Test
    @DisplayName("权限码声明:pending 端点 @PreAuthorize = approval.center.pending(铁律第9条 C1 已补齐)")
    void preAuthorize_present() throws Exception {
        Method m = ApprovalCenterController.class.getMethod("pending", Integer.class);
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        org.junit.jupiter.api.Assertions.assertNotNull(a, "pending 端点必须声明 @PreAuthorize(铁律第9条 C1)");
        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('approval.center.pending')", a.value(), "pending 权限码声明不符");
    }
}
