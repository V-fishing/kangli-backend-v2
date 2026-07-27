package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
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
        // 普通用户只能创建本公司用户(防跨公司伪造 org_id);跨公司管理员账号(org_id=null)仅管理员可建
        CompanyContext.CurrentUser cur = CompanyContext.get();
        if (cur != null && !CompanyContext.isAdmin()) {
            user.setOrgId(cur.orgId());
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
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        assertUserOwnership(existing);
        // 禁止通过 update 改动归属(防跨公司数据搬运)
        user.setOrgId(existing.getOrgId());
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(String id) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        assertUserOwnership(existing);
        sysUserMapper.deleteById(id);
        permissionLoader.evictUser(id);
    }

    @Override
    public void resetPassword(String id, String newPassword) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 关键:校验目标用户归属,防止分公司用户管理员重置跨公司 admin 密码(水平越权→垂直提权)
        assertUserOwnership(existing);
        SysUser u = new SysUser();
        u.setId(id);
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(u);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        SysUser existing = sysUserMapper.selectById(userId);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        assertUserOwnership(existing);
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

    /**
     * 用户账号归属校验:比通用 {@link DataScopeGuard#ensureOwner} 更严 --
     * org_id=null 的用户是跨公司管理员账号,普通用户禁止任何操作(防借 resetPassword/assignRoles 提权)。
     */
    private void assertUserOwnership(SysUser existing) {
        if (existing.getOrgId() == null) {
            if (!CompanyContext.isAdmin()) {
                throw new BusinessException(403, "无权操作跨公司管理员账号");
            }
            return;
        }
        DataScopeGuard.ensureOwner(existing.getOrgId());
    }
}
