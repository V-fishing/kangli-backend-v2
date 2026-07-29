package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.domain.uop.mapper.SysMenuMapper;
import com.konli.qms.service.uop.MenuService;
import com.konli.qms.service.uop.dto.MenuNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> list() {
        return sysMenuMapper.selectList(null);
    }

    @Override
    public void save(SysMenu menu) {
        if (menu.getId() == null) {
            sysMenuMapper.insert(menu);
        } else {
            sysMenuMapper.updateById(menu);
        }
    }

    @Override
    public void delete(String id) {
        sysMenuMapper.deleteById(id);
    }

    @Override
    public List<MenuNode> fullTree() {
        return buildTree(visibleMenus());
    }

    @Override
    public List<MenuNode> treeByCodes(Set<String> codes) {
        List<SysMenu> visible = visibleMenus();
        Set<String> allowed = new HashSet<>(codes);
        allowed.retainAll(visible.stream().map(SysMenu::getMenuCode).collect(Collectors.toSet()));
        if (allowed.isEmpty()) {
            return List.of();
        }
        Set<String> kept = computeKept(visible, allowed);
        List<SysMenu> filtered = visible.stream()
                .filter(m -> kept.contains(m.getId()))
                .collect(Collectors.toList());
        return buildTree(filtered);
    }

    /** 仅取 visible=true 的菜单(废弃项不参与导航/配置)。 */
    private List<SysMenu> visibleMenus() {
        return list().stream()
                .filter(m -> !Boolean.FALSE.equals(m.getVisible()))
                .collect(Collectors.toList());
    }

    /**
     * 计算需保留的菜单 id:自身码在 allowed,或含"码在 allowed 的后代",或含"码在 allowed 的祖先"。
     * 这样按根模块码(fia/spc/...)授权即显示整个模块;按子页签码授权即只显示该页签(模块仍在)。
     */
    private Set<String> computeKept(List<SysMenu> menus, Set<String> allowed) {
        Map<String, SysMenu> byId = menus.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        Map<String, List<SysMenu>> childrenMap = new HashMap<>();
        for (SysMenu m : menus) {
            if (m.getParentId() != null) {
                childrenMap.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m);
            }
        }
        Set<String> selected = menus.stream()
                .filter(m -> allowed.contains(m.getMenuCode()))
                .map(SysMenu::getId)
                .collect(Collectors.toSet());

        Map<String, Boolean> hasSelDesc = new HashMap<>();
        Set<String> kept = new HashSet<>();
        for (SysMenu m : menus) {
            if (hasSelectedDescendant(m, selected, childrenMap, hasSelDesc)) {
                kept.add(m.getId());
            }
        }
        // 向上找祖先:若祖先被选中或已被保留,则本节点保留
        for (SysMenu m : menus) {
            if (kept.contains(m.getId())) continue;
            String pid = m.getParentId();
            while (pid != null) {
                if (selected.contains(pid) || kept.contains(pid)) {
                    kept.add(m.getId());
                    break;
                }
                SysMenu p = byId.get(pid);
                pid = (p != null) ? p.getParentId() : null;
            }
        }
        return kept;
    }

    private boolean hasSelectedDescendant(SysMenu node, Set<String> selected,
                                          Map<String, List<SysMenu>> childrenMap,
                                          Map<String, Boolean> cache) {
        if (cache.containsKey(node.getId())) return cache.get(node.getId());
        boolean sel = selected.contains(node.getId());
        for (SysMenu k : childrenMap.getOrDefault(node.getId(), List.of())) {
            if (hasSelectedDescendant(k, selected, childrenMap, cache)) sel = true;
        }
        cache.put(node.getId(), sel);
        return sel;
    }

    private List<MenuNode> buildTree(List<SysMenu> menus) {
        Map<String, List<SysMenu>> childrenMap = new HashMap<>();
        for (SysMenu m : menus) {
            if (m.getParentId() != null) {
                childrenMap.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m);
            }
        }
        return menus.stream()
                .filter(m -> m.getParentId() == null)
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                .map(m -> toNode(m, childrenMap))
                .collect(Collectors.toList());
    }

    private MenuNode toNode(SysMenu m, Map<String, List<SysMenu>> childrenMap) {
        List<MenuNode> kids = childrenMap.getOrDefault(m.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(x -> x.getSortOrder() == null ? 0 : x.getSortOrder()))
                .map(c -> toNode(c, childrenMap))
                .collect(Collectors.toList());
        return new MenuNode(m.getId(), m.getParentId(), m.getMenuCode(), m.getMenuName(),
                m.getMenuType(), m.getPath(), m.getComponent(), m.getIcon(),
                m.getSortOrder(), m.getVisible(), kids);
    }
}
