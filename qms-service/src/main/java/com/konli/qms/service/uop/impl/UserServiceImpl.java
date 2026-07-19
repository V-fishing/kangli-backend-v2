package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.PermissionLoader;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.entity.SysUserRole;
import com.konli.qms.domain.uop.mapper.SysRoleMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.domain.uop.mapper.SysUserRoleMapper;
import com.konli.qms.service.uop.UserService;
import com.konli.qms.service.uop.dto.CurrentUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionLoader permissionLoader;

    @Override
    public List<SysUser> list() {
        return sysUserMapper.selectList(null);
    }

    @Override
    public CurrentUserVo getCurrent() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null) {
            throw new BusinessException(401, "未认证");
        }
        Set<String> perms = permissionLoader.loadPermissionCodes(u.userId());
        return new CurrentUserVo(u.userId(), u.username(), u.orgId(), u.dataScope(), perms);
    }

    @Override
    public SysUser create(SysUser user, String password) {
        Long c = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (c != null && c > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        user.setPasswordHash(passwordEncoder.encode(password));
        if (user.getStatus() == null) {
            user.setStatus("启用");
        }
        sysUserMapper.insert(user);
        return user;
    }

    @Override
    public void update(SysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(String id) {
        sysUserMapper.deleteById(id);
        permissionLoader.evictUser(id);
    }

    @Override
    public void resetPassword(String id, String newPassword) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(u);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            for (String rid : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(rid);
                sysUserRoleMapper.insert(ur);
            }
        }
        permissionLoader.evictUser(userId);
    }

    @Override
    public List<SysRole> getRoles(String userId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<String> roleIds = urs.stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds);
    }
}
