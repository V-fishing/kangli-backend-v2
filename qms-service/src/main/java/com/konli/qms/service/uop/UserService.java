package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.service.uop.dto.CurrentUserVo;

import java.util.List;

public interface UserService {

    List<SysUser> list();

    CurrentUserVo getCurrent();

    SysUser create(SysUser user, String password);

    void update(SysUser user);

    void delete(String id);

    void resetPassword(String id, String newPassword);

    void assignRoles(String userId, List<String> roleIds);

    List<SysRole> getRoles(String userId);
}
