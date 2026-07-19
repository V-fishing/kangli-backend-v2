package com.konli.qms.api.sqm.dto;

import lombok.Data;

/** 关闭审核不符合项请求(填写验证结论) */
@Data
public class CloseNcRequest {

    private String verifyResult;

    private String verifyComment;
}
