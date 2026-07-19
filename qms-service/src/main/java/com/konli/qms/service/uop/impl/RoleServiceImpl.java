package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.security.PermissionLoader;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysRoleButton;
import com.konli.qms.domain.uop.entity.SysRoleMenu;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.entity.SysUserRole;
import com.konli.qms.domain.uop.mapper.SysRoleButtonMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMenuMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.domain.uop.mapper.SysUserRoleMapper;
import com.konli.qms.service.uop.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleButtonMapper sysRoleButtonMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final PermissionLoader permissionLoader;

    @Override
    public List<SysRole> list() {
        return sysRoleMapper.selectList(null);
    }

    @Override
    public void save(SysRole role) {
        if (role.getId() == null) {
            sysRoleMapper.insert(role);
        } else {
            sysRoleMapper.updateById(role);
        }
    }

    @Override
    public void delete(String id) {
        sysRoleMapper.deleteById(id);
        permissionLoader.evictAll();
    }

    @Override
    @Transactional
    public void assignMenus(String roleId, List<String> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (String mid : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(mid);
                sysRoleMenuMapper.insert(rm);
            }
        }
        permissionLoader.evictAll();
    }

    @Override
    @Transactional
    public void assignButtons(String roleId, List<String> buttonIds) {
        sysRoleButtonMapper.delete(new LambdaQueryWrapper<SysRoleButton>().eq(SysRoleButton::getRoleId, roleId));
        if (buttonIds != null) {
            for (String bid : buttonIds) {
                SysRoleButton rb = new SysRoleButton();
                rb.setRoleId(roleId);
                rb.setButtonId(bid);
                sysRoleButtonMapper.insert(rb);
            }
        }
        permissionLoader.evictAll();
    }

    @Override
    @Transactional
    public void assignUsers(String roleId, List<String> userIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        if (userIds != null) {
            for (String uid : userIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(uid);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
        permissionLoader.evictAll();
    }

    @Override
    public List<SysUser> users(String roleId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        List<String> userIds = urs.stream().map(SysUserRole::getUserId).toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return sysUserMapper.selectBatchIds(userIds);
    }
}
