package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import com.konli.qms.service.sqm.PerfAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 供应商绩效分析:排名/趋势/柏拉图。sqm.supplier.list */
@RestController
@RequestMapping("/api/v1/sqm/perf-analysis")
@RequiredArgsConstructor
public class PerfAnalysisController {

    private final PerfAnalysisService perfAnalysisService;

    @GetMapping("/rank")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<PageResult<Map<String, Object>>> rank(@RequestParam String period,
                                                   @RequestParam(required = false) String category,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return R.ok(perfAnalysisService.rank(period, category, page, size));
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<SqmSupplierPerformance>> trend(@RequestParam List<String> supplierIds,
                                                 @RequestParam String periodStart,
                                                 @RequestParam String periodEnd) {
        return R.ok(perfAnalysisService.trend(supplierIds, periodStart, periodEnd));
    }

    @GetMapping("/pareto")
    @PreAuthorize("hasAuthority('sqm.supplier.list')")
    public R<List<Map<String, Object>>> pareto(@RequestParam String periodStart,
                                               @RequestParam String periodEnd,
                                               @RequestParam(defaultValue = "10") int topN) {
        return R.ok(perfAnalysisService.pareto(periodStart, periodEnd, topN));
    }
}
