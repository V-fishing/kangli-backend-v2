package com.konli.qms.api.fia.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.service.fia.FiaApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 首件审批(豁免/紧急放行/让步接收)。fia.std.create。 */
@RestController
@RequestMapping("/api/v1/fia/approvals")
@RequiredArgsConstructor
public class FiaApprovalController {

    private final FiaApprovalService fiaApprovalService;

    @GetMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<List<FiaApproval>> list() {
        return R.ok(fiaApprovalService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<FiaApproval> get(@PathVariable String id) {
        return R.ok(fiaApprovalService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<FiaApproval> create(@RequestBody FiaApproval approval) {
        return R.ok(fiaApprovalService.create(approval));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> approve(@PathVariable String id,
                           @RequestParam String opinion,
                           @RequestParam boolean approved) {
        String approverId = CompanyContext.get() == null ? "系统" : CompanyContext.get().userId();
        fiaApprovalService.approve(id, approverId, opinion, approved);
        return R.ok();
    }
}
