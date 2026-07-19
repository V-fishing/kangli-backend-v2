package com.konli.qms.api.fia.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.fia.entity.FiaTriggerType;
import com.konli.qms.service.fia.FiaTriggerTypeService;
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

/** 触发事件类型管理。fia.std.create。 */
@RestController
@RequestMapping("/api/v1/fia/triggers")
@RequiredArgsConstructor
public class FiaTriggerTypeController {

    private final FiaTriggerTypeService fiaTriggerTypeService;

    @GetMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<List<FiaTriggerType>> list() {
        return R.ok(fiaTriggerTypeService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<FiaTriggerType> create(@RequestBody FiaTriggerType triggerType) {
        return R.ok(fiaTriggerTypeService.create(triggerType));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> update(@PathVariable String id, @RequestBody FiaTriggerType triggerType) {
        triggerType.setId(id);
        fiaTriggerTypeService.update(triggerType);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> delete(@PathVariable String id) {
        fiaTriggerTypeService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('fia.std.create')")
    public R<Void> toggle(@PathVariable String id, @RequestParam boolean enabled) {
        fiaTriggerTypeService.toggle(id, enabled);
        return R.ok();
    }
}
