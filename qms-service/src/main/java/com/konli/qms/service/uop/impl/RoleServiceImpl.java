package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.PermissionLoader;
import com.konli.qms.domain.uop.entity.SysButton;
import com.konli.qms.domain.uop.entity.SysDataScope;
import com.konli.qms.domain.uop.entity.SysMenu;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysRoleButton;
import com.konli.qms.domain.uop.entity.SysRoleMenu;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.entity.SysUserRole;
import com.konli.qms.domain.uop.mapper.SysButtonMapper;
import com.konli.qms.domain.uop.mapper.SysDataScopeMapper;
import com.konli.qms.domain.uop.mapper.SysMenuMapper;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
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
        if (menuIds != null && !menuIds.isEmpty()) {
            // 祖先闭包归一化：选了子级菜单必须连带其所有祖先目录，避免"有子无父"的矛盾数据
            Set<String> expanded = expandWithAncestors(menuIds, loadMenuParentMap());
            for (String mid : expanded) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(mid);
                sysRoleMenuMapper.insert(rm);
            }
        }
        // 反向三级约束: 勾选菜单即授予该菜单下挂的 *.list 查询权限码(若已注册)。
        // 消除"给了二级菜单、却因未单独勾列表按钮码而进页 403"的割裂: 菜单可见性天然
        // 包含"能查看该页列表"。增删改等非 .list 码仍须单独勾选(最小权限)。
        grantMenuListCodes(roleId);
        permissionLoader.evictAll();
    }

    /**
     * 基于角色当前已授权的菜单集合, 把每个菜单下挂的 *.list 查询权限码补进 sys_role_button
     * (幂等: 已存在的行因唯一约束跳过)。与 assignButtons 的"DELETE ALL 再重插"兼容——
     * 不论保存顺序如何, 最终角色都能拿到其菜单对应的列表查询码。
     */
    private void grantMenuListCodes(String roleId) {
        List<SysRoleMenu> rms = sysRoleMenuMapper.selectList(
            new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (rms.isEmpty()) return;
        Set<String> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
        List<SysButton> listBtns = sysButtonMapper.selectList(
            new LambdaQueryWrapper<SysButton>()
                .in(SysButton::getMenuId, menuIds)
                .likeRight(SysButton::getBtnCode, ".list"));
        if (listBtns.isEmpty()) return;
        Set<String> existing = sysRoleButtonMapper.selectList(
                new LambdaQueryWrapper<SysRoleButton>().eq(SysRoleButton::getRoleId, roleId))
            .stream().map(SysRoleButton::getButtonId).collect(Collectors.toSet());
        for (SysButton b : listBtns) {
            if (existing.contains(b.getId())) continue;
            SysRoleButton rb = new SysRoleButton();
            rb.setRoleId(roleId);
            rb.setButtonId(b.getId());
            sysRoleButtonMapper.insert(rb);
        }
    }

    @Override
    @Transactional
    public void assignButtons(String roleId, List<String> buttonIds) {
        sysRoleButtonMapper.delete(new LambdaQueryWrapper<SysRoleButton>().eq(SysRoleButton::getRoleId, roleId));
        if (buttonIds != null && !buttonIds.isEmpty()) {
            for (String bid : buttonIds) {
                SysRoleButton rb = new SysRoleButton();
                rb.setRoleId(roleId);
                rb.setButtonId(bid);
                sysRoleButtonMapper.insert(rb);
            }
            // 按钮隐含其所属页面：把按钮挂载的菜单(及其祖先)补进 sys_role_menu，避免"有按钮无页面"
            List<SysButton> buttons = sysButtonMapper.selectBatchIds(buttonIds);
            Set<String> ownerMenuIds = new LinkedHashSet<>();
            for (SysButton b : buttons) {
                if (b.getMenuId() != null) {
                    ownerMenuIds.add(b.getMenuId());
                }
            }
            if (!ownerMenuIds.isEmpty()) {
                Set<String> required = expandWithAncestors(ownerMenuIds, loadMenuParentMap());
                Set<String> existing = new LinkedHashSet<>(roleMenus(roleId));
                for (String mid : required) {
                    if (!existing.contains(mid)) {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(mid);
                        sysRoleMenuMapper.insert(rm);
                    }
                }
            }
        }
        // 反向三级约束: 不论保存顺序(menus 先/buttons 先), 基于最新菜单集合补 .list 码,
        // 抵消本方法开头 DELETE ALL 对 assignMenus 已补 .list 码的影响。
        grantMenuListCodes(roleId);
        permissionLoader.evictAll();
    }

    /** 加载全量菜单，构建 id → parentId 映射（parentId 为 null 表示顶级）。 */
    private Map<String, String> loadMenuParentMap() {
        List<SysMenu> all = sysMenuMapper.selectList(null);
        Map<String, String> idToParent = new HashMap<>();
        for (SysMenu m : all) {
            idToParent.put(m.getId(), m.getParentId());
        }
        return idToParent;
    }

    /** 把选中的菜单集合向上回溯补齐所有祖先，返回去重后的闭包集合。 */
    private Set<String> expandWithAncestors(Collection<String> menuIds, Map<String, String> idToParent) {
        Set<String> result = new LinkedHashSet<>(menuIds);
        for (String id : new ArrayList<>(result)) {
            String p = idToParent.get(id);
            int guard = 0;
            while (p != null && guard++ < 64) { // guard 防御环状 parentId 造成死循环
                result.add(p);
                p = idToParent.get(p);
            }
        }
        return result;
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
        List<SysButton> buttons = sysButtonMapper.selectList(null);
        // 带出所属菜单名（含 visible=false 的隐藏菜单，树接口不返回，按钮分组需要展示）
        Set<String> menuIds = buttons.stream()
                .map(SysButton::getMenuId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        if (!menuIds.isEmpty()) {
            Map<String, String> nameMap = sysMenuMapper.selectBatchIds(menuIds).stream()
                    .collect(Collectors.toMap(SysMenu::getId, SysMenu::getMenuName, (a, b) -> a));
            buttons.forEach(b -> {
                if (b.getMenuId() != null) {
                    b.setMenuName(nameMap.get(b.getMenuId()));
                }
            });
        }
        return buttons;
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
