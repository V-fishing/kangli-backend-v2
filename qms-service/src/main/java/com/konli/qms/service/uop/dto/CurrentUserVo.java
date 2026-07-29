package com.konli.qms.service.uop.dto;

import java.util.List;
import java.util.Set;

/** 当前登录用户信息(含权限码与可见菜单树) */
public record CurrentUserVo(String userId, String username, String orgId,
                            String dataScope, Set<String> permissions, List<MenuNode> menus) {
}
