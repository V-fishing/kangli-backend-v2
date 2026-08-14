package com.konli.qms.api.uop.controller;

import com.konli.qms.api.uop.dto.CreateUserRequest;
import com.konli.qms.api.uop.dto.UpdateUserRequest;
import com.konli.qms.common.api.R;
import com.konli.qms.domain.uop.entity.SysRole;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.service.uop.UserService;
import com.konli.qms.service.uop.dto.CurrentUserVo;
import com.konli.qms.service.uop.dto.UserSelectVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户管理(CRUD + 改密 + 分配角色 + /me)。写操作需 system.user.create,删除需 system.user.delete。
 */
@RestController
@RequestMapping("/api/v1/uop")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public R<CurrentUserVo> me() {
        return R.ok(userService.getCurrent());
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('system.user.list')")
    public R<List<SysUser>> list() {
        return R.ok(userService.list());
    }

    /** 采集任务"选择接收人"下拉:返回启用用户的精简信息,无需 system.user.list 权限。 */
    @GetMapping("/users/select")
    @PreAuthorize("hasAuthority('spc.subgroup.create')")
    public R<List<UserSelectVo>> listForSelect() {
        return R.ok(userService.listForSelect());
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('system.user.create')")
    public R<SysUser> create(@Valid @RequestBody CreateUserRequest req) {
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setRealName(req.getRealName());
        u.setOrgId(req.getOrgId());
        u.setStatus(req.getStatus());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        return R.ok(userService.create(u, req.getPassword()));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('system.user.create')")
    public R<Void> update(@PathVariable String id, @RequestBody UpdateUserRequest req) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setRealName(req.getRealName());
        u.setOrgId(req.getOrgId());
        u.setStatus(req.getStatus());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        userService.update(u);
        return R.ok();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('system.user.delete')")
    public R<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return R.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('system.user.create')")
    public R<Void> resetPassword(@PathVariable String id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return R.ok();
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('system.role.assign')")
    public R<Void> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return R.ok();
    }

    @GetMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('system.user.list')")
    public R<List<SysRole>> getRoles(@PathVariable String id) {
        return R.ok(userService.getRoles(id));
    }
}
