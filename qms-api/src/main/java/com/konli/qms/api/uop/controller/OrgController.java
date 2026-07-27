package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysOrg;
import com.konli.qms.service.uop.OrgService;
import com.konli.qms.service.uop.dto.OrgTreeNode;
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

@RestController
@RequestMapping("/api/v1/uop/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @GetMapping
    @PreAuthorize("hasAuthority('system.org.list')")
    public R<List<SysOrg>> list() {
        return R.ok(orgService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system.org.list')")
    public R<List<OrgTreeNode>> tree() {
        return R.ok(orgService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system.org.create')")
    public R<Void> save(@RequestBody SysOrg org) {
        orgService.save(org);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system.org.delete')")
    public R<Void> delete(@PathVariable String id) {
        orgService.delete(id);
        return R.ok();
    }
}
