package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmDefectTrendReport;
import com.konli.qms.domain.ncm.entity.NcmDefectTrendRule;
import com.konli.qms.service.ncm.NcmDefectTrendService;
import com.konli.qms.service.ncm.dto.TrendRealtimeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ncm/trend-reports")
@RequiredArgsConstructor
public class NcmDefectTrendController {

    private final NcmDefectTrendService trendService;

    @GetMapping("/realtime")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<TrendRealtimeResult> realtime(@RequestParam(defaultValue = "day") String granularity,
                                            @RequestParam(required = false) String productModel,
                                            @RequestParam(required = false) String start,
                                            @RequestParam(required = false) String end) {
        return R.ok(trendService.realtime(granularity, productModel, start, end));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<NcmDefectTrendReport>> list(@RequestParam(required = false) String productModel,
                                              @RequestParam(required = false) String granularity,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return R.ok(trendService.listReports(productModel, granularity, page, size));
    }

    @GetMapping("/rule")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmDefectTrendRule> getRule() {
        return R.ok(trendService.getRule());
    }

    @PutMapping("/rule")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmDefectTrendRule> saveRule(@RequestBody NcmDefectTrendRule rule) {
        return R.ok(trendService.saveRule(rule));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<Void> generate(@RequestParam(defaultValue = "day") String granularity) {
        trendService.generateCurrent(granularity);
        return R.ok();
    }
}
