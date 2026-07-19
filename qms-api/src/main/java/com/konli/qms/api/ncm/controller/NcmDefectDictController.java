package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmDefectDict;
import com.konli.qms.service.ncm.NcmDefectDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 不良字典 CRUD。ncm.defect.list / ncm.defect.create */
@RestController
@RequestMapping("/api/v1/ncm/defect-dicts")
@RequiredArgsConstructor
public class NcmDefectDictController {

    private final NcmDefectDictService ncmDefectDictService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.defect.list')")
    public R<List<NcmDefectDict>> list() {
        return R.ok(ncmDefectDictService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.defect.list')")
    public R<NcmDefectDict> get(@PathVariable String id) {
        return R.ok(ncmDefectDictService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.defect.create')")
    public R<NcmDefectDict> create(@RequestBody NcmDefectDict dict) {
        return R.ok(ncmDefectDictService.create(dict));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.defect.create')")
    public R<Void> update(@PathVariable String id, @RequestBody NcmDefectDict dict) {
        dict.setId(id);
        ncmDefectDictService.update(dict);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.defect.create')")
    public R<Void> delete(@PathVariable String id) {
        ncmDefectDictService.delete(id);
        return R.ok();
    }
}
