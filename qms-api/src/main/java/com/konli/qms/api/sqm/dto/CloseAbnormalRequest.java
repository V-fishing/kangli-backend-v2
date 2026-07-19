package com.konli.qms.api.sqm.dto;

import lombok.Data;

/** 关闭来料异常整改单请求 */
@Data
public class CloseAbnormalRequest {

    private String disposal;

    private String disposalRemark;
}
