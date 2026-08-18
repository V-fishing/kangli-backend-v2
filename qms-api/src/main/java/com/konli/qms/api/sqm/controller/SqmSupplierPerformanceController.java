package com.konli.qms.api.sqm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmAuditFreqRule;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.domain.sqm.mapper.SqmAuditFreqRuleMapper;
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
import java.util.Map;

/** 供应商绩效:查询/新增/自动计算。sqm.supplier.list */
@RestController
@RequestMapping("/api/v1/sqm/performance")
@RequiredArgsConstructor
public class SqmSupplierPerformanceController {

    private final SqmSupplierPerformanceService sqmSupplierPerformanceService;
    private final SqmAuditFreqRuleMapper sqmAuditFreqRuleMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<SqmSupplierPerformance>> list(@RequestParam(required = false) String supplierId) {
        return R.ok(sqmSupplierPerformanceService.list(supplierId));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<PageResult<SqmSupplierPerformance>> page(@RequestParam(required = false) String supplierId,
                                                     @RequestParam(required = false) String period,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(sqmSupplierPerformanceService.listPage(supplierId, period, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<SqmSupplierPerformance> get(@PathVariable String id) {
        return R.ok(sqmSupplierPerformanceService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.perf.create')")
    public R<SqmSupplierPerformance> create(@RequestBody SqmSupplierPerformance performance) {
        return R.ok(sqmSupplierPerformanceService.create(performance));
    }

    @PostMapping("/calc")
    @PreAuthorize("hasAuthority('sqm.perf.calc')")
    public R<SqmSupplierPerformance> calc(@RequestParam String supplierId, @RequestParam String period) {
        return R.ok(sqmSupplierPerformanceService.calc(supplierId, period));
    }

    /** 供应商绩效→审核频次联动:按供应商等级查推荐审核频次(查sqm_audit_freq_rule) */
    @GetMapping("/audit-freq")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<java.util.Map<String, Object>> getAuditFreq(@RequestParam String supplierLevel) {
        SqmAuditFreqRule rule = sqmAuditFreqRuleMapper.selectOne(
                new LambdaQueryWrapper<SqmAuditFreqRule>().eq(SqmAuditFreqRule::getLevel, supplierLevel));
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("level", supplierLevel);
        result.put("freqPerYear", rule != null ? rule.getFreqPerYear() : 1);
        result.put("auditType", rule != null ? rule.getAuditType() : "年度");
        return R.ok(result);
    }
}
