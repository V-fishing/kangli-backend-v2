package com.konli.qms.service.uop.dto;

import lombok.Data;

/** 用户下拉选择项(不含密码等敏感字段)。 */
@Data
public class UserSelectVo {
    private String id;
    private String username;
    private String realName;
    private String orgId;
}
