package com.konli.qms.api.ncm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.ncm.entity.NcmAlertEscalation;
import com.konli.qms.service.ncm.NcmAlertEscalationService;
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

/** 告警升级配置 CRUD。ncm.record.create */
@RestController
@RequestMapping("/api/v1/ncm/escalations")
@RequiredArgsConstructor
public class NcmAlertEscalationController {

    private final NcmAlertEscalationService ncmAlertEscalationService;

    @GetMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<List<NcmAlertEscalation>> list() {
        return R.ok(ncmAlertEscalationService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<NcmAlertEscalation> create(@RequestBody NcmAlertEscalation escalation) {
        return R.ok(ncmAlertEscalationService.create(escalation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Void> update(@PathVariable String id, @RequestBody NcmAlertEscalation escalation) {
        escalation.setId(id);
        ncmAlertEscalationService.update(escalation);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ncm.record.create')")
    public R<Void> delete(@PathVariable String id) {
        ncmAlertEscalationService.delete(id);
        return R.ok();
    }
}
