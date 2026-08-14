package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcSpecStandard;
import com.konli.qms.service.spc.SpcSpecStandardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 标准线管理 CRUD(按组织+物料+工序定义规格上下限)。 */
@RestController
@RequestMapping("/api/v1/spc/spec-standards")
@RequiredArgsConstructor
public class SpcSpecStandardController {

    private final SpcSpecStandardService spcSpecStandardService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<List<SpcSpecStandard>> list() {
        return R.ok(spcSpecStandardService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<PageResult<SpcSpecStandard>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return R.ok(spcSpecStandardService.listPage(keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.list')")
    public R<SpcSpecStandard> get(@PathVariable String id) {
        return R.ok(spcSpecStandardService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spc.param.create')")
    public R<SpcSpecStandard> create(@RequestBody SpcSpecStandard standard) {
        return R.ok(spcSpecStandardService.create(standard));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.create')")
    public R<Void> update(@PathVariable String id, @RequestBody SpcSpecStandard standard) {
        standard.setId(id);
        spcSpecStandardService.update(standard);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.param.delete')")
    public R<Void> delete(@PathVariable String id) {
        spcSpecStandardService.delete(id);
        return R.ok();
    }
}
