package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierGradeRule;
import com.konli.qms.service.sqm.SqmSupplierGradeRuleService;
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

/** 供应商评级规则:查询/新增/编辑/删除。sqm.supplier.create */
@RestController
@RequestMapping("/api/v1/sqm/grade-rules")
@RequiredArgsConstructor
public class SqmSupplierGradeRuleController {

    private final SqmSupplierGradeRuleService sqmSupplierGradeRuleService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<List<SqmSupplierGradeRule>> list() {
        return R.ok(sqmSupplierGradeRuleService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<SqmSupplierGradeRule> create(@RequestBody SqmSupplierGradeRule rule) {
        return R.ok(sqmSupplierGradeRuleService.create(rule));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sqm.supplier.create')")
    public R<Void> update(@RequestBody SqmSupplierGradeRule rule) {
        sqmSupplierGradeRuleService.update(rule);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.delete')")
    public R<Void> delete(@PathVariable String id) {
        sqmSupplierGradeRuleService.delete(id);
        return R.ok();
    }
}
