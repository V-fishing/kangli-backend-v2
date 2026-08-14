package com.konli.qms.api.sqm.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.sqm.entity.SqmAbnormalRule;
import com.konli.qms.service.sqm.SqmAbnormalRuleService;
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

/** 来料异常严重度判定规则。sqm.abnormal-rule.list / sqm.abnormal-rule.edit */
@RestController
@RequestMapping("/api/v1/sqm/abnormal-rule")
@RequiredArgsConstructor
public class SqmAbnormalRuleController {

    private final SqmAbnormalRuleService service;

    @GetMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.rule.config')")
    public R<List<SqmAbnormalRule>> list() {
        return R.ok(service.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sqm.abnormal.rule.config')")
    public R<SqmAbnormalRule> save(@RequestBody SqmAbnormalRule rule) {
        return R.ok(service.save(rule));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sqm.abnormal.rule.config')")
    public R<Void> delete(@PathVariable String id) {
        service.delete(id);
        return R.ok();
    }
}
