package com.konli.qms.service.spc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 计数型(P/NP/C/U)参数的过程水平聚合(替代计量型 Cp/Cpk)。
 * 计数型无规格上下限,不计算过程能力指数,改用过程平均不合格率/缺陷率与合格率表达质量水平。
 * 与 ControlChartVo/CountSeries 同位于 service 层 dto 包,供 qms-api Controller 引用。
 */
@Data
public class CountCapabilityVo {

    /** 参数是否含计数型图(P/NP/C/U)。false 表示非计数型参数,本 VO 无业务含义。 */
    private boolean countType;

    /** 计数图主类型: P / NP / C / U(取 chartCandidates 中首个计数图)。非计数型为 null。 */
    private String chartKind;

    /** 过程平均不合格率 p̄ = Σ不合格数 / Σ检验数(P/NP 图)。非 P/NP 为 null。 */
    @JsonProperty("pBar")
    private BigDecimal pBar;

    /** 百万机会缺陷数 PPM = p̄ × 1_000_000(P/NP 图)。非 P/NP 为 null。 */
    private BigDecimal ppm;

    /** 合格率(良品率) = 1 − p̄(P/NP 图)。非 P/NP 为 null。 */
    private BigDecimal yieldRate;

    /** 平均单位缺陷数 ū = Σ缺陷数 / Σ检验单位数(C/U 图)。非 C/U 为 null。 */
    @JsonProperty("uBar")
    private BigDecimal uBar;

    /** 单位缺陷数 DPU = ū(C/U 图)。非 C/U 为 null。 */
    private BigDecimal dpu;

    /** 参与聚合的计数子组数(非计数型恒为 0)。 */
    private int sampleCount;
}
