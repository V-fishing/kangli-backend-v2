package com.konli.qms.api.ncm.dto;

import lombok.Data;

@Data
public class StageApproveDTO {
    private String stageCode;
    private boolean approved;
    private String comment;
    /** 电子签名口令:当前登录用户本人密码(经 PasswordEncoder 校验),替代原签批人姓名输入 */
    private String password;
}
