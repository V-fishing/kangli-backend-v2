package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.service.uop.MenuService;
import com.konli.qms.service.uop.dto.MenuNode;
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
@RequestMapping("/api/v1/uop/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @PreAuthorize("hasAuthority('system.menu.list')")
    public R<List<SysMenu>> list() {
        return R.ok(menuService.list());
    }

    /** 菜单树(含 children):供角色菜单配置 UI 与前端动态导航构建 */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system.menu.list')")
    public R<List<MenuNode>> tree() {
        return R.ok(menuService.fullTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system.menu.create')")
    public R<Void> save(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system.menu.delete')")
    public R<Void> delete(@PathVariable String id) {
        menuService.delete(id);
        return R.ok();
    }
}
