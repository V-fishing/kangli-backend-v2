package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditChecklistItem;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPhoto;
import com.konli.qms.domain.sqm.entity.SqmAuditWorkflowLog;
import com.konli.qms.api.sqm.dto.CompleteReviewRequest;
import com.konli.qms.service.sqm.SqmAuditExecuteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 供应商审核执行环节:检查项打分、现场照片、不符合项、流程轨迹、执行结果复核。
 * 照片文件上传复用 SqmAuditController 既有 POST /records/{recordId}/photos 端点。
 */
@RestController
@RequestMapping("/api/v1/sqm/audits")
@RequiredArgsConstructor
public class SqmAuditExecuteController {

    private final SqmAuditExecuteService executeService;

    /** 执行页初始化:计划 + 当前记录(id,无则惰性建) + 复核节点。 */
    @GetMapping("/plans/{id}/execute-init")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<SqmAuditExecuteService.ExecuteInit> init(@PathVariable String id) {
        return R.ok(executeService.init(id));
    }

    // ---- 检查项 ----

    @GetMapping("/records/{recordId}/checklist")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditChecklistItem>> listChecklist(@PathVariable String recordId) {
        return R.ok(executeService.listChecklist(recordId));
    }

    @PutMapping("/records/{recordId}/checklist")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> saveChecklist(@PathVariable String recordId,
                                 @RequestBody List<SqmAuditChecklistItem> items) {
        executeService.saveChecklist(recordId, items);
        return R.ok();
    }

    // ---- 现场照片(元数据) ----

    @GetMapping("/records/{recordId}/photos")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditPhoto>> listPhotos(@PathVariable String recordId) {
        return R.ok(executeService.listPhotos(recordId));
    }

    @PostMapping("/records/{recordId}/photos/meta")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditPhoto> addPhoto(@PathVariable String recordId,
                                     @RequestBody SqmAuditPhoto photo) {
        photo.setRecordId(recordId);
        return R.ok(executeService.addPhoto(photo));
    }

    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> removePhoto(@PathVariable String photoId) {
        executeService.removePhoto(photoId);
        return R.ok();
    }

    // ---- 不符合项 ----

    @GetMapping("/records/{recordId}/ncs")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditNc>> listNcs(@PathVariable String recordId) {
        return R.ok(executeService.listNcs(recordId));
    }

    @PostMapping("/records/{recordId}/ncs")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditNc> createNc(@PathVariable String recordId,
                                  @RequestBody SqmAuditNc nc) {
        return R.ok(executeService.createNc(recordId, nc));
    }

    // ---- 流程轨迹 ----

    @GetMapping("/plans/{id}/workflow-log")
    @PreAuthorize("hasAuthority('sqm.audit.list')")
    public R<List<SqmAuditWorkflowLog>> workflowLog(@PathVariable String id) {
        return R.ok(executeService.workflowLog(id));
    }

    // ---- 执行结果复核 ----

    @PostMapping("/plans/{id}/submit-review")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditApproval> submitReview(@PathVariable String id) {
        return R.ok(executeService.submitReview(id));
    }

    @PostMapping("/plans/{id}/complete-review")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> completeReview(@PathVariable String id,
                                  @RequestBody(required = false) CompleteReviewRequest body) {
        executeService.completeReview(id, body != null ? body.getConclusion() : null);
        return R.ok();
    }
}
