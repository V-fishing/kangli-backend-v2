package com.konli.qms.service.uop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.JwtUtil;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.uop.AuthService;
import com.konli.qms.service.uop.dto.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现。
 *
 * <p>dataScope 规则(简化多分公司):org_id 为 null -> 跨公司管理员(dataScope=all,看全部);
 * 否则 dataScope=org_id(仅看本公司)。JWT 携带 orgId + dataScope。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${qms.jwt.expiry:1800}")
    private long jwtExpiry;

    @Override
    public LoginResult login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"启用".equals(user.getStatus())) {
            throw new BusinessException(401, "账号已停用或锁定");
        }
        String orgId = user.getOrgId();
        String dataScope = (orgId == null) ? "all" : orgId;
        String token = jwtUtil.generate(user.getId(), user.getUsername(),
                orgId == null ? "ROOT" : orgId, dataScope);
        return new LoginResult(token, jwtExpiry);
    }
}
