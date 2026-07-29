package com.konli.qms.api.archive.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.service.archive.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    /**
     * 统一归档查询。
     * <p>type=fia:查 ops.fia_archived_report;type=audit:查 ops.sqm_audit_report_archive;
     * 不传 type:UNION 全部。keyword 模糊匹配 reportNo/woNo/archiveNo。</p>
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list')")
    public R<List<Map<String, Object>>> list(
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
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list')")
    public R<List<Map<String, Object>>> expiring(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        return R.ok(archiveService.expiring(days));
    }

    /**
     * 档案详情:按 type + refId 返回对应归档表全量字段及关联业务信息。
     */
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list')")
    public R<Map<String, Object>> detail(
            @RequestParam String type,
            @RequestParam String refId) {
        return R.ok(archiveService.detail(type, refId));
    }

    /**
     * 下载/预览归档 PDF:读本地 pdf_ref 路径返回文件流;未生成或不存在返回 404。
     */
    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('sqm.audit.list') or hasAuthority('fia.task.list')")
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
}
