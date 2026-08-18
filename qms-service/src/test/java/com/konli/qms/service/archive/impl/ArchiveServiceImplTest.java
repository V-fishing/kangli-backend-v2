package com.konli.qms.service.archive.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 统一归档服务单元测试（M9 archive，Mockito + JdbcTemplate mock）。
 * 聚焦：list 类型路由/分页/字段映射、expiring days 默认与剩余天数、detail 空参/查不到/null、pdfRef 透传。
 */
@ExtendWith(MockitoExtension.class)
class ArchiveServiceImplTest {

    @Mock JdbcTemplate jdbcTemplate;
    @InjectMocks ArchiveServiceImpl service;

    @Test
    @DisplayName("list:fia 分支返回字段映射(archiveType/archiveNo/refId/refNo)")
    void list_fia_mapsFields() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("archive_type", "fia");
        row.put("archive_no", "FIA-1");
        row.put("ref_id", "task-1");
        row.put("ref_no", "WO-1");
        row.put("archive_date", LocalDate.of(2026, 1, 1));
        row.put("retention_until", LocalDate.of(2027, 1, 1));
        row.put("report_hash", "h1");
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(row));

        List<Map<String, Object>> res = service.list("fia", null, 1, 20).getRecords();

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).containsEntry("archiveType", "fia")
                .containsEntry("archiveNo", "FIA-1")
                .containsEntry("refId", "task-1")
                .containsEntry("refNo", "WO-1")
                .containsEntry("reportHash", "h1");
        assertThat(res.get(0).get("archiveDate")).isEqualTo("2026-01-01");
        assertThat(res.get(0).get("retentionUntil")).isEqualTo("2027-01-01");
    }

    @Test
    @DisplayName("list:分页 offset 计算(page=2,size=10 -> offset=10)")
    void list_paginationOffset() {
        // 验证 list 内部 offset=(page-1)*size 不影响映射,且第二页不抛错
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("archive_type", "audit");
        row.put("archive_no", "A-1");
        row.put("ref_id", "rec-1");
        row.put("ref_no", "REC-1");
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(row));

        List<Map<String, Object>> res = service.list("audit", null, 2, 10).getRecords();
        assertThat(res).hasSize(1);
        assertThat(res.get(0).get("archiveType")).isEqualTo("audit");
    }

    @Test
    @DisplayName("list:空 type = 全部(UNION ALL 多分支),返回多类型行")
    void list_allTypesReturnsMixed() {
        Map<String, Object> fia = new LinkedHashMap<>();
        fia.put("archive_type", "fia");
        fia.put("archive_no", "F");
        fia.put("ref_id", "1");
        fia.put("ref_no", "WF");
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("archive_type", "audit");
        audit.put("archive_no", "A");
        audit.put("ref_id", "2");
        audit.put("ref_no", "RA");
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(fia, audit));

        List<Map<String, Object>> res = service.list(null, null, 1, 20).getRecords();
        assertThat(res).hasSize(2);
        assertThat(res.get(0).get("archiveType")).isEqualTo("fia");
        assertThat(res.get(1).get("archiveType")).isEqualTo("audit");
    }

    @Test
    @DisplayName("list:未知 type 返回空列表")
    void list_unknownType_returnsEmpty() {
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        List<Map<String, Object>> res = service.list("nonexist", null, 1, 20).getRecords();
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("expiring:days 默认 30 且计算 daysRemaining")
    void expiring_defaultDaysAndRemaining() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("archive_type", "fia");
        row.put("archive_no", "FIA-1");
        row.put("ref_id", "task-1");
        row.put("retention_until", LocalDate.now().plusDays(10)); // 剩 10 天
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(row));

        List<Map<String, Object>> res = service.expiring(null); // 默认 30
        assertThat(res).hasSize(1);
        assertThat(res.get(0).get("daysRemaining")).isEqualTo(10L);
    }

    @Test
    @DisplayName("detail:refId 空返回 null")
    void detail_emptyRefId_returnsNull() {
        assertThat(service.detail("fia", "  ")).isNull();
        assertThat(service.detail("fia", null)).isNull();
    }

    @Test
    @DisplayName("detail:fia 查到返回映射,且 hasPdf 对非 placeholder 为真")
    void detail_fia_mapsAndHasPdf() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("report_no", "FIA-1");
        row.put("task_id", "task-1");
        row.put("wo_no", "WO-1");
        row.put("status", "已归档");
        row.put("pdf_ref", "/archive/fia-1.pdf");
        row.put("report_hash", "h");
        row.put("archive_date", LocalDate.of(2026, 1, 1));
        row.put("retention_until", LocalDate.of(2027, 1, 1));
        // detail(fia) 还会查 queryLog/queryItems -> 兜底空
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(row));

        Map<String, Object> d = service.detail("fia", "task-1");
        assertThat(d).isNotNull();
        assertThat(d.get("archiveType")).isEqualTo("fia");
        assertThat(d.get("refId")).isEqualTo("task-1");
        assertThat(d.get("status")).isEqualTo("已归档");
        assertThat(d.get("hasPdf")).isEqualTo(true);
    }

    @Test
    @DisplayName("detail:查不到返回 null")
    void detail_notFound_returnsNull() {
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        assertThat(service.detail("audit", "missing")).isNull();
    }

    @Test
    @DisplayName("pdfRef:detail 为 null 时返回 null")
    void pdfRef_nullDetail_returnsNull() {
        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        assertThat(service.pdfRef("audit", "missing")).isNull();
    }
}
