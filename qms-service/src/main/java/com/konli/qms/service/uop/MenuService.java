package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.service.uop.dto.MenuNode;

import java.util.List;
import java.util.Set;

public interface MenuService {
    List<SysMenu> list();
    void save(SysMenu menu);
    void delete(String id);

    /** 完整可见菜单树(用于角色菜单配置 UI 与超管 /me)。 */
    List<MenuNode> fullTree();

    /**
     * 按菜单权限码集合过滤的菜单树:
     * 节点可见当且仅当自身码在 codes,或某祖先/后代码在 codes(保证模块与子页签结构完整)。
     */
    List<MenuNode> treeByCodes(Set<String> codes);
}
