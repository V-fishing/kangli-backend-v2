package com.konli.qms.api.fia.controller;

import com.konli.qms.service.fia.dto.FiaWoLockActiveDTO;
import com.konli.qms.common.api.R;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaWoLock;
import com.konli.qms.service.fia.FiaWoLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首件工单锁定查询(SR-FIA-022~026)。
 */
@RestController
@RequestMapping("/api/v1/fia/wo-lock")
@RequiredArgsConstructor
public class FiaWoLockController {

    private final FiaWoLockService fiaWoLockService;

    @GetMapping
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<FiaWoLock> get(@RequestParam String woNo) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaWoLockService.getByWoNo(orgId, woNo));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<FiaWoLockActiveDTO>> active() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaWoLockService.listActive(orgId));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('fia.task.list')")
    public R<List<FiaWoLock>> list(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) String woNo) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        return R.ok(fiaWoLockService.listAll(orgId, status, woNo));
    }
}