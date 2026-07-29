package com.konli.qms.service.uop;

import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.service.uop.dto.CurrentUserVo;
import com.konli.qms.service.uop.dto.UserSelectVo;

import java.util.List;

public interface UserService {

    List<SysUser> list();

    /** 用户下拉选择项(仅启用用户,不含敏感字段)。 */
    List<UserSelectVo> listForSelect();

    CurrentUserVo getCurrent();

    SysUser create(SysUser user, String password);

    void update(SysUser user);

    void delete(String id);

    void resetPassword(String id, String newPassword);

    void assignRoles(String userId, List<String> roleIds);

    List<SysRole> getRoles(String userId);
}
