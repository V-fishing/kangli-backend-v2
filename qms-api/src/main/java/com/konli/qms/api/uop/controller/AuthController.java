package com.konli.qms.api.uop.controller;

import com.konli.qms.api.uop.dto.LoginRequest;
import com.konli.qms.api.uop.dto.LoginResponse;
import com.konli.qms.common.api.R;
import com.konli.qms.service.uop.AuthService;
import com.konli.qms.service.uop.dto.LoginResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证(代码规范§3.4,路径含 /api/v1;前端 vite 代理不再剥离 /api)。
 *
 * <p>真实登录:AuthService 校验 sys_user + BCrypt -> 签发 JWT。
 * 失败抛 BusinessException(401),由 GlobalExceptionHandler 转 R。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResult result = authService.login(req.getUsername(), req.getPassword());
        return R.ok(new LoginResponse(result.accessToken(), "Bearer", result.expiresIn()));
    }

    /**
     * 退出登录。
     *
     * <p>JWT 为无状态令牌,服务端不维护会话,无需做令牌失效处理;前端在收到 2xx 后清除本地 token 即可。</p>
     */
    @PostMapping("/logout")
    public R<?> logout() {
        return R.ok();
    }
}
