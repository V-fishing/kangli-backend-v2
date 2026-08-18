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
     * 计算需保留的菜单 id。保留规则(精确,杜绝"未授权兄弟菜单搭便车显示"):
     *  - selected:自身 menu_code 在 allowed(被直接授权)。
     *  - 向上:selected 节点的所有祖先链(保证父模块壳可见)。
     * 子菜单是否可见完全取决于其自身 menu_code 是否被授权;绝不因父模块被授权而连带展出
     * 全部未授权子菜单(此前"向下展开全部后代"的写法会导致:角色仅勾二级子菜单、约束自动补父
     * 模块码后,父模块下所有兄弟子菜单都被放出,点击即 403)。
     * 超管走 fullTree 不受此约束;模块级整模块可见的需求由显式授权各子菜单码满足(最小权限)。
     */
    private Set<String> computeKept(List<SysMenu> menus, Set<String> allowed) {
        Map<String, SysMenu> byId = menus.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        Set<String> selected = menus.stream()
                .filter(m -> allowed.contains(m.getMenuCode()))
                .map(SysMenu::getId)
                .collect(Collectors.toSet());

        Set<String> kept = new HashSet<>(selected);
        // 向上:选中节点的全部祖先(保证父模块壳可见)
        for (String sid : selected) {
            String pid = byId.get(sid) != null ? byId.get(sid).getParentId() : null;
            while (pid != null) {
                if (!kept.add(pid)) break; // 已处理过,避免重复遍历
                pid = byId.get(pid) != null ? byId.get(pid).getParentId() : null;
            }
        }
        return kept;
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
