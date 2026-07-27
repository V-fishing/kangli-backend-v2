package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.service.sqm.SqmSupplierService;
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

/** 供应商档案:查询/新增/编辑/删除。sqm.supplier.list / sqm.supplier.create */
@RestController
@RequestMapping("/api/v1/sqm/suppliers")
@RequiredArgsConstructor
public class SqmSupplierController {

    private final SqmSupplierService sqmSupplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<SqmSupplier>> list() {
        return R.ok(sqmSupplierService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplier> get(@PathVariable String id) {
        return R.ok(sqmSupplierService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplier> create(@RequestBody SqmSupplier supplier) {
        return R.ok(sqmSupplierService.create(supplier));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<Void> update(@RequestBody SqmSupplier supplier) {
        sqmSupplierService.update(supplier);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.delete')")
    public R<Void> delete(@PathVariable String id) {
        sqmSupplierService.delete(id);
        return R.ok();
    }
}
