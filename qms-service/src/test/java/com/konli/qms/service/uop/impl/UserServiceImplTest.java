package com.konli.qms.service.uop.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysRoleMapper;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.domain.uop.mapper.SysUserRoleMapper;
import com.konli.qms.service.uop.MenuService;
import com.konli.qms.service.uop.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户管理 Service 单元测试（M11 uop，Mockito）。
 * 聚焦越权防护与存在性校验:getCurrent 未认证 401、create 重名 400、
 * update/resetPassword/assignRoles 不存在 404、以及跨公司管理员账号提权防护 403。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock SysUserMapper sysUserMapper;
    @Mock SysUserRoleMapper sysUserRoleMapper;
    @Mock SysRoleMapper sysRoleMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock com.konli.qms.common.security.PermissionLoader permissionLoader;
    @Mock MenuService menuService;

    @InjectMocks
    UserServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("ENC");
        lenient().when(permissionLoader.loadPermissionCodes(anyString())).thenReturn(java.util.Set.of());
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    private void setContext(String userId, String orgId, String dataScope) {
        CompanyContext.set(new CompanyContext.CurrentUser(userId, "u", orgId, dataScope));
    }

    // ---- getCurrent ----

    @Test
    @DisplayName("getCurrent: 未认证 -> 401")
    void getCurrent_noAuth_throws401() {
        assertThatThrownBy(() -> service.getCurrent())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    @DisplayName("getCurrent: 已认证 -> 返回当前用户 VO")
    void getCurrent_authed_returnsVo() {
        setContext("user-1", "MZ", "org");
        var vo = service.getCurrent();
        assertThat(vo.userId()).isEqualTo("user-1");
        assertThat(vo.orgId()).isEqualTo("MZ");
    }

    // ---- create ----

    @Test
    @DisplayName("create: 用户名已存在 -> 400")
    void create_duplicate_throws400() {
        SysUser u = new SysUser();
        u.setUsername("alice");
        when(sysUserMapper.selectCount(any())).thenReturn(1L);
        assertThatThrownBy(() -> service.create(u, "pw"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("create: 普通用户创建 -> 强制本公司 org 并插入")
    void create_normal_forcesOrgAndInserts() {
        setContext("admin-1", "MZ", "org");
        SysUser u = new SysUser();
        u.setUsername("bob");
        u.setOrgId("SZ"); // 试图伪造跨公司
        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        SysUser saved = service.create(u, "pw");
        assertThat(saved.getOrgId()).isEqualTo("MZ"); // 被强制改回当前公司
        assertThat(saved.getStatus()).isEqualTo("启用");
        verify(sysUserMapper).insert(any(SysUser.class));
    }

    // ---- update ----

    @Test
    @DisplayName("update: 用户不存在 -> 404")
    void update_notFound_throws404() {
        SysUser u = new SysUser();
        u.setId("missing");
        when(sysUserMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.update(u))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("update: 非管理员操作跨公司管理员账号(org_id=null) -> 403 提权防护")
    void update_crossOrgAdmin_throws403() {
        setContext("mgr-1", "MZ", "org"); // 非超管
        SysUser existing = new SysUser();
        existing.setId("root-1");
        existing.setOrgId(null); // 跨公司管理员账号
        when(sysUserMapper.selectById("root-1")).thenReturn(existing);
        SysUser u = new SysUser();
        u.setId("root-1");
        assertThatThrownBy(() -> service.update(u))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    // ---- resetPassword ----

    @Test
    @DisplayName("resetPassword: 用户不存在 -> 404")
    void resetPassword_notFound_throws404() {
        when(sysUserMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.resetPassword("missing", "newpw"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("resetPassword: 非管理员重置跨公司管理员密码 -> 403(水平越权→垂直提权防护)")
    void resetPassword_crossOrgAdmin_throws403() {
        setContext("mgr-1", "MZ", "org");
        SysUser existing = new SysUser();
        existing.setId("root-1");
        existing.setOrgId(null);
        when(sysUserMapper.selectById("root-1")).thenReturn(existing);
        assertThatThrownBy(() -> service.resetPassword("root-1", "hacked"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    @Test
    @DisplayName("resetPassword: 超管可重置跨公司管理员(org_id=null 放行)")
    void resetPassword_superAdmin_allowed() {
        setContext("super", null, "all"); // 超管
        SysUser existing = new SysUser();
        existing.setId("root-1");
        existing.setOrgId(null);
        when(sysUserMapper.selectById("root-1")).thenReturn(existing);
        service.resetPassword("root-1", "newpw");
        ArgumentCaptor<SysUser> cap = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(cap.capture());
        assertThat(cap.getValue().getPasswordHash()).isEqualTo("ENC");
    }

    // ---- assignRoles ----

    @Test
    @DisplayName("assignRoles: 用户不存在 -> 404")
    void assignRoles_notFound_throws404() {
        when(sysUserMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> service.assignRoles("missing", java.util.List.of("r1")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 404);
    }

    @Test
    @DisplayName("assignRoles: 非管理员给跨公司管理员分配角色 -> 403")
    void assignRoles_crossOrgAdmin_throws403() {
        setContext("mgr-1", "MZ", "org");
        SysUser existing = new SysUser();
        existing.setId("root-1");
        existing.setOrgId(null);
        when(sysUserMapper.selectById("root-1")).thenReturn(existing);
        assertThatThrownBy(() -> service.assignRoles("root-1", java.util.List.of("r1")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    @Test
    @DisplayName("assignRoles: 正常 -> 先删后插并清权限缓存")
    void assignRoles_normal_replacesAndEvicts() {
        setContext("mgr-1", "MZ", "org");
        SysUser existing = new SysUser();
        existing.setId("u-1");
        existing.setOrgId("MZ");
        when(sysUserMapper.selectById("u-1")).thenReturn(existing);
        service.assignRoles("u-1", java.util.List.of("r1", "r2"));
        verify(sysUserRoleMapper).delete(any());
        verify(sysUserRoleMapper, org.mockito.Mockito.times(2))
                .insert(any(com.konli.qms.domain.uop.entity.SysUserRole.class));
        verify(permissionLoader).evictUser("u-1");
    }
}
