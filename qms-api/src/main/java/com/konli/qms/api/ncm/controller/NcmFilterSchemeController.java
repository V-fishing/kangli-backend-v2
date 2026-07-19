package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmFilterScheme;
import com.konli.qms.service.ncm.NcmFilterSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 分析方案 CRUD。ncm.record.list */
@RestController
@RequestMapping("/api/v1/ncm/filter-schemes")
@RequiredArgsConstructor
public class NcmFilterSchemeController {

    private final NcmFilterSchemeService ncmFilterSchemeService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<List<NcmFilterScheme>> list() {
        return R.ok(ncmFilterSchemeService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<NcmFilterScheme> create(@RequestBody NcmFilterScheme scheme) {
        return R.ok(ncmFilterSchemeService.create(scheme));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.record.list')")
    public R<Void> delete(@PathVariable String id) {
        ncmFilterSchemeService.delete(id);
        return R.ok();
    }
}
