package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmChangeStrictInspect;
import com.konli.qms.service.sqm.SqmChangeStrictInspectService;
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

/** 加严检验:查询/新增/恢复。sqm.change.create */
@RestController
@RequestMapping("/api/v1/sqm/strict-inspects")
@RequiredArgsConstructor
public class SqmChangeStrictInspectController {

    private final SqmChangeStrictInspectService sqmChangeStrictInspectService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<List<SqmChangeStrictInspect>> list(@RequestParam(required = false) String changeId) {
        return R.ok(sqmChangeStrictInspectService.list(changeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<SqmChangeStrictInspect> get(@PathVariable String id) {
        return R.ok(sqmChangeStrictInspectService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<SqmChangeStrictInspect> create(@RequestBody SqmChangeStrictInspect inspect) {
        return R.ok(sqmChangeStrictInspectService.create(inspect));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('sqm.change.create')")
    public R<Void> restore(@PathVariable String id) {
        sqmChangeStrictInspectService.restore(id);
        return R.ok();
    }
}
