package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmDailyReportConfig;
import com.konli.qms.service.ncm.NcmDailyReportConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 定时日报配置:查询/upsert/启停。ncm.record.create */
@RestController
@RequestMapping("/api/v1/ncm/daily-report-config")
@RequiredArgsConstructor
public class NcmDailyReportConfigController {

    private final NcmDailyReportConfigService ncmDailyReportConfigService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<List<NcmDailyReportConfig>> list() {
        return R.ok(ncmDailyReportConfigService.list());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<NcmDailyReportConfig> save(@RequestBody NcmDailyReportConfig config) {
        return R.ok(ncmDailyReportConfigService.save(config));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Void> toggle(@PathVariable String id, @RequestParam Boolean enabled) {
        ncmDailyReportConfigService.toggle(id, enabled);
        return R.ok();
    }
}
