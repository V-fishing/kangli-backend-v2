package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierCert;
import com.konli.qms.service.sqm.SqmSupplierCertService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 供应商资质:查询/新增/删除。sqm.supplier.create */
@RestController
@RequestMapping("/api/v1/sqm/supplier-certs")
@RequiredArgsConstructor
public class SqmSupplierCertController {

    private final SqmSupplierCertService sqmSupplierCertService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<List<SqmSupplierCert>> list(@RequestParam(required = false) String supplierId) {
        return R.ok(sqmSupplierCertService.list(supplierId));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<SqmSupplierCert>> expiring(@RequestParam(defaultValue = "30") int days) {
        return R.ok(sqmSupplierCertService.expiring(days));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierCert> get(@PathVariable String id) {
        return R.ok(sqmSupplierCertService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierCert> create(@RequestBody SqmSupplierCert cert) {
        return R.ok(sqmSupplierCertService.create(cert));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.delete')")
    public R<Void> delete(@PathVariable String id) {
        sqmSupplierCertService.delete(id);
        return R.ok();
    }
}
