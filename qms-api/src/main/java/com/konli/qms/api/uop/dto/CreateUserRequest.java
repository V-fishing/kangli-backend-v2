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

    /** 邮箱(点对点邮件通知接收地址) */
    private String email;

    /** 手机号(短信通知 / 钉钉·企微桥接) */
    private String phone;
}
