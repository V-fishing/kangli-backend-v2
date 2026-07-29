package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.PermissionLoader;
import com.konli.qms.domain.uop.entity.SysButton;
import com.konli.qms.domain.uop.entity.SysDataScope;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysRoleButton;
import com.konli.qms.domain.uop.entity.SysRoleMenu;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.entity.SysUserRole;
import com.konli.qms.domain.uop.mapper.SysButtonMapper;
import com.konli.qms.domain.uop.mapper.SysDataScopeMapper;
import com.konli.qms.domain.uop.mapper.SysRoleButtonMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMenuMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.domain.uop.mapper.SysUserRoleMapper;
import com.konli.qms.service.uop.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysButtonMapper sysButtonMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleButtonMapper sysRoleButtonMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDataScopeMapper sysDataScopeMapper;
    private final PermissionLoader permissionLoader;
    private final JdbcTemplate jdbc;

    /**
     * 当前用户的分公司 org_id；跨公司管理员(dataScope=all, JWT orgId="ROOT")返回 null。
     */
    private String currentBranchOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return null;
        }
        String orgId = u.orgId();
        return (orgId == null || "ROOT".equals(orgId)) ? null : orgId;
    }

    @Override
    public List<SysRole> list(String orgIdParam) {
        String orgId = currentBranchOrgId();
        // 分公司管理员：只能看本 org，忽略传入参数
        if (orgId != null) {
            return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getOrgId, orgId));
        }
        // 跨公司管理员(sysadmin)：传了 orgId 则按该 org 过滤，否则看全部
        if (orgIdParam != null && !orgIdParam.isBlank()) {
            return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getOrgId, orgIdParam));
        }
        return sysRoleMapper.selectList(null);
    }

    @Override
    public void save(SysRole role) {
        String orgId = currentBranchOrgId();
        // 分公司管理员强制本 org（忽略请求体越权值）；sysadmin 可用请求体指定 org（含 null 建全局角色）
        if (orgId != null) {
            role.setOrgId(orgId);
        }
        if (role.getId() == null) {
            sysRoleMapper.insert(role);
        } else {
            sysRoleMapper.updateById(role);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        String curOrgId = currentBranchOrgId();
        // 分公司管理员只能删本 org 角色；sysadmin 可删任意
        if (curOrgId != null && !curOrgId.equals(role.getOrgId())) {
            throw new BusinessException(403, "只能删除本分公司的角色");
        }
        // 外键无 ON DELETE CASCADE，须先清子表
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        sysRoleButtonMapper.delete(new LambdaQueryWrapper<SysRoleButton>().eq(SysRoleButton::getRoleId, id));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        jdbc.update("DELETE FROM ops.sys_data_scope WHERE role_id = ?::uuid", id);
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

    @Override
    public List<SysButton> listButtons() {
        return sysButtonMapper.selectList(null);
    }

    @Override
    public List<SysButton> roleButtons(String roleId) {
        List<SysRoleButton> rbs = sysRoleButtonMapper.selectList(
                new LambdaQueryWrapper<SysRoleButton>().eq(SysRoleButton::getRoleId, roleId));
        List<String> ids = rbs.stream().map(SysRoleButton::getButtonId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return sysButtonMapper.selectBatchIds(ids);
    }

    @Override
    public List<String> roleMenus(String roleId) {
        List<SysRoleMenu> rms = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return rms.stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Override
    public SysRole getById(String id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void assignDataScopes(String roleId, List<SysDataScope> scopes) {
        sysDataScopeMapper.delete(
            new LambdaQueryWrapper<SysDataScope>().eq(SysDataScope::getRoleId, roleId));
        if (scopes != null) {
            for (SysDataScope ds : scopes) {
                ds.setRoleId(roleId);
                sysDataScopeMapper.insert(ds);
            }
        }
        permissionLoader.evictAll();
    }

    @Override
    public List<SysDataScope> getDataScopes(String roleId) {
        return sysDataScopeMapper.selectList(
            new LambdaQueryWrapper<SysDataScope>().eq(SysDataScope::getRoleId, roleId));
    }
}
