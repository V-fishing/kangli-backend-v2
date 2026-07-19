package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.service.uop.RoleService;
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
@RequestMapping("/api/v1/uop/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('system.role.list')")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system.role.list')")
    public R<Void> save(@RequestBody SysRole role) {
        roleService.save(role);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system.role.list')")
    public R<Void> delete(@PathVariable String id) {
        roleService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system.role.assign')")
    public R<Void> assignMenus(@PathVariable String id, @RequestBody List<String> menuIds) {
        roleService.assignMenus(id, menuIds);
        return R.ok();
    }

    @PostMapping("/{id}/buttons")
    @PreAuthorize("hasAuthority('system.role.assign')")
    public R<Void> assignButtons(@PathVariable String id, @RequestBody List<String> buttonIds) {
        roleService.assignButtons(id, buttonIds);
        return R.ok();
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("hasAuthority('system.role.assign')")
    public R<Void> assignUsers(@PathVariable String id, @RequestBody List<String> userIds) {
        roleService.assignUsers(id, userIds);
        return R.ok();
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('system.role.list')")
    public R<List<SysUser>> users(@PathVariable String id) {
        return R.ok(roleService.users(id));
    }
}
