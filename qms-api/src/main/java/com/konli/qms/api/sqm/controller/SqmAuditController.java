package com.konli.qms.api.sqm.controller;

import com.konli.qms.api.sqm.dto.CloseNcRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.service.sqm.SqmAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** 供应商审核:计划/记录/不符合项。sqm.audit.list / sqm.audit.create */
@RestController
@RequestMapping("/api/v1/sqm/audits")
@RequiredArgsConstructor
public class SqmAuditController {

    private final SqmAuditService sqmAuditService;

    // ---- 审核计划 ----

    @GetMapping("/plans")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditPlan>> listPlans() {
        return R.ok(sqmAuditService.listPlans());
    }

    @PostMapping("/plans")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditPlan> createPlan(@RequestBody SqmAuditPlan plan) {
        return R.ok(sqmAuditService.createPlan(plan));
    }

    // ---- 审核记录 ----

    @GetMapping("/records")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditRecord>> listRecords() {
        return R.ok(sqmAuditService.listRecords());
    }

    @PostMapping("/records")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditRecord> createRecord(@RequestBody SqmAuditRecord record) {
        return R.ok(sqmAuditService.createRecord(record));
    }

    // ---- 不符合项 ----

    @GetMapping("/ncs")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditNc>> listNcs() {
        return R.ok(sqmAuditService.listNcs());
    }

    @PostMapping("/ncs")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditNc> createNc(@RequestBody SqmAuditNc nc) {
        return R.ok(sqmAuditService.createNc(nc));
    }

    @PostMapping("/ncs/{id}/close")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> closeNc(@PathVariable String id, @RequestBody CloseNcRequest req) {
        sqmAuditService.closeNc(id, req.getVerifyResult(), req.getVerifyComment());
        return R.ok();
    }

    // ---- 审核报告 PDF ----

    /** 生成审核报告 PDF 并以附件下载(Content-Type: application/pdf)。 */
    @GetMapping("/records/{id}/report")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String id) {
        byte[] pdf = sqmAuditService.generateReport(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "audit-report-" + id + ".pdf");
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // ---- 审核照片上传/下载(简化:本地 logs/photos/ 目录,不接 MinIO) ----

    /**
     * 上传审核照片(存本地 logs/photos/)。
     * 返回文件路径(fileRef),后续可写入审核记录/NC 的附件字段。
     */
    @PostMapping("/records/{recordId}/photos")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<String> uploadPhoto(@PathVariable String recordId,
                                 @RequestParam("file") MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String fileName = "audit-" + recordId + "-" + System.currentTimeMillis()
                    + "-" + (original == null || original.isBlank() ? "photo" : original);
            Path path = Paths.get("logs", "photos", fileName);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            return R.ok(path.toString());
        } catch (Exception e) {
            throw new BusinessException(500, "照片上传失败: " + e.getMessage());
        }
    }

    /** 读取本地照片文件返回(按文件名,存于 logs/photos/)。 */
    @GetMapping("/photos/{fileName}")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String fileName) {
        try {
            // 防路径穿越:仅取文件名部分
            String safeName = Paths.get(fileName).getFileName().toString();
            Path path = Paths.get("logs", "photos", safeName).toAbsolutePath().normalize();
            Path base = Paths.get("logs", "photos").toAbsolutePath().normalize();
            if (!path.startsWith(base) || !Files.exists(path)) {
                throw new BusinessException(404, "照片不存在");
            }
            byte[] data = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "照片读取失败: " + e.getMessage());
        }
    }
}
