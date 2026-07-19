package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierMeasure;
import com.konli.qms.service.sqm.SqmSupplierMeasureService;
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

/** 供应商改善措施:查询/新增。sqm.abnormal.create */
@RestController
@RequestMapping("/api/v1/sqm/measures")
@RequiredArgsConstructor
public class SqmSupplierMeasureController {

    private final SqmSupplierMeasureService sqmSupplierMeasureService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<List<SqmSupplierMeasure>> list(@RequestParam(required = false) String abnormalId) {
        return R.ok(sqmSupplierMeasureService.list(abnormalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<SqmSupplierMeasure> get(@PathVariable String id) {
        return R.ok(sqmSupplierMeasureService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.create')")
    public R<SqmSupplierMeasure> create(@RequestBody SqmSupplierMeasure measure) {
        return R.ok(sqmSupplierMeasureService.create(measure));
    }
}
