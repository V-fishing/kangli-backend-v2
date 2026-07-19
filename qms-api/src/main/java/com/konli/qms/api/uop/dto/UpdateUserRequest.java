package com.konli.qms.api.uop.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String realName;

    private String orgId;

    private String status;
}
