package com.konli.qms.api.uop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String realName;

    private String orgId;   // null=跨公司管理员

    private String status;  // 默认 启用
}
