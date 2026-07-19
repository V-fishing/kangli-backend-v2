package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.service.sqm.SqmSupplierPerformanceService;
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

/** 供应商绩效:查询/新增/自动计算。sqm.supplier.list */
@RestController
@RequestMapping("/api/v1/sqm/performance")
@RequiredArgsConstructor
public class SqmSupplierPerformanceController {

    private final SqmSupplierPerformanceService sqmSupplierPerformanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<SqmSupplierPerformance>> list(@RequestParam(required = false) String supplierId) {
        return R.ok(sqmSupplierPerformanceService.list(supplierId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplierPerformance> get(@PathVariable String id) {
        return R.ok(sqmSupplierPerformanceService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplierPerformance> create(@RequestBody SqmSupplierPerformance performance) {
        return R.ok(sqmSupplierPerformanceService.create(performance));
    }

    @PostMapping("/calc")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplierPerformance> calc(@RequestParam String supplierId, @RequestParam String period) {
        return R.ok(sqmSupplierPerformanceService.calc(supplierId, period));
    }
}
