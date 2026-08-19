package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.service.sqm.SqmSupplierService;
import com.konli.qms.service.sqm.dto.SqmSupplierOverviewVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<PageResult<SqmSupplier>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String level,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmSupplierService.listPage(keyword, level, status, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplier> get(@PathVariable String id) {
        return R.ok(sqmSupplierService.get(id));
    }

    /** 供应商详情聚合:基础信息 + 资质/审核/绩效/异常/变更/批次各维度计数。 */
    @GetMapping("/{id}/overview")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplierOverviewVo> overview(@PathVariable String id) {
        return R.ok(sqmSupplierService.overview(id));
    }

    /** 按 MES 供应商编号(VEN 编号, 如 VEN00417)解析供应商, 供 MES 对接/脚本按 VEN 对齐。 */
    @GetMapping("/by-ven/{venCode}")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplier> getByVenCode(@PathVariable String venCode) {
        return R.ok(sqmSupplierService.findByVenCode(venCode));
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
