package com.konli.qms.service.spc.dto;

import lombok.Data;

import java.util.List;

/**
 * 计数型控制图序列(P/NP/C/U)。
 * 前端按 chartType 选对应序列渲染:每种计数型图一张卡片。
 * values / ucl / cl / lcl 与 subgroups 一一对应(按子组时间顺序),便于直接喂 ECharts markLine。
 */
@Data
public class CountSeries {

    /** 图类型: P / NP / C / U */
    private String chartType;

    /** 每子组统计值(p=不合格率, np=不合格数, c=缺陷数, u=单位缺陷数) */
    private List<BigDecimal> values;

    /** 控制限(与 values 等长;变限图每点不同,定限图全相同) */
    private List<BigDecimal> ucl;
    private List<BigDecimal> cl;
    private List<BigDecimal> lcl;
}
