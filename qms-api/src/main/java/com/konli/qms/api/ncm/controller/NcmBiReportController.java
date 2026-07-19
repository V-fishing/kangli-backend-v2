package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmBiReport;
import com.konli.qms.service.ncm.NcmBiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** BI 报表:查询/创建。ncm.record.list */
@RestController
@RequestMapping("/api/v1/ncm/bi-reports")
@RequiredArgsConstructor
public class NcmBiReportController {

    private final NcmBiReportService ncmBiReportService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<NcmBiReport>> list() {
        return R.ok(ncmBiReportService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmBiReport> get(@PathVariable String id) {
        return R.ok(ncmBiReportService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmBiReport> create(@RequestBody NcmBiReport report) {
        return R.ok(ncmBiReportService.create(report));
    }
}
