package com.konli.qms.api.uop.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.service.uop.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** 菜单树(含children):扁平菜单转树,供前端动态路由构建 */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system.menu.list')")
    public R<List<Map<String, Object>>> tree() {
        List<SysMenu> flat = menuService.list();
        List<Map<String, Object>> nodes = flat.stream().map(m -> {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", m.getId());
            n.put("parentId", m.getParentId());
            n.put("menuCode", m.getMenuCode());
            n.put("menuName", m.getMenuName());
            n.put("menuType", m.getMenuType());
            n.put("path", m.getPath());
            n.put("component", m.getComponent());
            n.put("icon", m.getIcon());
            n.put("sortOrder", m.getSortOrder());
            n.put("visible", m.getVisible());
            n.put("children", new ArrayList<>());
            return n;
        }).collect(Collectors.toList());
        // 按 parentId 挂载
        Map<String, List<Map<String, Object>>> parentMap = nodes.stream()
                .filter(n -> n.get("parentId") != null)
                .collect(Collectors.groupingBy(n -> (String) n.get("parentId")));
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> n : nodes) {
            List<Map<String, Object>> ch = parentMap.get((String) n.get("id"));
            if (ch != null) n.put("children", ch);
            if (n.get("parentId") == null) roots.add(n);
        }
        return R.ok(roots);
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
