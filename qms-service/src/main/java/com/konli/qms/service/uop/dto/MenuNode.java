package com.konli.qms.service.uop.dto;

import java.util.List;

/**
 * 菜单树节点(供 /me 与 /menus/tree 返回)。
 * path 为绝对路由路径,前端直接用其渲染 RouterLink,不做拼接。
 */
public record MenuNode(
        String id,
        String parentId,
        String menuCode,
        String menuName,
        String menuType,
        String path,
        String component,
        String icon,
        Integer sortOrder,
        Boolean visible,
        List<MenuNode> children
) {
}
