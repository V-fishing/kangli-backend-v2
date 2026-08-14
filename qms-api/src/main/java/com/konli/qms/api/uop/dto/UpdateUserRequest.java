package com.konli.qms.api.uop.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String realName;

    private String orgId;

    private String status;

    /** 邮箱(点对点邮件通知接收地址) */
    private String email;

    /** 手机号(短信通知 / 钉钉·企微桥接) */
    private String phone;
}
