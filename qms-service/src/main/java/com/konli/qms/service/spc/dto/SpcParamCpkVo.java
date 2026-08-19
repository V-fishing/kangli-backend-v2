package com.konli.qms.service.spc.dto;

import lombok.Data;

/** SPC 跨参数 CPK 对比:以参数维度聚合的过程能力指数(与概览/趋势同口径,优先取落库值)。 */
@Data
public class SpcParamCpkVo {
    private String paramId;
    private String paramName;
    /** 工序显示名(区分同名参数,如多个「关键尺寸」分属不同工序/检验项)。 */
    private String procName;
    /** 来源工单号(同名参数时用于进一步区分,可空)。 */
    private String srcWoNo;
    private Double cpk;
    private String level;
    private Integer sampleCount;
    private String calcNote;
}
