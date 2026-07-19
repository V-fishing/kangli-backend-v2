package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierShare;
import com.konli.qms.service.sqm.SqmSupplierShareService;
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

/** 供应商份额:查询/新增。sqm.supplier.create */
@RestController
@RequestMapping("/api/v1/sqm/shares")
@RequiredArgsConstructor
public class SqmSupplierShareController {

    private final SqmSupplierShareService sqmSupplierShareService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<List<SqmSupplierShare>> list(@RequestParam(required = false) String supplierId) {
        return R.ok(sqmSupplierShareService.list(supplierId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierShare> get(@PathVariable String id) {
        return R.ok(sqmSupplierShareService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierShare> create(@RequestBody SqmSupplierShare share) {
        return R.ok(sqmSupplierShareService.create(share));
    }
}
