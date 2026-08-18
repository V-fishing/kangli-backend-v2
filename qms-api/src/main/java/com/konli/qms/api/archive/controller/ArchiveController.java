package com.konli.qms.api.archive.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.archive.ArchiveService;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.patrol.PatlArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 统一归档查询(跨模块)。复用现有权限码(不新增 archive.list):
 * 归档查询用 {@code sqm.audit.list} 或 {@code fia.task.list}(二者其一即可访问)。
 *
 * <p>覆盖:
 * <ul>
 *   <li>{@code GET /api/v1/archives?type=fia|audit|8d&keyword=X&page=1&size=20}</li>
 *   <li>{@code GET /api/v1/archives/expiring?days=30}</li>
 * </ul></p>
 */
@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final Ncm8dArchiveService ncm8dArchiveService;
    private final PatlArchiveService patlArchiveService;
    private final com.konli.qms.service.sqm.SqmAuditReportArchiveService sqmAuditReportArchiveService;

    /**
     * 统一归档查询。
     * <p>type=fia:查 ops.fia_archived_report;type=audit:查 ops.sqm_audit_report_archive;
     * 不传 type:UNION 全部。keyword 模糊匹配 reportNo/woNo/archiveNo。</p>
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list') or hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list')")
    public R<com.konli.qms.common.api.PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return R.ok(archiveService.list(type, keyword, page, size));
    }

    /**
     * 留存到期提醒:查所有归档表中 retentionUntil <= now()+days 的记录。
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list') or hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list')")
    public R<List<Map<String, Object>>> expiring(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        return R.ok(archiveService.expiring(days));
    }

    /**
     * 档案详情:按 type + refId 返回对应归档表全量字段及关联业务信息。
     */
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list') or hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list')")
    public R<Map<String, Object>> detail(
            @RequestParam String type,
            @RequestParam String refId) {
        return R.ok(archiveService.detail(type, refId));
    }

    /**
     * 下载/预览归档 PDF:读本地 pdf_ref 路径返回文件流;未生成或不存在返回 404。
     */
    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list') or hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list')")
    public void pdf(
            @RequestParam String type,
            @RequestParam String refId,
            HttpServletResponse response) throws java.io.IOException {
        String pdfRef = archiveService.pdfRef(type, refId);
        if (pdfRef == null || pdfRef.startsWith("placeholder://") || !Files.exists(Paths.get(pdfRef))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "归档报告文件不存在或未生成");
            return;
        }
        byte[] data = Files.readAllBytes(Paths.get(pdfRef));
        String fileName = Paths.get(pdfRef).getFileName().toString();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.setContentLength(data.length);
        try (OutputStream os = response.getOutputStream()) {
            os.write(data);
            os.flush();
        }
    }

    /**
     * 历史数据补归档:将改动前已闭环/已完成、但未生成归档记录的业务数据补生成归档。
     * 复用各归档 Service 的幂等逻辑(已归档则覆盖更新),可重复安全执行。
     *
     * @param type 可选,指定补归档模块:8d / patrol;不传则两者都补
     */
    @GetMapping("/backfill")
    @PreAuthorize("hasAuthority('ncm.8d.list') or hasAuthority('patl.task.list') or hasAuthority('sqm.audit.list')")
    public R<Map<String, Integer>> backfill(
            @RequestParam(required = false) String type) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (type == null || "8d".equals(type)) {
            result.put("8d", ncm8dArchiveService.backfill());
        }
        if (type == null || "patrol".equals(type)) {
            result.put("patrol", patlArchiveService.backfill());
        }
        if (type == null || "audit".equals(type)) {
            result.put("audit", sqmAuditReportArchiveService.backfill());
        }
        return R.ok(result);
    }
}
