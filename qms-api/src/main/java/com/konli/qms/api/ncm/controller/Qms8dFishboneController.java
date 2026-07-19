package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.Qms8dFishbone;
import com.konli.qms.service.ncm.Qms8dFishboneService;
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

/** 8D 鱼骨图 CRUD。ncm.8d.create */
@RestController
@RequestMapping("/api/v1/ncm/fishbones")
@RequiredArgsConstructor
public class Qms8dFishboneController {

    private final Qms8dFishboneService qms8dFishboneService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<List<Qms8dFishbone>> list(@RequestParam String d8Id) {
        return R.ok(qms8dFishboneService.list(d8Id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Qms8dFishbone> create(@RequestBody Qms8dFishbone fishbone) {
        return R.ok(qms8dFishboneService.create(fishbone));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Void> update(@PathVariable String id, @RequestBody Qms8dFishbone fishbone) {
        fishbone.setId(id);
        qms8dFishboneService.update(fishbone);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.8d.create')")
    public R<Void> delete(@PathVariable String id) {
        qms8dFishboneService.delete(id);
        return R.ok();
    }
}
