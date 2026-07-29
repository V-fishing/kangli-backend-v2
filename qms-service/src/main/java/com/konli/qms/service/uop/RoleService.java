package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysButton;
import com.konli.qms.domain.uop.entity.SysDataScope;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;

import java.util.List;

public interface RoleService {
    List<SysRole> list(String orgId);
    void save(SysRole role);
    void delete(String id);
    void assignMenus(String roleId, List<String> menuIds);
    void assignButtons(String roleId, List<String> buttonIds);
    void assignUsers(String roleId, List<String> userIds);
    List<SysUser> users(String roleId);
    List<SysButton> listButtons();
    List<SysButton> roleButtons(String roleId);
    List<String> roleMenus(String roleId);
    SysRole getById(String id);
    void assignDataScopes(String roleId, List<SysDataScope> scopes);
    List<SysDataScope> getDataScopes(String roleId);
}
