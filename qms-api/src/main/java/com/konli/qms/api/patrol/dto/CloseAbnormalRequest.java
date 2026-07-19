package com.konli.qms.api.patrol.dto;

import lombok.Data;

/** 关闭巡检异常请求 */
@Data
public class CloseAbnormalRequest {

    private String handleRemark;
}
