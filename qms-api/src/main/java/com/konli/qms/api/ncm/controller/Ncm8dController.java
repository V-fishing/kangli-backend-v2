package com.konli.qms.api.ncm.controller;

import com.konli.qms.api.ncm.dto.AdvanceStageRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.dto.EightDVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 8D 报告:创建/查询/阶段推进。ncm.8d.list / ncm.8d.create */
@RestController
@RequestMapping("/api/v1/ncm/8d-reports")
@RequiredArgsConstructor
public class Ncm8dController {

    private final Ncm8dService ncm8dService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<List<Qms8dReport>> list() {
        return R.ok(ncm8dService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.8d.list')")
    public R<EightDVo> get(@PathVariable String id) {
        return R.ok(ncm8dService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Qms8dReport> create(@RequestBody Qms8dReport report) {
        return R.ok(ncm8dService.create(report));
    }

    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Void> advance(@PathVariable String id, @RequestBody AdvanceStageRequest req) {
        ncm8dService.advanceStage(id, req.getStageCode(), req.getContent(), req.getOwner());
        return R.ok();
    }
}
