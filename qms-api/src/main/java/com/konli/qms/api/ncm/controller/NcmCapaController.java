package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.dto.CapaVo;
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

/** CAPA:创建/查询/进度更新/关闭。ncm.capa.list / ncm.capa.create */
@RestController
@RequestMapping("/api/v1/ncm/capas")
@RequiredArgsConstructor
public class NcmCapaController {

    private final NcmCapaService ncmCapaService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.capa.list')")
    public R<List<QmsCapa>> list() {
        return R.ok(ncmCapaService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.capa.list')")
    public R<CapaVo> get(@PathVariable String id) {
        return R.ok(ncmCapaService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<QmsCapa> create(@RequestBody QmsCapa capa) {
        return R.ok(ncmCapaService.create(capa));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<Void> updateProgress(@PathVariable String id, @RequestParam Short progress) {
        ncmCapaService.updateProgress(id, progress);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ncm.capa.create')")
    public R<Void> close(@PathVariable String id) {
        ncmCapaService.close(id);
        return R.ok();
    }
}
