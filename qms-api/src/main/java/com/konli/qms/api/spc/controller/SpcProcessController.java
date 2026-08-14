package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcProcess;
import com.konli.qms.service.spc.SpcProcessService;
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

/** SPC 工序主数据 CRUD(参数的父级分组:装配/焊接/检测/系统...)。 */
@RestController
@RequestMapping("/api/v1/spc/processes")
@RequiredArgsConstructor
public class SpcProcessController {

    private final SpcProcessService spcProcessService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.process.list')")
    public R<List<SpcProcess>> list() {
        return R.ok(spcProcessService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('spc.process.list')")
    public R<PageResult<SpcProcess>> page(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return R.ok(spcProcessService.listPage(keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.process.list')")
    public R<SpcProcess> get(@PathVariable String id) {
        return R.ok(spcProcessService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('spc.process.create')")
    public R<SpcProcess> create(@RequestBody SpcProcess process) {
        return R.ok(spcProcessService.create(process));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.process.create')")
    public R<Void> update(@PathVariable String id, @RequestBody SpcProcess process) {
        process.setId(id);
        spcProcessService.update(process);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.process.delete')")
    public R<Void> delete(@PathVariable String id) {
        spcProcessService.delete(id);
        return R.ok();
    }
}
