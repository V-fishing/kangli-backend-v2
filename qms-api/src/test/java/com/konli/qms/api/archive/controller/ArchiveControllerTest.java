package com.konli.qms.api.archive.controller;

import com.konli.qms.service.archive.ArchiveService;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.patrol.PatlArchiveService;
import com.konli.qms.service.sqm.SqmAuditReportArchiveService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 统一归档 Controller 接口测试（M9 archive，standalone MockMvc）。
 * 覆盖：list/expiring/detail/backfill 转发、pdf 404 分支、@PreAuthorize 权限码（复用现有码，铁律第 9 条）。
 */
@ExtendWith(MockitoExtension.class)
class ArchiveControllerTest {

    @Mock ArchiveService archiveService;
    @Mock Ncm8dArchiveService ncm8dArchiveService;
    @Mock PatlArchiveService patlArchiveService;
    @Mock SqmAuditReportArchiveService sqmAuditReportArchiveService;

    @InjectMocks ArchiveController controller;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("list:转发 archiveService.list 并返回 code=0")
    void list_forwards() throws Exception {
        when(archiveService.list(any(), any(), any(), any()))
                .thenReturn(new com.konli.qms.common.api.PageResult<>(List.of(Map.of("archiveType", "fia")), 1L, 1, 20));
        mvc.perform(get("/api/v1/archives").param("type", "fia").param("keyword", "X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
        verify(archiveService).list("fia", "X", null, null);
    }

    @Test
    @DisplayName("expiring:转发 archiveService.expiring 并返回 code=0")
    void expiring_forwards() throws Exception {
        when(archiveService.expiring(any())).thenReturn(List.of());
        mvc.perform(get("/api/v1/archives/expiring").param("days", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(archiveService).expiring(15);
    }

    @Test
    @DisplayName("detail:转发 archiveService.detail 并返回 code=0")
    void detail_forwards() throws Exception {
        when(archiveService.detail(anyString(), anyString())).thenReturn(Map.of("archiveType", "audit"));
        mvc.perform(get("/api/v1/archives/detail").param("type", "audit").param("refId", "rec-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.archiveType").value("audit"));
        verify(archiveService).detail("audit", "rec-1");
    }

    @Test
    @DisplayName("pdf:pdfRef 为 null 时返回 404")
    void pdf_notFound_returns404() throws Exception {
        when(archiveService.pdfRef(anyString(), anyString())).thenReturn(null);
        mvc.perform(get("/api/v1/archives/pdf").param("type", "audit").param("refId", "rec-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("backfill:转发各归档 Service 并返回 code=0")
    void backfill_forwards() throws Exception {
        when(ncm8dArchiveService.backfill()).thenReturn(2);
        when(patlArchiveService.backfill()).thenReturn(3);
        when(sqmAuditReportArchiveService.backfill()).thenReturn(1);
        mvc.perform(get("/api/v1/archives/backfill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.8d").value(2))
                .andExpect(jsonPath("$.data.patrol").value(3))
                .andExpect(jsonPath("$.data.audit").value(1));
        verify(ncm8dArchiveService).backfill();
        verify(patlArchiveService).backfill();
        verify(sqmAuditReportArchiveService).backfill();
    }

    @Test
    @DisplayName("权限码声明:各端点 @PreAuthorize 复用现有码(铁律第 9 条,不新增 archive.*)")
    void preAuthorize_reusesExistingCodes() throws Exception {
        String expected = "hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list') "
                + "or hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list')";
        assertPre("list", expected);
        assertPre("expiring", expected);
        assertPre("detail", expected);
        assertPre("pdf", expected);
        assertPre("backfill", "hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list') "
                + "or hasAuthority('sqm.audit.list')");
    }

    private void assertPre(String methodName, String expected) throws Exception {
        Method m = ArchiveController.class.getMethod(methodName, methodParamTypes(methodName));
        PreAuthorize a = m.getAnnotation(PreAuthorize.class);
        if (a == null) throw new AssertionError("方法 " + methodName + " 缺少 @PreAuthorize");
        org.junit.jupiter.api.Assertions.assertEquals(expected, a.value(),
                "方法 " + methodName + " 权限码声明不符");
    }

    private Class<?>[] methodParamTypes(String name) {
        return switch (name) {
            case "list" -> new Class<?>[]{String.class, String.class, Integer.class, Integer.class};
            case "expiring" -> new Class<?>[]{Integer.class};
            case "detail" -> new Class<?>[]{String.class, String.class};
            case "pdf" -> new Class<?>[]{String.class, String.class, jakarta.servlet.http.HttpServletResponse.class};
            case "backfill" -> new Class<?>[]{String.class};
            default -> new Class<?>[]{};
        };
    }
}
