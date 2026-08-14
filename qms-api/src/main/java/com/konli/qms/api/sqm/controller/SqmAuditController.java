package com.konli.qms.api.sqm.controller;

import com.konli.qms.api.sqm.dto.ApproveAuditRequest;
import com.konli.qms.api.sqm.dto.AuditApprovalCfgRequest;
import com.konli.qms.api.sqm.dto.CloseNcRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.oss.ObjectStorageService;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditApprovalCfg;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.entity.SqmAuditReportArchive;
import com.konli.qms.service.sqm.SqmAuditApprovalCfgService;
import com.konli.qms.service.sqm.SqmAuditReportArchiveService;
import com.konli.qms.service.sqm.SqmAuditService;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.sqm.dto.AuditorDef;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/** 供应商审核:计划/记录/不符合项。sqm.audit.list / sqm.audit.create */
@RestController
@RequestMapping("/api/v1/sqm/audits")
@RequiredArgsConstructor
public class SqmAuditController {

    private final SqmAuditService sqmAuditService;
    private final SqmAuditReportArchiveService sqmAuditReportArchiveService;
    private final SqmAuditApprovalCfgService approvalCfgService;
    private final ObjectStorageService objectStorageService;

    // ---- 审核计划 ----

    @GetMapping("/plans")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditPlan>> listPlans() {
        return R.ok(sqmAuditService.listPlans());
    }

    @GetMapping("/plans/page")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<PageResult<SqmAuditPlan>> listPlansPage(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auditType,
            @RequestParam(required = false) String supplierId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmAuditService.listPlansPage(status, auditType, supplierId, page, size));
    }

    @PostMapping("/plans")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditPlan> createPlan(@RequestBody SqmAuditPlan plan) {
        return R.ok(sqmAuditService.createPlan(plan));
    }

    @PutMapping("/plans/{id}/confirm")
    @PreAuthorize("hasAuthority('sqm.audit.plan.confirm')")
    public R<Void> confirmPlan(@PathVariable String id) {
        sqmAuditService.confirmPlan(id);
        return R.ok();
    }

    @PostMapping("/plans/{id}/start")
    @PreAuthorize("hasAuthority('sqm.audit.plan.start')")
    public R<SqmAuditPlan> startPlan(@PathVariable String id) {
        return R.ok(sqmAuditService.startPlan(id));
    }

    @GetMapping("/plans/{id}")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<SqmAuditPlan> getPlan(@PathVariable String id) {
        return R.ok(sqmAuditService.getPlan(id));
    }

    /** 列表级改派审核组长(更新审核组长 + 推送被指派人任务中心)。 */
    @PostMapping("/plans/{id}/reassign")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> reassignPlan(@PathVariable String id, @RequestBody DefectLaunchRequest req) {
        sqmAuditService.reassign(id, req);
        return R.ok();
    }

    /** 按来源变更单 id 反查关联审核计划(双向追溯:变更单详情 → 审核计划)。 */
    @GetMapping("/plans/by-change/{changeId}")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditPlan>> listByChangeId(@PathVariable String changeId) {
        return R.ok(sqmAuditService.listByChangeId(changeId));
    }

    // ---- 审核会签(质量/采购/研发并行 + 质量一票否决) ----

    @GetMapping("/plans/{id}/approvals")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditApproval>> listApprovals(@PathVariable String id) {
        return R.ok(sqmAuditService.listApprovals(id));
    }

    @PostMapping("/plans/{id}/approvals")
    @PreAuthorize("hasAuthority('sqm.audit.approve')")
    public R<Void> approve(@PathVariable String id, @RequestBody ApproveAuditRequest req) {
        sqmAuditService.approve(id, req.getApprovalRole(), req.isApproved(), req.getOpinion());
        return R.ok();
    }

    // ---- 审核会签配置(按审核类型可配置会签人员/否决权) ----

    /** 列出全部审核类型的会签配置(供管理员配置页使用)。 */
    @GetMapping("/audit-approval-cfg")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditApprovalCfg>> listApprovalCfg() {
        return R.ok(approvalCfgService.listAll());
    }

    /** 保存某审核类型的会签配置,并重置该类型计划的会签链(下次打开详情按新配置重建)。 */
    @PutMapping("/audit-approval-cfg")
    @PreAuthorize("hasAuthority('sqm.audit.approve')")
    public R<Void> saveApprovalCfg(@RequestBody AuditApprovalCfgRequest req) {
        List<AuditorDef> auditors = new ArrayList<>();
        if (req.getAuditors() != null) {
            for (AuditApprovalCfgRequest.AuditorItem it : req.getAuditors()) {
                AuditorDef d = new AuditorDef();
                d.setRole(it.getRole() == null ? "" : it.getRole());
                d.setLabel(it.getLabel());
                d.setVeto(it.isVeto());
                d.setUserId(it.getUserId());
                d.setUserIds(it.getUserIds());
                auditors.add(d);
            }
        }
        approvalCfgService.save(req.getAuditType(), auditors);
        sqmAuditService.resetApprovalsForType(req.getAuditType());
        return R.ok();
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

    @GetMapping("/records/{id}")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<SqmAuditRecord> getRecord(@PathVariable String id) {
        return R.ok(sqmAuditService.getRecord(id));
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
    @PreAuthorize("hasAuthority('sqm.audit.nc.close')")
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

    // ---- 审核报告归档 ----

    /** 查某审核记录的归档(可传 recordId 过滤;不传则查全部)。 */
    @GetMapping("/records/{id}/archive")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditReportArchive>> getArchive(@PathVariable String id) {
        return R.ok(sqmAuditReportArchiveService.list(id));
    }

    /** 触发归档:生成审核报告 PDF + SHA-256 hash + retentionUntil=now+15y,落 sqm_audit_report_archive。 */
    @PostMapping("/records/{id}/archive/generate")
    @PreAuthorize("hasAuthority('sqm.audit.archive')")
    public R<SqmAuditReportArchive> generateArchive(@PathVariable String id) {
        return R.ok(sqmAuditReportArchiveService.generatePdf(id));
    }

    // ---- 审核照片上传/下载(统一写入 MinIO) ----

    /**
     * 上传审核照片(统一写入 MinIO,objectKey 形如 audit-photos/{uuid}-{原文件名})。
     * 返回 objectKey,前端回填到记录的 filePath / 用于下载端点。
     */
    @PostMapping("/records/{recordId}/photos")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<String> uploadPhoto(@PathVariable String recordId,
                                 @RequestParam("file") MultipartFile file) {
        String objectKey = objectStorageService.upload("audit-photos", file);
        return R.ok(objectKey);
    }

    /**
     * 读取 MinIO 中的照片(按 objectKey,含前缀如 audit-photos/{uuid}-name)。
     * 用 /** 通配捕获含斜杠的 objectKey,避免路径变量匹配失败。
     */
    @GetMapping("/photos/**")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public ResponseEntity<byte[]> getPhoto(HttpServletRequest request) {
        try {
            // 取 /photos/ 之后的完整路径作为 objectKey
            String key = request.getRequestURI().substring(request.getRequestURI().indexOf("/photos/") + "/photos/".length());
            byte[] data = objectStorageService.download(key);
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
