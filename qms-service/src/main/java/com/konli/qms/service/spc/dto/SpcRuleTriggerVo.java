package com.konli.qms.service.spc.dto;

import lombok.Data;

/** SPC 判异规则触发次数:每条规则命中异常子组的数量。 */
@Data
public class SpcRuleTriggerVo {
    private String code;
    private String name;
    private String level;
    private long cnt;
}
