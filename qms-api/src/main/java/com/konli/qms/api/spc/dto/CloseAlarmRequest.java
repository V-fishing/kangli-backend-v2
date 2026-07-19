package com.konli.qms.api.spc.dto;

import lombok.Data;

/** SPC 告警关闭请求(记录原因 + 处置方式)。 */
@Data
public class CloseAlarmRequest {

    private String closeReason;    // 关闭原因

    private String disposition;    // 处置方式
}
