package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysDelegation;
import com.konli.qms.service.uop.DelegationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 代班委派(创建/列表/撤销)。需 system.delegation.manage 权限。 */
@RestController
@RequestMapping("/api/v1/uop/delegations")
@RequiredArgsConstructor
public class DelegationController {

    private final DelegationService delegationService;

    @GetMapping
    @PreAuthorize("hasAuthority('system.delegation.manage')")
    public R<List<SysDelegation>> list() {
        return R.ok(delegationService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system.delegation.manage')")
    public R<Void> create(@RequestBody SysDelegation delegation) {
        delegationService.create(delegation);
        return R.ok();
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('system.delegation.manage')")
    public R<Void> revoke(@PathVariable String id) {
        delegationService.revoke(id);
        return R.ok();
    }
}
