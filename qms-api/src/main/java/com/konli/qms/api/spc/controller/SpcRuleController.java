package com.konli.qms.api.spc.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.spc.entity.SpcRule;
import com.konli.qms.service.spc.SpcRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** SPC 判异规则查询与启用/停用。 */
@RestController
@RequestMapping("/api/v1/spc/rules")
@RequiredArgsConstructor
public class SpcRuleController {

    private final SpcRuleService spcRuleService;

    @GetMapping
    @PreAuthorize("hasAuthority('spc.rule.list')")
    public R<List<SpcRule>> list() {
        return R.ok(spcRuleService.list());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spc.rule.list')")
    public R<Void> toggle(@PathVariable String id, @RequestParam boolean enabled) {
        spcRuleService.toggle(id, enabled);
        return R.ok();
    }
}
