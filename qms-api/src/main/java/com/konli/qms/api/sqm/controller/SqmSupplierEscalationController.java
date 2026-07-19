package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierEscalation;
import com.konli.qms.service.sqm.SqmSupplierEscalationService;
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

/** 供应商升级管理:查询/新增。sqm.supplier.create */
@RestController
@RequestMapping("/api/v1/sqm/escalations")
@RequiredArgsConstructor
public class SqmSupplierEscalationController {

    private final SqmSupplierEscalationService sqmSupplierEscalationService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<List<SqmSupplierEscalation>> list(@RequestParam(required = false) String supplierId) {
        return R.ok(sqmSupplierEscalationService.list(supplierId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierEscalation> get(@PathVariable String id) {
        return R.ok(sqmSupplierEscalationService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierEscalation> create(@RequestBody SqmSupplierEscalation escalation) {
        return R.ok(sqmSupplierEscalationService.create(escalation));
    }
}
