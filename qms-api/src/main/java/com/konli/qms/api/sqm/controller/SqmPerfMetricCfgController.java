package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmPerfMetricCfg;
import com.konli.qms.service.sqm.SqmPerfMetricCfgService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 供应商绩效指标配置:读写权重/阈值。sqm.perf.config */
@RestController
@RequestMapping("/api/v1/sqm/perf-metric-cfg")
@RequiredArgsConstructor
public class SqmPerfMetricCfgController {

    private final SqmPerfMetricCfgService sqmPerfMetricCfgService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.capa')")
    public R<List<SqmPerfMetricCfg>> list() {
        return R.ok(sqmPerfMetricCfgService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.capa')")
    public R<SqmPerfMetricCfg> save(@RequestBody SqmPerfMetricCfg cfg) {
        return R.ok(sqmPerfMetricCfgService.save(cfg));
    }
}
