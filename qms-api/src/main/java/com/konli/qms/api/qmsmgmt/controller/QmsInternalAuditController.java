package com.konli.qms.api.qmsmgmt.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.qmsmgmt.entity.QmsAuditNc;
import com.konli.qms.domain.qmsmgmt.entity.QmsInternalAudit;
import com.konli.qms.service.qmsmgmt.QmsInternalAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/** 内审数据管理(计划 + 不符合项)。qms-mgmt.audit.* */
@RestController
@RequestMapping("/api/v1/qms-mgmt/audits")
@RequiredArgsConstructor
@Slf4j
@Validated
public class QmsInternalAuditController {

    private final QmsInternalAuditService service;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.list')")
    public R<PageResult<QmsInternalAudit>> page(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") @Min(1) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return R.ok(service.page(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.list')")
    public R<QmsInternalAudit> get(@PathVariable String id) {
        return R.ok(service.get(id));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.list')")
    public R<Map<String, Object>> stats() {
        return R.ok(service.stats());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('qms-mgmt.audit.create')")
    public R<QmsInternalAudit> create(@RequestBody QmsInternalAudit audit) {
        return R.ok(service.create(audit));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('qms-mgmt.audit.edit')")
    public R<QmsInternalAudit> update(@RequestBody QmsInternalAudit audit) {
        return R.ok(service.update(audit));
    }

    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.edit')")
    public R<Void> advance(@PathVariable String id, @RequestParam String status) {
        service.advance(id, status);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.delete')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.list')")
    public void exportCsv(HttpServletResponse response,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status) {
        service.exportCsv(response, keyword, status);
    }

    // ---- 不符合项 ----

    @GetMapping("/nc/page")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.nc')")
    public R<PageResult<QmsAuditNc>> ncPage(@RequestParam(required = false) String auditId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") @Min(1) int page,
                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return R.ok(service.ncPage(auditId, status, page, size));
    }

    @PostMapping("/nc")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.nc')")
    public R<QmsAuditNc> saveNc(@RequestBody QmsAuditNc nc) {
        return R.ok(service.saveNc(nc));
    }

    @DeleteMapping("/nc/{id}")
    @PreAuthorize("hasAuthority('qms-mgmt.audit.nc')")
    public R<Void> deleteNc(@PathVariable String id) {
        service.deleteNc(id);
        return R.ok();
    }
}
