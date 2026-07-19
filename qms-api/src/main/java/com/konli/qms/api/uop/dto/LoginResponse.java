package com.konli.qms.api.uop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;   // Bearer
    private long expiresIn;     // 秒
}
