package com.konli.qms.service.uop.dto;

import java.util.Set;

/** 当前登录用户信息(含权限码) */
public record CurrentUserVo(String userId, String username, String orgId,
                            String dataScope, Set<String> permissions) {
}
