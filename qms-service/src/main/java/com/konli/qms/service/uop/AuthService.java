package com.konli.qms.service.uop;

import com.konli.qms.service.uop.dto.LoginResult;

/**
 * 认证服务(代码规范§2.1:{Module}Service 接口)。
 */
public interface AuthService {

    /**
     * 登录:校验 sys_user + BCrypt -> 签发 JWT。
     * @throws com.konli.qms.common.exception.BusinessException(401) 用户名/密码错误或账号停用
     */
    LoginResult login(String username, String password);
}
