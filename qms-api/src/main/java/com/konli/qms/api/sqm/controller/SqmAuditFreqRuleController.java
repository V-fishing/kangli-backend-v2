package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmAuditFreqRule;
import com.konli.qms.service.sqm.SqmAuditFreqRuleService;
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

/** 审核频次规则:查询/新增/编辑/删除。sqm.audit.create */
@RestController
@RequestMapping("/api/v1/sqm/audit-freq-rules")
@RequiredArgsConstructor
public class SqmAuditFreqRuleController {

    private final SqmAuditFreqRuleService sqmAuditFreqRuleService;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<List<SqmAuditFreqRule>> list() {
        return R.ok(sqmAuditFreqRuleService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<SqmAuditFreqRule> create(@RequestBody SqmAuditFreqRule rule) {
        return R.ok(sqmAuditFreqRuleService.create(rule));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> update(@RequestBody SqmAuditFreqRule rule) {
        sqmAuditFreqRuleService.update(rule);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.audit.create')")
    public R<Void> delete(@PathVariable String id) {
        sqmAuditFreqRuleService.delete(id);
        return R.ok();
    }
}
