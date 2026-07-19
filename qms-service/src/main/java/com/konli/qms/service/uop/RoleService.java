package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;

import java.util.List;

public interface RoleService {
    List<SysRole> list();
    void save(SysRole role);
    void delete(String id);
    void assignMenus(String roleId, List<String> menuIds);
    void assignButtons(String roleId, List<String> buttonIds);
    void assignUsers(String roleId, List<String> userIds);
    List<SysUser> users(String roleId);
}
