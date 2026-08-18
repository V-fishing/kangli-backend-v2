package com.konli.qms.service.uop.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.mapper.SysButtonMapper;
import com.konli.qms.domain.uop.mapper.SysMenuMapper;
import com.konli.qms.domain.uop.mapper.SysRoleButtonMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMapper;
import com.konli.qms.domain.uop.mapper.SysRoleMenuMapper;
import com.konli.qms.domain.uop.mapper.SysUserRoleMapper;
import com.konli.qms.domain.uop.mapper.SysDataScopeMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色管理 delete 单元测试（M11 uop 权限，Mockito）。
 * 聚焦:不存在 404、分公司管理员只能删本 org 角色 403、删前清子表(FK 无级联)。
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplDeleteTest {

    @Mock SysRoleMapper sysRoleMapper;
    @Mock SysMenuMapper sysMenuMapper;
    @Mock SysButtonMapper sysButtonMapper;
    @Mock SysRoleMenuMapper sysRoleMenuMapper;
    @Mock SysRoleButtonMapper sysRoleButtonMapper;
    @Mock SysUserRoleMapper sysUserRoleMapper;
    @Mock SysUserMapper sysUserMapper;
    @Mock SysDataScopeMapper sysDataScopeMapper;
    @Mock com.konli.qms.common.security.PermissionLoader permissionLoader;
    @Mock JdbcTemplate jdbc;

    @InjectMocks
    RoleServiceImpl service;

    @BeforeEach
    @AfterEach
    void clear() {
        CompanyContext.clear();
    }

    private void setContext(String orgId) {
        CompanyContext.set(new CompanyContext.CurrentUser("u", "u", orgId, "org"));
    }

    @Test
    @DisplayName("delete: 角色不存在 -> 404")
    void delete_notFound_throws404() {
        when(sysRoleMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("delete: 分公司管理员删其他公司角色 -> 403")
    void delete_crossOrg_throws403() {
        setContext("MZ");
        SysRole role = new SysRole();
        role.setId("r-1");
        role.setOrgId("SZ"); // 其他分公司
        when(sysRoleMapper.selectById("r-1")).thenReturn(role);
        assertThatThrownBy(() -> service.delete("r-1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    @Test
    @DisplayName("delete: 同公司角色 -> 清子表后删除并清权限缓存")
    void delete_sameOrg_clearsChildrenAndDeletes() {
        setContext("MZ");
        SysRole role = new SysRole();
        role.setId("r-1");
        role.setOrgId("MZ");
        when(sysRoleMapper.selectById("r-1")).thenReturn(role);
        service.delete("r-1");
        verify(sysRoleMenuMapper).delete(any());
        verify(sysRoleButtonMapper).delete(any());
        verify(sysUserRoleMapper).delete(any());
        verify(jdbc).update(any(String.class), any(Object.class));
        verify(sysRoleMapper).deleteById("r-1");
        verify(permissionLoader).evictAll();
    }
}
