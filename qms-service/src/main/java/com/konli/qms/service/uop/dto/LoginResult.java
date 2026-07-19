package com.konli.qms.service.uop.dto;

/**
 * 登录结果(service 层 DTO,避免 service 反向依赖 api 层)。
 */
public record LoginResult(String accessToken, long expiresIn) {
}
